package can.interfaces.vector.jni;

public class XL_CAN_TX_MSG {
	public static final int XL_CAN_MAX_DATA_LEN = 64;
	public long id;
	public int flags;
	public int dlc;

	public byte[] data = new byte[XL_CAN_MAX_DATA_LEN];
}
