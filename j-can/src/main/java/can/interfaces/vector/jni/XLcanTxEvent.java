package can.interfaces.vector.jni;

public class XLcanTxEvent {
	public int tag; // 2 - type of the event
	public int transId; // 2
	public int channelIndex; // 1 - internal has to be 0
	
	public XL_CAN_TX_MSG tagData = new XL_CAN_TX_MSG();
}
