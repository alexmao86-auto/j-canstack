package isotp.protocol;

import java.util.Arrays;

import isotp.errors.ValueError;

/**
 * Converts a CAN Message into a meaningful PDU such as SingleFrame, FirstFrame,
 * ConsecutiveFrame, FlowControl
 */
public class PDU {

	class Type {
		public static final int SINGLE_FRAME = 0;
		public static final int FIRST_FRAME = 1;
		public static final int CONSECUTIVE_FRAME = 2;
		public static final int FLOW_CONTROL = 3;
	}

	class FlowStatus {
		public static final int ContinueToSend = 0;
		public static final int Wait = 1;
		public static final int Overflow = 2;
	}

	public int type;
	public int length;
	public byte[] data;
	public int blocksize;
	public int stmin;
	public Long stmin_sec = -1l;
	public int seqnum;
	public int flow_status;

	/**
	 * @param msg The CAN message
	 */
	public PDU(CanMessage msg, int start_of_data, int datalen) throws Exception {

		if (msg == null)
			return;

		if (msg.data.length > start_of_data) {
			int hnb = (msg.data[start_of_data] >> 4) & 0xF;
			if (hnb > 3) {
				throw new ValueError("Received message with unknown frame type " + hnb);
			}
			this.type = hnb;
		} else {
			throw new ValueError("Empty CAN frame");
		}

		switch (this.type) {
		case Type.SINGLE_FRAME:
			this.length = msg.data[start_of_data] & 0xF;
			if (msg.data.length < (this.length + 1)) {
				throw new ValueError("Single Frame length is bigger than CAN frame length");
			}
			if (this.length == 0 || (this.length > (datalen - 1 - start_of_data))) {
				throw new ValueError("Received Single Frame with invalid length of " + this.length);
			}
			this.data = new byte[this.length];
			System.arraycopy(msg.data, 1 + start_of_data, this.data, 0, this.length);
			break;
		case Type.FIRST_FRAME:
			if (msg.data.length < (2 + start_of_data)) {
				throw new ValueError("First frame must be at least  " + this.length + " bytes long");
			}
			int len_h = (msg.data[start_of_data] & 0xF) << 8;
			byte len_l = msg.data[start_of_data + 1];
			this.length = len_h | len_l & 0x0FFF; // TODO: 0xFFF changed to mtu

			if (msg.data.length < datalen) {
				if (msg.data.length < (this.length + 2 + start_of_data)) {
					throw new ValueError(
							"First frame specifies a length that is inconsistent with underlying CAN message DLC");
				}
			}
			int newlen = this.length < (datalen - 2 - start_of_data) ? this.length : (datalen - 2 - start_of_data);
			this.data = new byte[newlen];
			System.arraycopy(msg.data, 2 + start_of_data, this.data, 0, newlen);

			break;
		case Type.CONSECUTIVE_FRAME:
			this.seqnum = msg.data[start_of_data] & 0xF;
			int leftlen = msg.data.length - 1;
			this.length = leftlen < datalen ? leftlen : datalen;
			this.data = new byte[this.length];

			System.arraycopy(msg.data, 1 + start_of_data, this.data, 0, this.length - start_of_data);
			break;
		case Type.FLOW_CONTROL:
			if (msg.data.length < (3 + start_of_data)) {
				throw new Exception("Flow Control frame must be at least 3 bytes");
			}
			this.flow_status = msg.data[start_of_data] & 0xF;
			if (this.flow_status >= 3) {
				throw new Exception("Unknown flow status");
			}
			this.blocksize = msg.data[1 + start_of_data];
			int stmin_temp = msg.data[2 + start_of_data];

			if (stmin_temp >= 0 && stmin_temp <= 0x7F) {
				this.stmin_sec = (long) stmin_temp;
			} else if (stmin_temp >= 0xF1 && stmin_temp <= 0xF9) {
				this.stmin_sec = (long) (stmin_temp - 0xF0);
			}

			if (this.stmin_sec < 0) {
				throw new ValueError("Invalid StMin received in Flow Control");
			}
			break;
		default:
			break;
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PDU [type=");
		builder.append(type);
		builder.append(", length=");
		builder.append(length);
		builder.append(", data=");
		builder.append(Arrays.toString(data));
		builder.append(", blocksize=");
		builder.append(blocksize);
		builder.append(", stmin=");
		builder.append(stmin);
		builder.append(", stmin_sec=");
		builder.append(stmin_sec);
		builder.append(", seqnum=");
		builder.append(seqnum);
		builder.append(", flow_status=");
		builder.append(flow_status);
		builder.append("]");
		return builder.toString();
	}

	public static byte[] craft_flow_control_data(int flow_status, int blocksize, int stmin) {
		byte[] result = new byte[3];
		result[0] = (byte) (0x30 | flow_status);
		result[1] = (byte) (blocksize & 0xFF);
		result[2] = (byte) (stmin & 0xFF);
		return result;
	}

	public String name() {
		switch (this.type) {
		case Type.SINGLE_FRAME:
			return "SINGLE_FRAME";
		case Type.FIRST_FRAME:
			return "FIRST_FRAME";
		case Type.CONSECUTIVE_FRAME:
			return "CONSECUTIVE_FRAME";
		case Type.FLOW_CONTROL:
			return "FLOW_CONTROL";
		default:
			return "Reserved";
		}
	}
}
