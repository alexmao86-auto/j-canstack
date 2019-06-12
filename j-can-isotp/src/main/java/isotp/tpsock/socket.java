package isotp.tpsock;

import java.net.Socket;
import java.net.SocketException;

import isotp.address.Address;

/** A IsoTP socket wrapper for easy configuration */
public class socket {

//	/**
//	 * The underlying socket timeout set with ``settimeout``. Makes the reception
//	 * thread sleep
//	 */
//	private int timeout;

	/** The network interface to use */
	private String inter_face;

	private Address address;
	private boolean bound;
	private boolean closed;
	private Socket _socket;

	public socket(int timeout) throws Exception {
		throw new Exception("not implemented yet");
//		this.inter_face = null;
//		this.address = null;
//		this.bound = false;
//		this.closed = false;
//		this._socket = new Socket();
//		if (timeout > 0) {
//			try {
//				this._socket.setSoTimeout(timeout);
//			} catch (SocketException e) {
//				e.printStackTrace();
//			}
//		}
	}

	public void send() {
		if (!this.bound) {

		}

	}

}
