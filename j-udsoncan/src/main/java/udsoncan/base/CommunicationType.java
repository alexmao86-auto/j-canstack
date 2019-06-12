package udsoncan.base;

/**
 * # Communication type is a single byte value including message type and
 * subnet. # Used by CommunicationControl service and defined by ISO-14229:2006
 * Annex B, table B.1
 * 
 * This class represents a pair of subnet and message types. This value is
 * mainly used by the :ref:`CommunicationControl<CommunicationControl>` service
 * 
 * :param subnet: Represent the subnet number. Value ranges from 0 to 0xF :type
 * subnet: int
 * 
 * :param normal_msg: Bit indicating that the `normal messages` are involved
 * :type normal_msg: bool
 * 
 * :param network_management_msg: Bit indicating that the `network management
 * messages` are involved :type network_management_msg: bool
 */
public class CommunicationType {

	public class Subnet {
		public static final int node = 0;
		public static final int network = 0xF;

		private int subnet;

		public Subnet(int subnet) throws Exception {
			super();

			if (subnet < 0 || subnet > 0xF) {
				throw new Exception("subnet must be an integer between 0 and 0xF");
			}
			this.subnet = subnet;
		}

		public int value() {
			return this.subnet;
		}

	}

	public Subnet subnet;
	public boolean normal_msg = false;
	public boolean network_management_msg = false;

	public CommunicationType(Subnet subnet, boolean normal_msg, boolean network_management_msg) throws Exception {
		if ((!normal_msg) && (!network_management_msg)) {
			throw new Exception("At least one message type must be controlled");
		}
		this.subnet = subnet;
		this.normal_msg = normal_msg;
		this.network_management_msg = network_management_msg;
	}

	public CommunicationType(int subnet, boolean normal_msg, boolean network_management_msg) throws Exception {
		if ((!normal_msg) && (!network_management_msg)) {
			throw new Exception("At least one message type must be controlled");
		}
		this.subnet = new Subnet(subnet);
		this.normal_msg = normal_msg;
		this.network_management_msg = network_management_msg;
	}

	public CommunicationType(int val) throws Exception {
		this.subnet = new Subnet((val & 0xF0) >> 4);
		if ((val & 1) > 0) {
			this.normal_msg = true;
		}
		if ((val & 2) > 0) {
			this.network_management_msg = true;
		}
	}

	public int get_byte_as_int() {
		int message_type = 0;
		if (this.normal_msg) {
			message_type |= 1;
		}
		if (this.network_management_msg) {
			message_type |= 2;
		}
		return message_type;
	}

	public byte get_byte() {
		return (byte) this.get_byte_as_int();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("subnet=0x");
		builder.append(String.format("%x", this.subnet));
		builder.append(", Flags : ");
		if (this.normal_msg)
			builder.append("NormalMsg");
		if (this.network_management_msg)
			builder.append(" NetworkManagementMsg");

		return builder.toString();
	}

	public static CommunicationType from_byte(byte val) throws Exception {
		int _subnet = (val & 0xF0) >> 4;
		boolean _normal_msg = false;
		boolean _network_management_msg = false;
		if ((val & 1) > 0) {
			_normal_msg = true;
		}
		if ((val & 2) > 0) {
			_network_management_msg = true;
		}

		return new CommunicationType(_subnet, _normal_msg, _network_management_msg);

	}

}
