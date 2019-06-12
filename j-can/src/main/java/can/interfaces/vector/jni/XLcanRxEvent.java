package can.interfaces.vector.jni;

public class XLcanRxEvent {
	public int size; // 4 - overall size of the complete event
	public int tag; // 2 - type of the event
	public int channelIndex; // 2
	public int userHandle; // 4 (lower 12 bit available for CAN)
	public int flagsChip; // 2 queue overflow (upper 8bit)

	public long timeStampSync; // 8 - timestamp which is synchronized by the driver
	public XL_CAN_EV_RX_MSG canRxOkMsg;
}
