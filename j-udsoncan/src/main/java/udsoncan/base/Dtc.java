package udsoncan.base;

import java.util.List;

/**
 * Defines a Diagnostic Trouble Code which consist of a 3-byte ID, a status, a
 * severity and some diagnostic data.
 * 
 * :param dtcid: The 3-byte ID of the DTC :type dtcid: int
 * 
 **/
public class Dtc {

	/**
	 * Provide a list of DTC formats and their indices. These values are used by the
	 * :ref:`The ReadDTCInformation<ReadDtcInformation>` when requesting a number of
	 * DTCs.
	 */
	public class Format {
		public final int ISO15031_6 = 0;
		public final int ISO14229_1 = 1;
		public final int SAE_J1939_73 = 2;
		public final int ISO11992_4 = 3;
	}

	/**
	 * # DTC Status byte # This byte is an 8-bit flag indicating how much we are
	 * sure that a DTC is active. Represents a DTC status which consists of 8
	 * boolean flags (a byte). All flags can be set after instantiation without
	 * problems.
	 * 
	 * :param test_failed: DTC is no longer failed at the time of the request :type
	 * test_failed: bool
	 * 
	 * :param test_failed_this_operation_cycle: DTC never failed on the current
	 * operation cycle. :type test_failed_this_operation_cycle: bool
	 * 
	 * :param pending: DTC failed on the current or previous operation cycle. :type
	 * pending: bool
	 * 
	 * :param confirmed: DTC is not confirmed at the time of the request. :type
	 * confirmed: bool
	 * 
	 * :param test_not_completed_since_last_clear: DTC test has been completed since
	 * the last codeclear. :type test_not_completed_since_last_clear: bool
	 * 
	 * :param test_failed_since_last_clear: DTC test failed at least once since last
	 * code clear. :type test_failed_since_last_clear: bool
	 * 
	 * :param test_not_completed_this_operation_cycle: DTC test completed this
	 * operation cycle. :type test_not_completed_this_operation_cycle: bool
	 * 
	 * :param warning_indicator_requested: Server is not requesting warningIndicator
	 * to be active. :type warning_indicator_requested: bool
	 * 
	 */
	public class Status {
		private boolean test_failed;
		private boolean test_failed_this_operation_cycle;
		private boolean pending;
		private boolean confirmed;
		private boolean test_not_completed_since_last_clear;
		private boolean test_failed_since_last_clear;
		private boolean test_not_completed_this_operation_cycle;
		private boolean warning_indicator_requested;

		public Status(boolean test_failed, boolean test_failed_this_operation_cycle, boolean pending, boolean confirmed,
				boolean test_not_completed_since_last_clear, boolean test_failed_since_last_clear,
				boolean test_not_completed_this_operation_cycle, boolean warning_indicator_requested) {
			this.test_failed = test_failed;
			this.test_failed_this_operation_cycle = test_failed_this_operation_cycle;
			this.pending = pending;
			this.confirmed = confirmed;
			this.test_not_completed_since_last_clear = test_not_completed_since_last_clear;
			this.test_failed_since_last_clear = test_failed_since_last_clear;
			this.test_not_completed_this_operation_cycle = test_not_completed_this_operation_cycle;
			this.warning_indicator_requested = warning_indicator_requested;
		}

		public Status(byte b) {
			this.set_byte(b);
		}

		public int get_byte_as_int() {
			int b = 0;
			if (this.test_failed)
				b |= 0x01;
			if (this.test_failed_this_operation_cycle)
				b |= 0x02;
			if (this.pending)
				b |= 0x04;
			if (this.confirmed)
				b |= 0x08;
			if (this.test_not_completed_since_last_clear)
				b |= 0x10;
			if (this.test_failed_since_last_clear)
				b |= 0x20;
			if (this.test_not_completed_this_operation_cycle)
				b |= 0x40;
			if (this.warning_indicator_requested)
				b |= 0x80;
			return b;
		}

		public byte get_byte() {
			return (byte) this.get_byte_as_int();
		}

		public void set_byte(byte b) {
			this.test_failed = (b &= 0x01) > 0 ? true : false;
			this.test_failed_this_operation_cycle = (b &= 0x02) > 0 ? true : false;
			this.pending = (b &= 0x04) > 0 ? true : false;
			this.confirmed = (b &= 0x08) > 0 ? true : false;
			this.test_not_completed_since_last_clear = (b &= 0x10) > 0 ? true : false;
			this.test_failed_since_last_clear = (b &= 0x20) > 0 ? true : false;
			this.test_not_completed_this_operation_cycle = (b &= 0x40) > 0 ? true : false;
			this.warning_indicator_requested = (b &= 0x80) > 0 ? true : false;
		}

	}

	/**
	 * DTC Severity byte, it's a 3-bit indicator telling how serious a trouble code
	 * is.
	 * 
	 * Represents a DTC severity which consists of 3 boolean flags. All flags can be
	 * set after instantiation without problems.
	 * 
	 * :param maintenance_only: This value indicates that the failure requests
	 * maintenance only :type maintenance_only: bool
	 * 
	 * :param check_at_next_exit: This value indicates that the failure requires a
	 * check of the vehicle at the next halt. :type check_at_next_exit: bool
	 * 
	 * :param check_immediately: This value indicates that the failure requires an
	 * immediate check of the vehicle. :type check_immediately: bool
	 */
	public class Severity {
		private boolean maintenance_only = false;
		private boolean check_at_next_exit = false;
		private boolean check_immediately = false;

		public Severity(boolean maintenance_only, boolean check_at_next_exit, boolean check_immediately) {
			this.maintenance_only = maintenance_only;
			this.check_at_next_exit = check_at_next_exit;
			this.check_immediately = check_immediately;
		}

		public Severity(byte b) {
			this.set_byte(b);
		}

		public boolean available() {
			return (this.get_byte_as_int() > 0);
		}

		public int get_byte_as_int() {
			int b = 0;
			if (this.maintenance_only)
				b |= 0x20;
			if (this.check_at_next_exit)
				b |= 0x40;
			if (this.check_immediately)
				b |= 0x80;
			return b;
		}

		public byte get_byte() {
			return (byte) this.get_byte_as_int();
		}

		public void set_byte(byte b) {
			this.maintenance_only = (b &= 0x20) > 0 ? true : false;
			this.check_at_next_exit = (b &= 0x40) > 0 ? true : false;
			this.check_immediately = (b &= 0x80) > 0 ? true : false;
		}
	}

	/**
	 * # A snapshot data. Not defined by ISO14229 and implementation specific. # To
	 * read this data, the client must have a DID codec set in its config.
	 */
	public class Snapshot {
		public int record_number;
		public int did;
		public byte[] data;
		public byte[] raw_data;
	}

	/**
	 * # Extended data. Not defined by ISO14229 and implementation specific # Only
	 * raw data can be given to user.
	 */
	public class ExtendedData {
		public int record_number;
		public byte[] raw_data;
	}

	/*
	 * self.id = dtcid self.status = Dtc.Status() self.snapshots = [] # . DID codec
	 * must be configured self.extended_data = [] self.severity = Dtc.Severity()
	 * self.functional_unit = None # Implementation specific (ISO 14229 D.4)
	 * self.fault_counter = None
	 */
	private int id;
	private Status status;
	private List<Snapshot> snapshots;
	private List<ExtendedData> extended_data;
	private Severity severity;
	private Object functional_unit;
	private Object fault_counter;

	public Dtc(int id, Status status, List<Snapshot> snapshots, List<ExtendedData> extended_data, Severity severity,
			Object functional_unit, Object fault_counter) {
		super();
		this.id = id;
		this.status = status;
		this.snapshots = snapshots;
		this.extended_data = extended_data;
		this.severity = severity;
		this.functional_unit = functional_unit;
		this.fault_counter = fault_counter;
	}

}
