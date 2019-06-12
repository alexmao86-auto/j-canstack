package can.interfaces.vector.jni;

public class XL_CAN_EV_RX_MSG {

	public static final int XL_CAN_MAX_DATA_LEN = 64;
	public int canId;
	public int msgFlags;
	public int crc;

	public int totalBitCnt;
	public int dlc;

	public byte[] data = new byte[XL_CAN_MAX_DATA_LEN];
}
