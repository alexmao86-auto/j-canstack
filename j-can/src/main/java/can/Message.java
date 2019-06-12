package can;

import java.util.Arrays;

public class Message {

	public long timestamp;
	public long arbitration_id;
	public Boolean is_extended_id;
	public boolean is_remote_frame;
	public boolean is_error_frame;
	public int channel;
	public int dlc;
	public byte[] data;
	public boolean is_fd;
	public boolean bitrate_switch;
	public boolean error_state_indicator;

	private float timestamp_delta = 1.0e-6f;

	/**
	 * To create a message object, simply provide any of the below attributes
	 * together with additional parameters as keyword arguments to the constructor.
	 * 
	 * @throws Exception
	 */
	public Message(long timestamp, long arbitration_id, Boolean is_extended_id, Boolean is_remote_frame,
			Boolean is_error_frame, Integer channel, Integer dlc, byte[] data, Boolean is_fd, Boolean bitrate_switch,
			Boolean error_state_indicator, Boolean check) throws Exception {

		if (is_extended_id == null)
			is_extended_id = false;
		if (is_remote_frame == null)
			is_remote_frame = false;
		if (is_error_frame == null)
			is_error_frame = false;
		if (data == null || is_remote_frame) {
			this.data = new byte[8];
		} else {
			this.data = data;
		}
		if (dlc != null) {
			this.dlc = dlc;
		} else {
			this.dlc = this.data.length;
		}
		if (bitrate_switch == null)
			bitrate_switch = false;
		if (error_state_indicator == null)
			error_state_indicator = false;
		if (check == null)
			check = false;
		if (channel == null)
			channel = 0;
		if (is_fd == null)
			is_fd = false;

		this.timestamp = timestamp;
		this.arbitration_id = arbitration_id;
		this.is_remote_frame = is_remote_frame;
		this.is_error_frame = is_error_frame;
		this.channel = channel;
		this.is_extended_id = is_extended_id;
		this.bitrate_switch = bitrate_switch;
		this.error_state_indicator = error_state_indicator;
		this.is_fd = is_fd;

		if (check) {
			this._check();
		}
	}

	/**
	 * Checks if the message parameters are valid. Assumes that the types are
	 * already correct.
	 * 
	 * @throws Exception
	 */
	private void _check() throws Exception {

		if (this.timestamp < 0.0f) {
			throw new Exception("the timestamp may not be negative");
		}
		if (Float.isInfinite(this.timestamp)) {
			throw new Exception("the timestamp may not be infinite");
		}
		if (Float.isNaN(this.timestamp)) {
			throw new Exception("the timestamp may not be NaN");
		}
		if (this.is_remote_frame && this.is_error_frame) {
			throw new Exception("a message cannot be a remote and an error frame at the same time");
		}
		if (this.arbitration_id < 0) {
			throw new Exception("arbitration IDs may not be negative");
		}
		if (this.is_extended_id != null) {
			if (this.arbitration_id >= 0x20000000) {
				throw new Exception("Extended arbitration IDs must be less than 2^29");
			}
		} else if (this.arbitration_id >= 0x800) {
			throw new Exception("Normal arbitration IDs must be less than 2^11");
		}
		if (this.dlc < 0) {
			throw new Exception("DLC may not be negative");
		}
		if (this.is_fd) {
			if (this.dlc > 64) {
				throw new Exception("DLC was " + this.dlc + " but it should be <= 64 for CAN FD frames");
			}
		} else {
			if (8 < this.dlc) {
				throw new Exception("DLC was " + this.dlc + " but it should be <= 8 for normal CAN frames");
			}
			if (this.bitrate_switch) {
				throw new Exception("bitrate switch is only allowed for CAN FD frames");
			}
			if (this.error_state_indicator) {
				throw new Exception("error stat indicator is only allowed for CAN FD frames");
			}
		}
		if (this.is_remote_frame) {
			if (this.data != null && this.data.length != 0) {
				throw new Exception("remote frames may not carry any data");
			}
		} else if (this.dlc != this.data.length) {
			throw new Exception("the DLC and the length of the data must match up for non remote frames");
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[timestamp:");
		builder.append(String.format("%015.6f ", timestamp / 1000.0f));
		builder.append("ID=");
//		String arbitration_id_string;
//		if (this.is_extended_id != null) {
//			arbitration_id_string = String.format("0x%x", arbitration_id);
//		} else {
//			arbitration_id_string = String.format("0x%x", arbitration_id);
//		}
		builder.append(String.format("%12s", String.format("0x%x", arbitration_id)));
		if (is_extended_id != null)
			builder.append(" X");
		else
			builder.append(" S");

		if (is_error_frame)
			builder.append("E");
		else
			builder.append(" ");

		if (is_remote_frame)
			builder.append("R");
		else
			builder.append(" ");

		if (is_fd)
			builder.append("F");
		else
			builder.append(" ");

		if (bitrate_switch)
			builder.append("BS");
		else
			builder.append("  ");

		if (error_state_indicator)
			builder.append("EI");
		else
			builder.append("  ");

		builder.append(String.format("DLC: %02d ", dlc));

		if (data != null) {
			String data_string = "";
			for (int i = 0; i < (dlc < data.length ? dlc : data.length); i++) {
				data_string = data_string + String.format(" %02x", data[i]);
			}
			builder.append(String.format("%24s", data_string));
		}

		builder.append(" Channel: " + channel);

		builder.append("]");
		return builder.toString();
	}

	/** return the dlc such that it also works on remote frames */
	public int len() {
		return this.dlc;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (arbitration_id ^ (arbitration_id >>> 32));
		result = prime * result + (bitrate_switch ? 1231 : 1237);
		result = prime * result + channel;
		result = prime * result + Arrays.hashCode(data);
		result = prime * result + dlc;
		result = prime * result + (error_state_indicator ? 1231 : 1237);
		result = prime * result + (is_error_frame ? 1231 : 1237);
		result = prime * result + ((is_extended_id == null) ? 0 : is_extended_id.hashCode());
		result = prime * result + (is_fd ? 1231 : 1237);
		result = prime * result + (is_remote_frame ? 1231 : 1237);
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + Float.floatToIntBits(timestamp_delta);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (arbitration_id != other.arbitration_id)
			return false;
		if (bitrate_switch != other.bitrate_switch)
			return false;
		if (channel != other.channel)
			return false;
		if (!Arrays.equals(data, other.data))
			return false;
		if (dlc != other.dlc)
			return false;
		if (error_state_indicator != other.error_state_indicator)
			return false;
		if (is_error_frame != other.is_error_frame)
			return false;
		if (is_extended_id == null) {
			if (other.is_extended_id != null)
				return false;
		} else if (!is_extended_id.equals(other.is_extended_id))
			return false;
		if (is_fd != other.is_fd)
			return false;
		if (is_remote_frame != other.is_remote_frame)
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (Float.floatToIntBits(timestamp_delta) != Float.floatToIntBits(other.timestamp_delta))
			return false;
		return true;
	}

	public boolean equals(Object obj, float timestamp_delta) {
		float temp_timestamp_delta = this.timestamp_delta;
		this.timestamp_delta = timestamp_delta;
		boolean result = this.equals(obj);
		this.timestamp_delta = temp_timestamp_delta;
		return result;

	}
}
