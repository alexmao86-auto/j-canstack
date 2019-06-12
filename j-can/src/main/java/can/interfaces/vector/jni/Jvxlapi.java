package can.interfaces.vector.jni;

import java.util.ArrayList;
import java.util.List;

public class Jvxlapi {
	static {
		System.loadLibrary("Jvxlapi64");
	}

	public static final int XL_BUS_TYPE_CAN = 0x00000001;
	public static final int XL_INTERFACE_VERSION = 3;
	public static final int XL_INTERFACE_VERSION_V4 = 4;
	public static final int XL_CAN_STD = 1;
	public static final int XL_CAN_EXT = 2;
	public static final int XL_ERR_QUEUE_IS_EMPTY = 10;
	public static final int XL_RECEIVE_MSG = 1;
	public static final int XL_CAN_EV_TAG_RX_OK = 1024;
	public static final int XL_CAN_EV_TAG_TX_OK = 1028;
	public static final int XL_TRANSMIT_MSG = 10;
	public static final int XL_CAN_EV_TAG_TX_MSG = 1088;
	public static final int XL_CAN_EXT_MSG_ID = 0x80000000;
	public static final int XL_CAN_MSG_FLAG_ERROR_FRAME = 0x01;
	public static final int XL_CAN_MSG_FLAG_REMOTE_FRAME = 0x10;
	public static final int XL_CAN_MSG_FLAG_TX_COMPLETED = 0x40;

	public static final int XL_CAN_TXMSG_FLAG_EDL = 0x0001;
	public static final int XL_CAN_TXMSG_FLAG_BRS = 0x0002;
	public static final int XL_CAN_TXMSG_FLAG_RTR = 0x0010;
	public static final int XL_CAN_RXMSG_FLAG_EDL = 0x0001;
	public static final int XL_CAN_RXMSG_FLAG_BRS = 0x0002;
	public static final int XL_CAN_RXMSG_FLAG_ESI = 0x0004;
	public static final int XL_CAN_RXMSG_FLAG_RTR = 0x0010;
	public static final int XL_CAN_RXMSG_FLAG_EF = 0x0200;

	public static native int xlOpenDriver();

	public static native int xlCloseDriver();

	public static native int xlGetDriverConfig(XLdriverConfig pDriverConfig);

	public static native int xlGetApplConfig(String appName, int appChannel, int[] pHwType, int[] pHwIndex,
			int[] pHwChannel, int busType);

	public static native int xlGetChannelIndex(int hwType, int hwIndex, int hwChannel);

	public static native int xlOpenPort(long[] portHandle, String userName, long accessMask, long[] permissionMask,
			int rxQueueSize, int xlInterfaceVersion, int busType);

	public static native int xlGetSyncTime(long portHandle, long[] time);

	public static native int xlClosePort(long portHandle);

	public static native int xlSetNotification(long portHandle, long[] handle, int queueLevel);

	public static native int xlCanSetChannelMode(long portHandle, long accessMask, int tx, int txrq);

	public static native int xlActivateChannel(long portHandle, long accessMask, int busType, int flags);

	public static native int xlDeactivateChannel(long portHandle, long accessMask);

	public static native int xlCanFdSetConfiguration(long portHandle, long accessMask, XLcanFdConf pCanFdConf);

	public static native int xlReceive(long portHandle, int[] pEventCount, XLevent[] pEventList);

	public static native int xlCanReceive(long portHandle, XLcanRxEvent pXlCanRxEvt);

	public static native String xlGetErrorString(int err);

	public static native int xlCanSetChannelBitrate(long portHandle, long accessMask, long bitrate);

	public static native int xlCanTransmit(long portHandle, long accessMask, int[] messageCount, XLevent[] pMessages);

	public static native int xlCanTransmitEx(long portHandle, long accessMask, int msgCnt, int[] pMsgCntSent,
			XLcanTxEvent[] pXlCanTxEvt);

	public static native int xlCanFlushTransmitQueue(long portHandle, long accessMask);

	public static native int xlCanSetChannelAcceptance(long portHandle, long accessMask, long code, long mask,
			int idRange);

	public static native int xlCanResetAcceptance(long portHandle, long accessMask, int idRange);

	public static native int WaitForSingleObject(long handle, long time_left_ms);

	public Jvxlapi() {
	}

	public static List<XLchannelConfig> get_channel_configs() {
		XLdriverConfig pDriverConfig = new XLdriverConfig();
		xlOpenDriver();
		xlGetDriverConfig(pDriverConfig);
		xlCloseDriver();

		List<XLchannelConfig> channels = new ArrayList<XLchannelConfig>();
		for (int i = 0; i < pDriverConfig.channelCount; i++) {
			channels.add(pDriverConfig.channel[i]);
		}

		return channels;
	}

	public static void main(String[] args) {
		System.out.println("reading the CAN config.");
//		Jvxlapi jv = new Jvxlapi();
		List<XLchannelConfig> channels = get_channel_configs();
		for (XLchannelConfig ch : channels) {
			System.out
					.println("hwType=" + ch.hwType + "hwChannel=" + ch.hwChannel + "channelindex=" + ch.channelIndex);
		}

		int[] hw_type = { -1 };
		int[] hw_index = { -1 };
		int[] hw_channel = { -1 };
		int ree = xlGetApplConfig("CANalyzer", 0, hw_type, hw_index, hw_channel, Jvxlapi.XL_BUS_TYPE_CAN);
		System.out.println(ree + ":CANalyzer: hw_type=" + hw_type[0] + ", hw_index=" + hw_index[0] + ", hw_channel="
				+ hw_channel[0]);
	}
}
