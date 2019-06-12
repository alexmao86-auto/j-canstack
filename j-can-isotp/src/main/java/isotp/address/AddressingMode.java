package isotp.address;

public class AddressingMode {

	public static final int Normal_11bits = 0;
	public static final int Normal_29bits = 1;
	public static final int NormalFixed_29bits = 2;
	public static final int Extended_11bits = 3;
	public static final int Extended_29bits = 4;
	public static final int Mixed_11bits = 5;
	public static final int Mixed_29bits = 6;

	public String get_name(int num) {
		switch (num) {
		case AddressingMode.Normal_11bits:
			return "Normal_11bits";
		case AddressingMode.Normal_29bits:
			return "Normal_29bits";
		case AddressingMode.NormalFixed_29bits:
			return "NormalFixed_29bits";
		case AddressingMode.Extended_11bits:
			return "Extended_11bits";
		case AddressingMode.Extended_29bits:
			return "Extended_29bits";
		case AddressingMode.Mixed_11bits:
			return "Mixed_11bits";
		case AddressingMode.Mixed_29bits:
			return "Mixed_29bits";
		default:
			break;
		}
		return "Unknown";
	}

	public static boolean is29bits(int num) {
		switch (num) {
		case AddressingMode.Normal_11bits:
			return false;
		case AddressingMode.Normal_29bits:
			return true;
		case AddressingMode.NormalFixed_29bits:
			return true;
		case AddressingMode.Extended_11bits:
			return false;
		case AddressingMode.Extended_29bits:
			return true;
		case AddressingMode.Mixed_11bits:
			return false;
		case AddressingMode.Mixed_29bits:
			return true;
		default:
			break;
		}
		return false;

	}

	public static boolean isAddressingMode(int num) {
		switch (num) {
		case AddressingMode.Normal_11bits:
		case AddressingMode.Normal_29bits:
		case AddressingMode.NormalFixed_29bits:
		case AddressingMode.Extended_11bits:
		case AddressingMode.Extended_29bits:
		case AddressingMode.Mixed_11bits:
		case AddressingMode.Mixed_29bits:
			return true;
		default:
			return false;
		}
	}
}
