package udsoncan.connections;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import udsoncan.base.exceptions.TimeoutException;

public class QueueConnection implements BaseConnection {

	public String name;

	BlockingQueue<byte[]> fromuserqueue;
	BlockingQueue<byte[]> touserqueue;
	boolean opened;
	Integer mtu;

	public QueueConnection(Integer mtu) {
		this.fromuserqueue = new ArrayBlockingQueue<byte[]>(65535);
		this.touserqueue = new ArrayBlockingQueue<byte[]>(65535);
		this.opened = false;
		this.mtu = mtu;
		this.name = "Connection[" + name + "]";
	}

	public QueueConnection() {
		this(4095);
	}

	@Override
	public void specific_send(byte[] payload) throws Exception {
		if (this.mtu != null) {
			if (payload.length > this.mtu) {
				logger.warn(String.format("Truncating payload to be set to a length of %d", this.mtu));
				payload = Arrays.copyOfRange(payload, 0, this.mtu);
			}
		}
		this.touserqueue.put(payload);

	}

	@Override
	public byte[] specific_wait_frame(long timeout) throws Exception {
		if (!this.opened) {
			throw new Exception("Connection is not open");
		}

		boolean timedout = false;
		byte[] frame = null;
		try {
			frame = this.fromuserqueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			timedout = true;
		}

		if (timedout) {
			throw new TimeoutException(
					String.format("Did not receive frame from user queue in time (timeout=%s ms)", timeout));
		}

		if (this.mtu != null) {
			if (frame != null) {
				if (frame.length > this.mtu) {
					logger.warn(String.format("Truncating received payload to a length of %d", this.mtu));
					frame = Arrays.copyOfRange(frame, 0, this.mtu);
				}
			}
		}
		return frame;
	}

	@Override
	public void open() {
		this.opened = true;
		logger.info("Connection opened");
	}

	@Override
	public void close() {
		this.fromuserqueue.clear();
		this.touserqueue.clear();
		this.opened = false;
		logger.info("Connection closed");
	}

	@Override
	public void empty_rxqueue() {
		this.fromuserqueue.clear();
	}

	public void empty_txqueue() {
		this.touserqueue.clear();
	}

	@Override
	public boolean is_open() {
		return this.opened;
	}

}
