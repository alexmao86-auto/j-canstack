package can;

public class Util {

	public static final int[] CAN_FD_DLC = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 12, 16, 20, 24, 32, 48, 64 };

	public static int dlc2len(int dlc) {
		int ret = 64;
		if (dlc < 15)
			ret = CAN_FD_DLC[dlc];
		return ret;
	}

	public static int len2dlc(int length) {
		if (length <= 8) {
			return length;
		}

		for (int i = 0; i < CAN_FD_DLC.length; i++) {
			if (CAN_FD_DLC[i] >= length)
				return i;
		}
		return 15;
	}
}
