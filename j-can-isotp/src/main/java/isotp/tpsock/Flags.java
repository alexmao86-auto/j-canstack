package isotp.tpsock;

public class Flags {
	/** Puts the socket in Listen mode, which prevents transmission of data */
	public static final int LISTEN_MODE = 0x001;

	/** When set, an address extension byte (set in socket general options) will be
	 added to each payload sent. Unless public static final int RX_EXT_ADDR is
	 also set, this value will be expected for reception as well*/
	public static final int EXTEND_ADDR = 0x002;
	
	/** Enables padding of transmitted data with a byte set in the socket general
	 options*/
	public static final int TX_PADDING = 0x004;
	
	/**Indicates that data padding is possible in reception. Must be set for
	 CHK_PAD_LEN and CHK_PAD_DATA to have an effect*/
	public static final int RX_PADDING = 0x008;
	 
	/**Makes the socket validate the padding length of the CAN message*/
	public static final int CHK_PAD_LEN = 0x010;
	 
	/** Makes the socket validate the padding bytes of the CAN message*/
	public static final int CHK_PAD_DATA = 0x020;
	
	/** Sets the socket in half duplex mode, forcing transmission and reception to
	 happen sequentially*/
	public static final int HALF_DUPLEX = 0x040;
	
	/**
	 * Forces the socket to use the separation time sets in general options,
	 * overriding stmin value received in flow control frames.
	 */
	public static final int FORCE_TXSTMIN = 0x080;
	 
	/**
	 * Forces the socket to ignore any message received faster than stmin given in
	 * the flow control frame
	 */
	public static final int FORCE_RXSTMIN = 0x100;
	 
	/**
	 * When sets, a different extended address can be used for reception than for
	 * transmission.
	 */
	public static final int RX_EXT_ADDR = 0x200;
}
