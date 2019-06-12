package can.interfaces.vector.jni;

public class XLevent {
	public int tag;
	public int chanIndex; // 1
	public int transId; // 2
	public int portHandle; // 2 internal use only !!!!
	public int flags; // 1 (e.g. XL_EVENT_FLAG_OVERRUN)
	public long timeStamp; // 8
	public s_xl_can_msg tagData=new s_xl_can_msg(); // 32 Bytes
}
