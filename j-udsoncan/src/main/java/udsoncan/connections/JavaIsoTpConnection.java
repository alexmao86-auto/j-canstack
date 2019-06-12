package udsoncan.connections;

import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import can.BusABC;
import isotp.protocol.TransportLayer;
import udsoncan.base.exceptions.TimeoutException;

/**
 * Sends and receives data using a "j-can-isotp" module which is a Java
 * implementation of the IsoTp transport protocol which can be coupled with
 * "j-can" to interract with CAN hardware
 */
public class JavaIsoTpConnection implements BaseConnection {
	public String name;
	public int mtu = 4095;
	public TransportLayer isotp_layer;
	public BlockingQueue<byte[]> fromIsoTPQueue;
	public BlockingQueue<byte[]> toIsoTPQueue;

	ExecutorService rxthread;//
	boolean exit_requested;
	boolean opened;

	public JavaIsoTpConnection(TransportLayer isotp_layer, String name) {
		this.toIsoTPQueue = new ArrayBlockingQueue<byte[]>(65535);
		this.fromIsoTPQueue = new ArrayBlockingQueue<byte[]>(65535);
		this.rxthread = null;
		this.exit_requested = false;
		this.opened = false;
		this.isotp_layer = isotp_layer;
	}

	@Override
	public void specific_send(byte[] payload) throws Exception {
		if (payload.length > this.mtu) {
			logger.warn(String.format("Truncating payload to be set to a length of %d", this.mtu));
			payload = Arrays.copyOfRange(payload, 0, this.mtu);
		}

		this.toIsoTPQueue.put(payload);
	}

	@Override
	public byte[] specific_wait_frame(long timeout) throws Exception {
		if (!this.opened) {
			throw new Exception("Connection is not open");
		}
		boolean timedout = false;
		byte[] frame = null;
		try {
			frame = this.fromIsoTPQueue.poll(timeout, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			timedout = true;
		}
		if (timedout) {
			throw new TimeoutException(
					String.format("Did not receive frame from user queue in time (timeout=%s ms)", timeout));
		}
		if (frame != null) {
			if (frame.length > this.mtu) {
				logger.warn(String.format("Truncating received payload to a length of %d", this.mtu));
				frame = Arrays.copyOfRange(frame, 0, this.mtu);
			}
		}
		return frame;
	}

	@Override
	public void open() {
		this.exit_requested = false;
		this.rxthread = Executors.newCachedThreadPool();
		this.rxthread.execute(new rxthread_task());
		this.opened = true;
		logger.info("Connection opened");
	}

	public void open(BusABC bus) {
		if (bus != null) {
			// TODO: implement set_bus
//			this.isotp_layer.set_bus(bus);
		}
		this.open();
	}

	@Override
	public void close() {
		this.empty_rxqueue();
		this.empty_txqueue();
		this.exit_requested = true;
		try {
			this.rxthread.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.isotp_layer.reset();
		this.opened = false;
		logger.info("Connection closed");

	}

	@Override
	public void empty_rxqueue() {
		this.fromIsoTPQueue.clear();
	}

	@Override
	public void empty_txqueue() {
		this.toIsoTPQueue.clear();
	}

	@Override
	public boolean is_open() {
		return this.opened;
	}

	class rxthread_task implements Runnable {

		@Override
		public void run() {
			while (!exit_requested) {
				try {
					while(!toIsoTPQueue.isEmpty()) {
						isotp_layer.send(toIsoTPQueue.take());
					}
					isotp_layer.process();
					while(isotp_layer.available()) {
						fromIsoTPQueue.offer(isotp_layer.recv());
					}
					Thread.sleep(isotp_layer.sleep_time());

				} catch (Exception e) {
					exit_requested = true;
					logger.error(e.getMessage());
				}
				
			}
		}

	}

}
