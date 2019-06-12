package isotp.protocol;

import java.util.Arrays;

/** Represent a CAN message (ISO-11898) */
public class CanMessage {

	/**
	 * The CAN arbitration ID. Must be a 11 bits value or a 29 bits value if
	 * ``extended_id`` is True
	 */
	public int arbitration_id;
	/** The Data Length Code representing the number of bytes in the data field */
	public Integer dlc;
	/** The 8 bytes payload of the message */
	public byte[] data;
	/** When True, the arbitration ID stands on 29 bits. 11 bits when False */
	public Boolean is_extended_id;

	public CanMessage(int arbitration_id, Integer dlc, byte[] data, Boolean extended_id) {
		this.arbitration_id = arbitration_id;
		this.dlc = dlc;
		this.data = data;
		if (extended_id == null) {
			this.is_extended_id = false;
		} else {
			this.is_extended_id = extended_id;
		}
	}

//	public CanMessage(byte[] data) {
//		this(null, null, data, false);
//	}
//
//	public CanMessage(Integer arbitration_id) {
//		this(arbitration_id, null, null, false);
//	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("CanMessage [arbitration_id=0x%x", arbitration_id));
		builder.append(", dlc=");
		builder.append(dlc);
		builder.append(", data=");
		builder.append(Arrays.toString(data));
		builder.append(", is_extended_id=");
		builder.append(is_extended_id);
		builder.append("]");
		return builder.toString();
	}

}
