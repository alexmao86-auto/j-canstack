package can.interfaces.vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import can.BusABC;
import can.Filter;
import can.Message;
import can.Util;
import can.interfaces.vector.jni.Jvxlapi;
import can.interfaces.vector.jni.XLcanFdConf;
import can.interfaces.vector.jni.XLcanRxEvent;
import can.interfaces.vector.jni.XLcanTxEvent;
import can.interfaces.vector.jni.XLchannelConfig;
import can.interfaces.vector.jni.XLevent;

/** The CAN Bus implemented for the Vector interface. */
public class VectorBus extends BusABC {

	public Long poll_interval; // millisecond
	public String app_name = "";
	public String channel_info;
	public int[] channels;
	public long[] port_handle = new long[1];
	public long mask;
	public boolean fd;
	public Map<Integer, Long> channel_masks;
	public Map<Integer, Integer> index_to_channel;
	public XLcanFdConf canFdConf;
	public long[] event_handle;
	public long time_offset;
	public boolean is_filtered;

	public VectorBus(int[] channels, List<Filter> can_filters, Long poll_interval, Boolean receive_own_messages,
			Integer bitrate, Integer rx_queue_size, String app_name, Integer serial, Boolean fd, Integer data_bitrate,
			Integer sjwAbr, Integer tseg1Abr, Integer tseg2Abr, Integer sjwDbr, Integer tseg1Dbr, Integer tseg2Dbr,
			String[] args) throws Exception {
		// super(channels, can_filters, args);

		// Default value if input is null
		if (receive_own_messages == null)
			receive_own_messages = false;
		if (app_name == null)
			app_name = "CANalyzer";
		if (rx_queue_size == null)
			rx_queue_size = (int) Math.pow(2, 14);
		if (fd == null)
			fd = false;
		if (sjwAbr == null)
			sjwAbr = 2;
		if (tseg1Abr == null)
			tseg1Abr = 6;
		if (tseg2Abr == null)
			tseg2Abr = 3;
		if (sjwDbr == null)
			sjwDbr = 2;
		if (tseg1Dbr == null)
			tseg1Dbr = 6;
		if (tseg2Dbr == null)
			tseg2Dbr = 3;

		if (poll_interval == null) {
			this.poll_interval = 10l;
		} else {
			this.poll_interval = poll_interval;
		}
		this.channels = channels;

		if (app_name != null) {
			this.app_name = app_name;
		}
		StringBuffer sb = new StringBuffer("Application " + this.app_name + " : ");
		for (int ch : this.channels) {
			sb.append("CAN " + (ch + 1));
		}
		this.channel_info = sb.toString();

		if (serial != null) {
			app_name = null;
			List<Integer> channel_index = new ArrayList<>();
			List<XLchannelConfig> channel_configs = Jvxlapi.get_channel_configs();
			for (XLchannelConfig channel_config : channel_configs) {
				if (channel_config.serialNumber == serial.intValue()) {
					if (-1 != Arrays.binarySearch(this.channels, channel_config.hwChannel)) {
						channel_index.add(channel_config.hwChannel);
					}
				}
			}
			if (channel_index.size() > 0) {
				if (channel_index.size() != this.channels.length)
					logger.info("At least one defined channel wasn't found on the specified hardware.");
				int[] cns = new int[channel_index.size()];
				int idx = 0;
				for (int i : channel_index)
					cns[idx++] = i;
				this.channels = cns;
			} else {
				throw new Exception("None of the configured channels could be found on the specified hardware.");
			}
		}

		check_status(Jvxlapi.xlOpenDriver(), "xlOpenDriver");
		this.port_handle[0] = -1L;
		this.mask = 0;
		this.fd = fd;

		// Get channels masks
		this.channel_masks = new HashMap<>();
		this.index_to_channel = new HashMap<>();

		int idx = 0;
		long mask = 0;
		for (int channel : this.channels) {
			if (app_name != null) {
				// Get global channel index from application channel
				int[] hw_type = new int[1];
				int[] hw_index = new int[1];
				int[] hw_channel = new int[1];
				check_status(Jvxlapi.xlGetApplConfig(this.app_name, channel, hw_type, hw_index, hw_channel,
						Jvxlapi.XL_BUS_TYPE_CAN), "xlGetApplConfig");
				logger.info("Channel index " + channel + " found.");
				idx = Jvxlapi.xlGetChannelIndex(hw_type[0], hw_index[0], hw_channel[0]);
				if (idx < 0) {
					/*
					 * Undocumented behavior! See issue #353. If hardware is unavailable, this
					 * function returns -1. Raise an exception as if the driver would have signalled
					 * XL_ERR_HW_NOT_PRESENT.
					 */
					throw new VectorError("XL_ERR_HW_NOT_PRESENT, xlGetChannelIndex");
				}
			} else {
				// Channel already given as global channel
				idx = channel;
			}
			mask = 1 << idx;
			this.channel_masks.put(channel, mask);
			this.index_to_channel.put(idx, channel);
			this.mask |= mask;
		}

		long[] permission_mask = new long[1];
		// Set mask to request channel init permission if needed
		if (bitrate != null || fd) {
			permission_mask[0] = this.mask;
		}
		if (fd) {
			check_status(Jvxlapi.xlOpenPort(this.port_handle, this.app_name, this.mask, permission_mask, rx_queue_size,
					Jvxlapi.XL_INTERFACE_VERSION_V4, Jvxlapi.XL_BUS_TYPE_CAN), "xlOpenPort");
		} else {
			logger.info("permission_mask=" + permission_mask[0]);
			check_status(Jvxlapi.xlOpenPort(this.port_handle, this.app_name, this.mask, permission_mask, rx_queue_size,
					Jvxlapi.XL_INTERFACE_VERSION, Jvxlapi.XL_BUS_TYPE_CAN), "xlOpenPort");
		}
		logger.info(String.format("Open Port: PortHandle: %d, PermissionMask: 0x%X", this.port_handle[0],
				permission_mask[0]));

		if (permission_mask[0] == this.mask) {
			if (fd) {
				this.canFdConf = new XLcanFdConf();
				if (bitrate != null) {
					this.canFdConf.arbitrationBitRate = bitrate;
				} else {
					this.canFdConf.arbitrationBitRate = 500000;
				}
				this.canFdConf.sjwAbr = sjwAbr;
				this.canFdConf.tseg1Abr = tseg1Abr;
				this.canFdConf.tseg2Abr = tseg2Abr;
				if (data_bitrate != null) {
					this.canFdConf.dataBitRate = data_bitrate;
				} else {
					this.canFdConf.dataBitRate = this.canFdConf.arbitrationBitRate;
				}
				this.canFdConf.sjwDbr = sjwDbr;
				this.canFdConf.tseg1Dbr = tseg1Dbr;
				this.canFdConf.tseg2Dbr = tseg2Dbr;

				check_status(Jvxlapi.xlCanFdSetConfiguration(this.port_handle[0], this.mask, this.canFdConf),
						"xlCanFdSetConfiguration");
				logger.info(String.format("SetFdConfig.: ABaudr.=%u, DBaudr.=%u", this.canFdConf.arbitrationBitRate,
						this.canFdConf.dataBitRate));
				logger.info(String.format("SetFdConfig.: sjwAbr=%u, tseg1Abr=%u, tseg2Abr=%u", this.canFdConf.sjwAbr,
						this.canFdConf.tseg1Abr, this.canFdConf.tseg2Abr));
				logger.info(String.format("SetFdConfig.: sjwDbr=%u, tseg1Dbr=%u, tseg2Dbr=%u", this.canFdConf.sjwDbr,
						this.canFdConf.tseg1Dbr, this.canFdConf.tseg2Dbr));
			} else {
				if (bitrate != null) {
					check_status(Jvxlapi.xlCanSetChannelBitrate(this.port_handle[0], permission_mask[0], bitrate),
							"xlCanSetChannelBitrate");
				}
			}
		} else {
			logger.info("No init access");
		}

		// Enable/disable TX receipts
		int tx_receipts = 0;
		if (receive_own_messages)
			tx_receipts = 1;
		check_status(Jvxlapi.xlCanSetChannelMode(this.port_handle[0], this.mask, tx_receipts, 0),
				"xlCanSetChannelMode");

		this.event_handle = new long[1];
		check_status(Jvxlapi.xlSetNotification(this.port_handle[0], this.event_handle, 1), "xlSetNotification");
		logger.info(String.format("event handler= 0x%X", this.event_handle[0]));

		// set filters just before activate the channel
		this.set_filters(can_filters);

		try {
			check_status(Jvxlapi.xlActivateChannel(this.port_handle[0], this.mask, Jvxlapi.XL_BUS_TYPE_CAN, 0),
					"xlActivateChannel");
		} catch (VectorError e) {
			this.shutdown();
			throw e;
		}

		// Calculate time offset for absolute timestamps
		long[] offset = new long[1];
		Jvxlapi.xlGetSyncTime(this.port_handle[0], offset);
		this.time_offset = System.currentTimeMillis() - offset[0] * 1000;

		this.is_filtered = false;
	}

	@Override
	public void _apply_filters(List<Filter> filters) {
		if (filters != null) {
			if (filters.size() == 1 || (filters.size() == 2
					&& filters.get(0).extended.booleanValue() != filters.get(1).extended.booleanValue())) {
				try {
					for (Filter can_filter : filters) {
						int idrange = Jvxlapi.XL_CAN_STD;
						if (can_filter.extended != null)
							idrange = Jvxlapi.XL_CAN_EXT;
						check_status(Jvxlapi.xlCanSetChannelAcceptance(this.port_handle[0], this.mask,
								can_filter.can_id, can_filter.can_mask, idrange), "xlCanSetChannelAcceptance");
					}
				} catch (VectorError e) {
					logger.warn("Could not set filters: " + e.getMessage());
				}
				this.is_filtered = true;
				return;
			}
		}

		// fallback: reset filters
		this.is_filtered = false;
		try {
			int res = Jvxlapi.xlCanSetChannelAcceptance(this.port_handle[0], this.mask, 0, 0, Jvxlapi.XL_CAN_EXT);
			check_status(res, "xlCanSetChannelAcceptance");
			res = Jvxlapi.xlCanSetChannelAcceptance(this.port_handle[0], this.mask, 0, 0, Jvxlapi.XL_CAN_STD);
			check_status(res, "xlCanSetChannelAcceptance");
		} catch (VectorError e) {
			logger.warn("Could not reset filters: " + e.getMessage());
		}
	}

	@Override
	public Map<String, Object> _recv_internal(Long timeout) throws Exception {
		Long end_time = null;
		if (timeout != null)
			end_time = System.currentTimeMillis() + timeout;

		XLcanRxEvent rxEvent = new XLcanRxEvent();
		while (true) {
			if (this.fd) {
				try {
					check_status(Jvxlapi.xlCanReceive(this.port_handle[0], rxEvent), "xlCanReceive");
				} catch (VectorError e) {
					if (e.error_code != Jvxlapi.XL_ERR_QUEUE_IS_EMPTY) {
						throw e;
					}
				}
				if (rxEvent.tag == Jvxlapi.XL_CAN_EV_TAG_RX_OK || rxEvent.tag == Jvxlapi.XL_CAN_EV_TAG_TX_OK) {
					long msg_id = rxEvent.canRxOkMsg.canId;
					int dlc = Util.dlc2len(rxEvent.canRxOkMsg.dlc);
					int flags = rxEvent.canRxOkMsg.msgFlags;
					long timestamp = rxEvent.timeStampSync * 1000 + this.time_offset;
					int channel = this.index_to_channel.get((rxEvent.channelIndex));
					boolean is_extended_id = ((msg_id & Jvxlapi.XL_CAN_EXT_MSG_ID) != 0) ? true : false;
					boolean is_remote_frame = ((flags & Jvxlapi.XL_CAN_RXMSG_FLAG_RTR) != 0) ? true : false;
					boolean is_error_frame = ((flags & Jvxlapi.XL_CAN_RXMSG_FLAG_EF) != 0) ? true : false;
					boolean is_fd = ((flags & Jvxlapi.XL_CAN_RXMSG_FLAG_EDL) != 0) ? true : false;
					boolean error_state_indicator = ((flags & Jvxlapi.XL_CAN_RXMSG_FLAG_ESI) != 0) ? true : false;
					Message msg = new Message(timestamp, msg_id, is_extended_id, is_remote_frame, is_error_frame,
							channel, dlc, Arrays.copyOfRange(rxEvent.canRxOkMsg.data, 0, dlc), is_fd, null,
							error_state_indicator, null);
					Map<String, Object> ret = new HashMap<>();
					ret.put("Message", msg);
					ret.put("already_filtered", this.is_filtered);
					return ret;
				}

			} else {
				XLevent[] event = new XLevent[1];
				int[] event_count = { 1 };
				try {
					check_status(Jvxlapi.xlReceive(this.port_handle[0], event_count, (XLevent[]) event), "xlReceive");
				} catch (VectorError e) {
					if (e.error_code != Jvxlapi.XL_ERR_QUEUE_IS_EMPTY) {
//						throw e;
					}
				}

				if (event[0] != null) {
					if (event[0].tag == Jvxlapi.XL_RECEIVE_MSG) {
						long msg_id = event[0].tagData.id & 0x1FFFFFFF;
						int dlc = event[0].tagData.dlc;
						int flags = event[0].tagData.flags;
						long timestamp = event[0].timeStamp * 1000 + this.time_offset;
						int channel = this.index_to_channel.get((event[0].chanIndex));
						boolean is_extended_id = ((msg_id & Jvxlapi.XL_CAN_EXT_MSG_ID) != 0) ? true : false;
						boolean is_remote_frame = ((flags & Jvxlapi.XL_CAN_MSG_FLAG_REMOTE_FRAME) != 0) ? true : false;
						boolean is_error_frame = ((flags & Jvxlapi.XL_CAN_MSG_FLAG_ERROR_FRAME) != 0) ? true : false;
						Message msg = new Message(timestamp, msg_id, is_extended_id, is_remote_frame, is_error_frame,
								channel, dlc, Arrays.copyOfRange(event[0].tagData.data, 0, dlc), false, null, null,
								null);
						Map<String, Object> ret = new HashMap<>();
						ret.put("Message", msg);
						ret.put("already_filtered", this.is_filtered);
						return ret;
					}
				}
			}

			if (end_time != null) {
				if (System.currentTimeMillis() > end_time) {
					Map<String, Object> ret = new HashMap<>();
					ret.put("Message", null);
					ret.put("already_filtered", this.is_filtered);
					return ret;
				}
			}

			// wait for receive event to occur
			long time_left_ms = 0;
			if (timeout == null) {
				time_left_ms = 0xFFFFFFFF;// INFINITE timeout
			} else {
				time_left_ms = end_time - System.currentTimeMillis();
			}
			logger.debug("wait for " + time_left_ms + "ms.");
			Jvxlapi.WaitForSingleObject(this.port_handle[0], time_left_ms);
		}

	}

	@Override
	public void send(Message msg, Long timeout) {
		long msg_id = msg.arbitration_id;
		if (msg.is_extended_id) {
			msg_id |= Jvxlapi.XL_CAN_EXT_MSG_ID;
		}
		int flags = 0;
		// If channel has been specified, try to send only to that one.
		// Otherwise send to all channels
		long mask = this.mask;
		if (this.channel_masks.containsKey(msg.channel))
			mask = this.channel_masks.get(msg.channel);

		if (this.fd) {
			if (msg.is_fd) {
				flags |= Jvxlapi.XL_CAN_TXMSG_FLAG_EDL;
			}
			if (msg.bitrate_switch) {
				flags |= Jvxlapi.XL_CAN_TXMSG_FLAG_BRS;
			}
			if (msg.is_remote_frame) {
				flags |= Jvxlapi.XL_CAN_TXMSG_FLAG_RTR;
			}
			int message_count = 1;
			int[] MsgCntSent = { 1 };
			XLcanTxEvent[] xl_events = new XLcanTxEvent[1];
			XLcanTxEvent xLcanTxEvent = new XLcanTxEvent();
			xLcanTxEvent.tag = Jvxlapi.XL_CAN_EV_TAG_TX_MSG;
			xLcanTxEvent.transId = 0xffff;
			xLcanTxEvent.tagData.id = msg_id;
			xLcanTxEvent.tagData.flags = flags;
			xLcanTxEvent.tagData.dlc = Util.len2dlc(msg.dlc);

			for (int i = 0; i < msg.data.length; i++) {
				xLcanTxEvent.tagData.data[i] = msg.data[i];
			}
			xl_events[0] = xLcanTxEvent;
			try {
				check_status(Jvxlapi.xlCanTransmitEx(this.port_handle[0], mask, message_count, MsgCntSent, xl_events),
						"xlCanTransmitEx");
			} catch (VectorError e) {
				logger.warn("Error happened when sending message. " + e.getMessage());
			}
		} else {
			if (msg.is_remote_frame) {
				flags |= Jvxlapi.XL_CAN_MSG_FLAG_REMOTE_FRAME;
			}
			int[] message_count = { 1 };
			XLevent[] xl_events = new XLevent[1];
			XLevent xlevent = new XLevent();
			xlevent.tag = Jvxlapi.XL_TRANSMIT_MSG;
			xlevent.tagData.id = msg_id;
			xlevent.tagData.dlc = msg.dlc;
			xlevent.tagData.flags = flags;
			for (int i = 0; i < msg.data.length; i++) {
				xlevent.tagData.data[i] = msg.data[i];
			}
			xl_events[0] = xlevent;
			try {
				check_status(Jvxlapi.xlCanTransmit(this.port_handle[0], mask, message_count, xl_events),
						"xlCanTransmit");
			} catch (VectorError e) {
				logger.warn("Error happened when sending message. " + e.getMessage());
			}
		}

	}

	@Override
	public void send(Message msg) {
		this.send(msg, null);

	}

	@Override
	public void flush_tx_buffer() {
		try {
			check_status(Jvxlapi.xlCanFlushTransmitQueue(this.port_handle[0], this.mask), "xlCanFlushTransmitQueue");
		} catch (VectorError e) {
			logger.warn(e.getMessage());
		}
	}

	@Override
	public void shutdown() {
		try {
			check_status(Jvxlapi.xlDeactivateChannel(this.port_handle[0], this.mask), "xlDeactivateChannel");
			check_status(Jvxlapi.xlClosePort(this.port_handle[0]), "xlClosePort");
			check_status(Jvxlapi.xlCloseDriver(), "xlCloseDriver");
		} catch (VectorError e) {
			logger.warn(e.getMessage());
		}
	}

	public void reset() {
		try {
			check_status(Jvxlapi.xlDeactivateChannel(this.port_handle[0], this.mask), "xlDeactivateChannel");
			check_status(Jvxlapi.xlActivateChannel(this.port_handle[0], this.mask, Jvxlapi.XL_BUS_TYPE_CAN, 0),
					"xlActivateChannel");
		} catch (VectorError e) {
			logger.warn(e.getMessage());
		}
	}

	void check_status(int result, String funname) throws VectorError {
		logger.debug("[Result=" + result + ",  Calling " + funname + "]");
		if (result > 0) {
			throw new VectorError(result, Jvxlapi.xlGetErrorString(result), funname);
		}
	}

	public static void main(String[] args) {
		try {
			VectorBus bus = new VectorBus(new int[] { 0 }, null, null, false, 125000, null, null, null, false, null,
					null, null, null, null, null, null, null);

			Message msg = new Message(0, 0x3b3, null, false, false, 0, null,
					new byte[] { 0x44, 0, 2, 0x0C, (byte) 0xE6, 0, 0, 0 }, false, null, null, null);
//			bus.send(msg);
//			while (true) {
//				System.out.println("sending msg.");
//				bus.send(msg);
//				Thread.sleep(1000);
//			}

			while (true) {
				Message msg1 = bus.recv(5000L);
				if (msg1 != null) {
					if (msg1.arbitration_id == 0x7A1 || msg1.arbitration_id == 0x7A7) {
						System.out.println(msg1.toString());
					}
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
