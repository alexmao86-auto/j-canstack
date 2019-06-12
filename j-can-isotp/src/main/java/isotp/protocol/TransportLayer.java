package isotp.protocol;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isotp.address.Address;
import isotp.address.TargetAddressType;
import isotp.errors.ConsecutiveFrameTimeoutError;
import isotp.errors.Error_handler;
import isotp.errors.FlowControlTimeoutError;
import isotp.errors.InvalidCanDataError;
import isotp.errors.IsoTpError;
import isotp.errors.MaximumWaitFrameReachedError;
import isotp.errors.ReceptionInterruptedWithFirstFrameError;
import isotp.errors.ReceptionInterruptedWithSingleFrameError;
import isotp.errors.UnexpectedConsecutiveFrameError;
import isotp.errors.UnexpectedFlowControlError;
import isotp.errors.UnsuportedWaitFrameError;
import isotp.errors.WrongSequenceNumberError;

/** The IsoTP transport layer implementation */
public class TransportLayer {
	Logger logger = LoggerFactory.getLogger(TransportLayer.class);

	class Timer {
		Long timeout;
		Long start_time;

		public void setTimeout(Long timeout) {
			this.timeout = timeout;
		}

		public Timer(Long timeout) {
			this.setTimeout(timeout);
			this.start_time = null;
		}

		public void start(Long timeout) {
			if (timeout != null)
				this.setTimeout(timeout);
			this.start_time = System.currentTimeMillis();
		}

		public void start() {
			this.start(null);
		}

		public void stop() {
			this.start_time = null;
		}

		public long elapsed() {
			if (this.start_time != null) {
				return System.currentTimeMillis() - this.start_time;
			} else {
				return 0;
			}
		}

		public boolean is_timed_out() {
			if (this.is_stopped()) {
				return false;
			} else {
				return (this.elapsed() >= this.timeout) || (this.timeout == 0);
			}
		}

		private boolean is_stopped() {
			return (this.start_time == null);
		}

	}

	class RxState {
		public static final int IDLE = 0;
		public static final int WAIT_CF = 1;
	}

	class TxState {
		public static final int IDLE = 0;
		public static final int WAIT_FC = 1;
		public static final int TRANSMIT_CF = 2;
	}

	// fields of TransportLayer
	public Params params;
	public RxFunction rxfn;
	public TxFunction txfn;
	public Integer remote_blocksize;
	public Address address;
	public BlockingQueue<Map<String, Object>> tx_queue;
	public BlockingQueue<byte[]> rx_queue;
	public int rx_state;
	public int tx_state;
	public int rx_block_counter;
	public Integer last_seqnum;
	public int rx_frame_length;
	public int tx_frame_length;
	public PDU last_flow_control_frame;
	public int tx_block_counter;
	public int tx_seqnum;
	public int wft_counter;
	public boolean pending_flow_control_tx;
	public List<Byte> tx_buffer = new ArrayList<Byte>();
	public List<Byte> rx_buffer = new ArrayList<Byte>();
	public Timer timer_tx_stmin;
	public Timer timer_rx_fc;
	public Timer timer_rx_cf;

	public Error_handler error_handler;

	public TransportLayer() {
		this(null, null, null, null, null);
	}

	public TransportLayer(Address address) {
		this(null, null, address, null, null);
	}

	public TransportLayer(RxFunction rxfn, TxFunction txfn, Address address, Error_handler error_handler,
			Params params) {
		this.params = new Params(params);
		this.remote_blocksize = null; // Block size received in Flow Control message

		this.rxfn = rxfn;
		this.txfn = txfn;

		this.set_address(address);

		this.tx_queue = new ArrayBlockingQueue<Map<String, Object>>(65535); // Layer Input queue for IsoTP frame
		this.rx_queue = new ArrayBlockingQueue<byte[]>(65535); // Layer Output queue for IsoTP frame

		this.rx_state = RxState.IDLE; // State of the reception FSM
		this.tx_state = TxState.IDLE; // State of the transmission FSM

		this.rx_block_counter = 0;
		this.last_seqnum = null; // Consecutive frame Sequence number of previous message
		this.rx_frame_length = 0; // Length of IsoTP frame being received at the moment
		this.tx_frame_length = 0; // Length of the data that we are sending
		this.last_flow_control_frame = null;// When a FlowControl is received. Put here
		this.tx_block_counter = 0; // Keeps track of how many block we've sent
		this.tx_seqnum = 0; // Keeps track of the actual sequence number whil sending
		this.wft_counter = 0; // Keeps track of how many wait frame we've receiveds

		this.pending_flow_control_tx = false; // Flag indicating that we need to transmist a flow control message. Set
												// by Rx Process, Cleared by Tx Process
		this.empty_rx_buffer();
		this.empty_tx_buffer();

		this.timer_tx_stmin = new Timer(0l);
		this.timer_rx_fc = new Timer(this.params.rx_flowcontrol_timeout);
		this.timer_rx_cf = new Timer(this.params.rx_consecutive_frame_timeout);

		this.error_handler = error_handler;
	}

	/**
	 * Enqueue an IsoTP frame to be sent over CAN network
	 * 
	 * @throws Exception
	 */
	public void send(byte[] data, int target_address_type) throws Exception {
		if (target_address_type == TargetAddressType.Functional) {
			if (data.length > (7 - this.address.tx_payload_prefix.length)) {
				throw new IsoTpError("Cannot send multipacket frame with Functional TargetAddressType");
			}
		}
		Map<String, Object> e = new HashMap<>();
		e.put("data", data);
		e.put("target_address_type", target_address_type);
		this.tx_queue.add(e);
	}

	public void send(byte[] data) throws Exception {
		this.send(data, TargetAddressType.Physical);
	}

	/**
	 * Receive an IsoTP frame. Output of the layer Dequeue an IsoTP frame from the
	 * reception queue if available.
	 * 
	 * @return The next available IsoTP frame, byte[] or null
	 */
	public byte[] recv() {
//		if(!this.rx_queue.isEmpty()) {
		return (byte[]) this.rx_queue.poll();
//		}
	}

	/**
	 * Returns ``True`` if an IsoTP frame is awaiting in the reception ``queue``.
	 * False otherwise
	 */
	public boolean available() {
		return !this.rx_queue.isEmpty();
	}

	public boolean transmitting() {
		return ((!this.tx_queue.isEmpty()) || (this.tx_state != TxState.IDLE));
	}

	/**
	 * Function to be called periodically, as fast as possible. This function is
	 * non-blocking.
	 * 
	 * @throws Exception
	 */
	public void process() throws Exception {
		CanMessage msg = new CanMessage(0, 8, null, false);
		while (msg != null) {
			msg = this.rxfn.recv();
			if (msg != null) {
				this.logger
						.debug(String.format("Receiving : <%03X> %s", msg.arbitration_id, Arrays.toString(msg.data)));
				this.process_rx(msg);
			}
		}

		msg = new CanMessage(0, 8, null, false);
		while (msg != null) {
			msg = this.process_tx();
			if (msg != null) {
				this.logger.debug(String.format("Sending : <%03X> %s", msg.arbitration_id, Arrays.toString(msg.data)));
				this.txfn.send(msg);
			}
		}
	}

	/**
	 * process_rx
	 * 
	 * @throws Exception
	 */
	private void process_rx(CanMessage msg) throws Exception {
		if (!this.address.is_for_me.check(msg))
			return;
		// Decoding of message into PDU
		PDU pdu = null;
		try {
			pdu = new PDU(msg, this.address.rx_prefix_size, this.params.ll_data_length);
		} catch (Exception e) {
			this.trigger_error(new InvalidCanDataError("Received invalid CAN frame." + e));
			this.stop_receiving();
			return;
		}

		// Check timeout first
		if (this.timer_rx_cf.is_timed_out()) {
			this.trigger_error(new ConsecutiveFrameTimeoutError("Reception of CONSECUTIVE_FRAME timed out."));
			this.stop_receiving();
		}

		// Process Flow Control message
		if (pdu.type == PDU.Type.FLOW_CONTROL) {
			this.last_flow_control_frame = pdu;// Given to process_tx method. Queue of 1 message depth

			if (this.rx_state == RxState.WAIT_CF) {
				if (pdu.flow_status == PDU.FlowStatus.Wait || pdu.flow_status == PDU.FlowStatus.ContinueToSend) {
					this.start_rx_cf_timer();
				}
			} else {
				// Nothing else to be done with FlowControl. Return and wait for next message
				return;
			}
		}

		// Process the state machine
		if (this.rx_state == RxState.IDLE) {
			this.rx_frame_length = 0;
			this.timer_rx_cf.stop();

			if (pdu.type == PDU.Type.SINGLE_FRAME) {
				try {
					this.rx_queue.put(pdu.data);
				} catch (InterruptedException e) {
				}
			} else if (pdu.type == PDU.Type.FIRST_FRAME) {
				this.start_reception_after_first_frame(pdu);
			} else if (pdu.type == PDU.Type.CONSECUTIVE_FRAME) {
				this.trigger_error(new UnexpectedConsecutiveFrameError(
						"Received a ConsecutiveFrame while reception was idle. Ignoring"));
			}

		} else if (this.rx_state == RxState.WAIT_CF) {
			if (pdu.type == PDU.Type.SINGLE_FRAME) {
				try {
					this.rx_queue.put(pdu.data);
				} catch (InterruptedException e) {
				}
				this.rx_state = RxState.IDLE;
				this.trigger_error(new ReceptionInterruptedWithSingleFrameError(
						"Reception of IsoTP frame interrupted with a new FirstFrame."));
			} else if (pdu.type == PDU.Type.FIRST_FRAME) {
				this.start_reception_after_first_frame(pdu);
				this.trigger_error(new ReceptionInterruptedWithFirstFrameError(
						"Reception of IsoTP frame interrupted with a new FirstFrame."));
			} else if (pdu.type == PDU.Type.CONSECUTIVE_FRAME) {

				this.start_rx_cf_timer();// Received a CF message. Restart counter. Timeout handled above.

				int expected_seqnum = (this.last_seqnum + 1) & 0xF;
				if (pdu.seqnum == expected_seqnum) {
					this.last_seqnum = expected_seqnum;

					int bytes_to_receive = this.rx_frame_length - this.rx_buffer.size();

					byte[] pdudata = Arrays.copyOfRange(pdu.data, 0,
							pdu.data.length < bytes_to_receive ? pdu.data.length : bytes_to_receive);
					this.append_rx_data(pdudata);
					if (this.rx_buffer.size() >= this.rx_frame_length) {
						try {
							byte[] rb = new byte[this.rx_buffer.size()];
							for (int i = 0; i < this.rx_buffer.size(); i++) {
								rb[i] = this.rx_buffer.get(i);
							}
							this.rx_queue.put(rb);// Data complete
						} catch (InterruptedException e) {
						}
						this.stop_receiving();// Go back to IDLE. Reset all variables and timers.
					} else {
						this.rx_block_counter++;
						if ((this.rx_block_counter % this.params.blocksize) == 0) {
							this.request_tx_flowcontrol(); // Sets a flag to 1. process_tx will send it for use.
							this.timer_rx_cf.stop();// Deactivate that timer while we wait for flow control
						}
					}
				} else {
					this.stop_receiving();
					this.trigger_error(new WrongSequenceNumberError(String.format(
							"Received a ConsecutiveFrame with wrong SequenceNumber. Expecting 0x%X, Received 0x%X",
							expected_seqnum, pdu.seqnum)));
				}

			}
		}

	}

	private void append_rx_data(byte[] data) {
		for (int i = 0; i < data.length; i++) {
			this.rx_buffer.add(data[i]);
		}
	}

	private void request_tx_flowcontrol() {
		this.pending_flow_control_tx = true;
	}

	private void start_reception_after_first_frame(PDU frame) {
		this.last_seqnum = 0;
		this.rx_block_counter = 0;
		this.empty_rx_buffer();
		this.rx_frame_length = frame.length;
		this.rx_state = RxState.WAIT_CF;
		this.append_rx_data(frame.data);
		this.request_tx_flowcontrol();
		this.start_rx_cf_timer();
	}

	private void start_rx_cf_timer() {
		this.timer_rx_cf = new Timer(this.params.rx_consecutive_frame_timeout);
		this.timer_rx_cf.start();
	}

	private void stop_receiving() {
		this.rx_state = RxState.IDLE;
		this.empty_rx_buffer();
		this.stop_sending_flow_control();
		this.timer_rx_cf.stop();
	}

	private void stop_sending_flow_control() {
		this.pending_flow_control_tx = false;
		this.last_flow_control_frame = null;
	}

	private void trigger_error(IsoTpError e) {
		if (this.error_handler != null) {
			this.error_handler.handle(e);
		}
		logger.warn(e.getMessage());

	}

	private CanMessage process_tx() {
		CanMessage output_msg = null; // Value outputed. If None, no subsequent call to process_tx will be done.

		// Sends flow control if process_rx requested it
		if (this.pending_flow_control_tx) {
			this.pending_flow_control_tx = false;
			return this.make_flow_control(PDU.FlowStatus.ContinueToSend);
		}

		// Handle flow control reception
		PDU flow_control_frame = this.last_flow_control_frame;
		this.last_flow_control_frame = null;

		if (flow_control_frame != null) {
			if (flow_control_frame.flow_status == PDU.FlowStatus.Overflow) {
				// Needs to stop sending.
				this.stop_sending();
				return null;
			}

			if (this.tx_state == TxState.IDLE) {
				this.trigger_error(new UnexpectedFlowControlError(
						"Received a FlowControl message while transmission was Idle. Ignoring"));
			} else {
				if (flow_control_frame.flow_status == PDU.FlowStatus.Wait) {
					if (this.params.wftmax == 0) {
						this.trigger_error(new UnsuportedWaitFrameError(
								"Received a FlowControl requesting to wait, but fwtmax is set to 0"));
					} else if (this.wft_counter >= this.params.wftmax) {
						this.stop_sending();
						this.trigger_error(new MaximumWaitFrameReachedError(String.format(
								"Received %d wait frame which is the maximum set in params.wftmax", this.wft_counter)));

					} else {
						this.wft_counter++;
						if (this.tx_state == TxState.WAIT_FC || this.tx_state == TxState.TRANSMIT_CF) {
							this.tx_state = TxState.WAIT_FC;
							this.start_rx_fc_timer();
						}
					}
				} else if (flow_control_frame.flow_status == PDU.FlowStatus.ContinueToSend
						&& !this.timer_rx_fc.is_timed_out()) {
					this.wft_counter = 0;
					this.timer_rx_fc.stop();
					this.timer_tx_stmin.setTimeout(flow_control_frame.stmin_sec);
					this.remote_blocksize = flow_control_frame.blocksize;

					if (this.tx_state == TxState.WAIT_FC) {
						this.tx_block_counter = 0;
						this.timer_tx_stmin.start();
					} else if (this.tx_state == TxState.TRANSMIT_CF) {

					}

					this.tx_state = TxState.TRANSMIT_CF;
				}
			}
		}

		// ======= Timeouts ======
		if (this.timer_rx_fc.is_timed_out()) {
			this.stop_sending();
			this.trigger_error(
					new FlowControlTimeoutError("Reception of FlowControl timed out. Stopping transmission"));
		}

		// ======= FSM ======

		// Check this first as we may have another isotp frame to send and we need to
		// handle it right away without waiting for next "process()" call
		if (this.tx_state != TxState.IDLE && this.tx_buffer.size() == 0) {
			this.stop_sending();
		}

		if (this.tx_state == TxState.IDLE) {
			// Read until we get non-empty frame to send
			boolean read_tx_queue = true;
			while (read_tx_queue) {
				read_tx_queue = false;
				if (!this.tx_queue.isEmpty()) {
					Map<String, Object> popped_object = this.tx_queue.poll();
					byte[] d = (byte[]) popped_object.get("data");
					int tat = (int) popped_object.get("target_address_type");
					if (d.length == 0) {
						// Read another frame from tx_queue
						read_tx_queue = true;
					} else {
						this.tx_buffer.clear();
						for (int i = 0; i < d.length; i++)
							this.tx_buffer.add(d[i]);

						if (this.tx_buffer
								.size() <= (this.params.ll_data_length - 1 - this.address.tx_payload_prefix.length)) {
							// Single frame
							byte[] msg_data = new byte[this.address.tx_payload_prefix.length + 1
									+ this.tx_buffer.size()];
							System.arraycopy(this.address.tx_payload_prefix, 0, msg_data, 0,
									this.address.tx_payload_prefix.length);
							msg_data[this.address.tx_payload_prefix.length] = (byte) this.tx_buffer.size();

							byte[] tb = new byte[this.tx_buffer.size()];
							for (int i = 0; i < this.tx_buffer.size(); i++) {
								tb[i] = this.tx_buffer.get(i);
							}

							System.arraycopy(tb, 0, msg_data, this.address.tx_payload_prefix.length + 1,
									this.tx_buffer.size());

							int arbitration_id = this.address.get_tx_arbitration_id(tat);

							output_msg = this.make_tx_msg(arbitration_id, msg_data);
						} else {
							// Multi frame
							int data_length = this.params.ll_data_length - 2 - this.address.tx_payload_prefix.length;
							this.tx_frame_length = this.tx_buffer.size();
							byte[] msg_data = new byte[this.address.tx_payload_prefix.length + 2 + data_length];
							System.arraycopy(this.address.tx_payload_prefix, 0, msg_data, 0,
									this.address.tx_payload_prefix.length);
							msg_data[this.address.tx_payload_prefix.length] = (byte) (0x10
									| (this.tx_buffer.size() >> 8) & 0xF);
							msg_data[this.address.tx_payload_prefix.length + 1] = (byte) (this.tx_frame_length & 0xff);
							byte[] tb = new byte[this.tx_buffer.size()];
							for (int i = 0; i < this.tx_buffer.size(); i++) {
								tb[i] = this.tx_buffer.get(i);
							}
							System.arraycopy(tb, 0, msg_data, this.address.tx_payload_prefix.length + 2, data_length);

							int arbitration_id = this.address.get_tx_arbitration_id();
							output_msg = this.make_tx_msg(arbitration_id, msg_data);

							this.tx_buffer = this.tx_buffer.subList(data_length, this.tx_buffer.size());

							this.tx_state = TxState.WAIT_FC;
							this.tx_seqnum = 1;
							this.start_rx_fc_timer();
						}
					}
				}
			}

		} else if (this.tx_state == TxState.WAIT_FC) {
			// Nothing to do. Flow control will make the FSM switch state by calling
			// init_tx_consecutive_frame
		} else if (this.tx_state == TxState.TRANSMIT_CF) {
			if (this.timer_tx_stmin.is_timed_out() || this.params.squash_stmin_requirement) {
				int data_length = this.params.ll_data_length - 1 - this.address.tx_payload_prefix.length;
				int msg_length = this.tx_buffer.size() < data_length ? this.tx_buffer.size() : data_length;
				byte[] msg_data = new byte[this.address.tx_payload_prefix.length + 1 + msg_length];
				if (this.address.tx_payload_prefix.length > 0) {
					System.arraycopy(this.address.tx_payload_prefix, 0, msg_data, 0,
							this.address.tx_payload_prefix.length);
				}
				msg_data[this.address.tx_payload_prefix.length] = (byte) (0x20 | this.tx_seqnum);
				byte[] tb = new byte[msg_length];
				for (int i = 0; i < msg_length; i++) {
					tb[i] = this.tx_buffer.get(i);
				}

				System.arraycopy(tb, 0, msg_data, this.address.tx_payload_prefix.length + 1, msg_length);

				int arbitration_id = this.address.get_tx_arbitration_id();
				output_msg = this.make_tx_msg(arbitration_id, msg_data);
				if (this.tx_buffer.size() > data_length) {
					this.tx_buffer = this.tx_buffer.subList(data_length, this.tx_buffer.size());
				} else {
					this.tx_buffer.clear();
				}
				this.tx_seqnum = (this.tx_seqnum + 1) & 0xF;
				this.timer_tx_stmin.start();
				this.tx_block_counter++;
			}

			if ((this.remote_blocksize != 0) && (this.tx_block_counter >= this.remote_blocksize)) {
				this.tx_state = TxState.WAIT_FC;
				this.start_rx_fc_timer();
			}

		}

		return output_msg;
	}

	private CanMessage make_tx_msg(int arbitration_id, byte[] msg_data) {
		msg_data = this.pad_message_data(msg_data);
		return new CanMessage(arbitration_id, msg_data == null ? null : msg_data.length, msg_data,
				this.address.is_29bits);
	}

	private byte[] pad_message_data(byte[] msg_data) {
		if (msg_data == null)
			return msg_data;
		if ((msg_data.length < this.params.ll_data_length) && (this.params.tx_padding != null)) {
			byte[] padded_data = new byte[this.params.ll_data_length];
			System.arraycopy(msg_data, 0, padded_data, 0, msg_data.length);
			for (int i = msg_data.length; i < this.params.ll_data_length; i++) {
				padded_data[i] = (byte) (this.params.tx_padding & 0xFF);
			}
			return padded_data;
		} else
			return msg_data;
	}

	private void start_rx_fc_timer() {
		this.timer_rx_fc = new Timer(this.params.rx_flowcontrol_timeout);
		this.timer_rx_fc.start();
	}

	private void stop_sending() {
		this.empty_tx_buffer();
		this.tx_state = TxState.IDLE;
		this.tx_frame_length = 0;
		this.timer_rx_fc.stop();
		this.timer_tx_stmin.stop();
		this.remote_blocksize = null;
		this.tx_block_counter = 0;
		this.tx_seqnum = 0;
		this.wft_counter = 0;
	}

	private CanMessage make_flow_control(int flow_status, Integer blocksize, Integer stmin) {
		if (blocksize == null) {
			blocksize = this.params.blocksize;
		}
		if (stmin == null) {
			stmin = this.params.stmin;
		}

		byte[] data = PDU.craft_flow_control_data(flow_status, blocksize, stmin);
		byte[] msg_data = null;

		if (address.tx_payload_prefix.length > 0) {
			int newlen = address.tx_payload_prefix.length;
			if (data != null)
				newlen += data.length;

			msg_data = new byte[newlen];
			System.arraycopy(address.tx_payload_prefix, 0, msg_data, 0, address.tx_payload_prefix.length);
			System.arraycopy(data, 0, msg_data, address.tx_payload_prefix.length, data.length);

		} else {
			msg_data = data;
		}

		return this.make_tx_msg(this.address.get_tx_arbitration_id(), msg_data);

	}

	private CanMessage make_flow_control(int flow_status) {
		return make_flow_control(flow_status, null, null);
	}

	private void empty_tx_buffer() {
		this.tx_buffer.clear();
	}

	private void empty_rx_buffer() {
		this.rx_buffer.clear();
	}

	/**
	 * Sets the layer :class:`Address<isotp.Address>`. Can be set after
	 * Initialisation if needed.
	 */
	private void set_address(Address address) {
		this.address = address;

		if (address == null)
			return;
		if (this.address.txid != null) {
			if (((this.address.txid > 0x7F4 && this.address.txid < 0x7F6)
					|| (this.address.txid > 0x7FA && this.address.txid < 0x7FB))) {
				this.logger
						.warn("Used txid overlaps the range of ID reserved by ISO-15765 (0x7F4-0x7F6 and 0x7FA-0x7FB)");
			}
		}

		if (this.address.rxid != null) {
			if (((this.address.rxid > 0x7F4 && this.address.rxid < 0x7F6)
					|| (this.address.rxid > 0x7FA && this.address.rxid < 0x7FB))) {
				this.logger
						.warn("Used rxid overlaps the range of ID reserved by ISO-15765 (0x7F4-0x7F6 and 0x7FA-0x7FB)");
			}
		}
	}

	/**
	 * Reset the layer: Empty all buffers, set the internal state machines to Idle
	 */
	public void reset() {
		this.stop_sending();
		this.stop_receiving();
		this.tx_queue.clear();
		this.rx_queue.clear();
	}

	// Gives a time to pass to time.sleep() based on the state of the FSM. Avoid
	// using too much CPU
	/**
	 * Returns a value in seconds that can be passed to ``time.sleep()`` when the
	 * stack is processed in a different thread.
	 * 
	 * The value will change according to the internal state machine state, sleeping
	 * longer while idle and shorter when active.
	 */
	public long sleep_time() {
		if (this.rx_state == RxState.IDLE && this.tx_state == TxState.IDLE)
			return 50;
		if (this.rx_state == RxState.IDLE && this.tx_state == TxState.WAIT_FC)
			return 10;
		return 1;
	}
}
