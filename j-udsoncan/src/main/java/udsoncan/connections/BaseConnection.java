package udsoncan.connections;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import udsoncan.Request;
import udsoncan.Response;

public interface BaseConnection {
	public Logger logger = LoggerFactory.getLogger(BaseConnection.class);

//	public String name;

//	public BaseConnection(String name) {
//		this.name = "Connection[" + name + "]";
//	}

	/**
	 * The implementation of the send method.
	 * @throws Exception 
	 */
	public void specific_send(byte[] payload) throws Exception;

	/**
	 * Sends data to the underlying transport protocol
	 * 
	 * @param data The data or object to send. If a Request or Response is given,
	 *             the value returned by get_payload() will be sent.
	 * @throws Exception 
	 */
	public default void send(byte[] data) throws Exception {
		byte[] payload = data;
		logger.debug(String.format("Sending %d bytes : [%s]", payload.length, Arrays.toString(payload)));
		this.specific_send(payload);
	}

	public default void send(Request request) throws Exception {
		this.send(request.get_payload());
	}

	public default void send(Response response) throws Exception {
		this.send(response.get_payload());
	}

	/**
	 * Waits for the reception of a frame of data from the underlying transport
	 * protocol
	 * 
	 * @param timeout   The maximum amount of time to wait before giving up in ms
	 * @param exception Boolean value indicating if this function may return
	 *                  exceptions. <br>
	 *                  When "True", all exceptions may be raised, including
	 *                  "TimeoutException" <br>
	 *                  When "False", all exceptions will be logged as "DEBUG" and
	 *                  "None" will be returned.
	 * @return Received data
	 */
	public default byte[] wait_frame(long timeout, boolean exception) throws Exception {
		byte[] frame = null;
		try {
			frame = this.specific_wait_frame(timeout);
		} catch (Exception e) {
			logger.debug(String.format("No data received: [%s] - %s ", e.getClass().getSimpleName(), e.getMessage()));

			if (exception) {
				throw e;
			}
		}
		return frame;
	}

	/**
	 * The implementation of the ``wait_frame`` method.
	 * 
	 * @param timeout The maximum amount of time to wait before giving up
	 * @return Received data
	 */
	public byte[] specific_wait_frame(long timeout) throws Exception;

	public default byte[] specific_wait_frame() throws Exception {
		return this.specific_wait_frame(2000);
	}

	/** Set up the connection object */
	public void open();

	/** Close the connection object */
	public void close();

	/** Empty all unread data in the reception buffer. */
	public void empty_rxqueue();
	
	/** Empty all unread data in the transition buffer. */
	public void empty_txqueue();

	public boolean is_open();
}
