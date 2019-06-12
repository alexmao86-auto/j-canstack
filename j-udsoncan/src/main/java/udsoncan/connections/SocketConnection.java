package udsoncan.connections;

import java.net.Socket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Sends and receives data through a socket. */
public class SocketConnection implements BaseConnection {

	Logger logger = LoggerFactory.getLogger(SocketConnection.class);

	/**
	 * The socket to use. This socket must be bound and ready to use. Only
	 * ``send()`` and ``recv()`` will be called by this Connections
	 */
	private Socket sock;

	/** Maximum buffer size of the socket, this value is passed to ``recv()`` */
	private int bufsize;

	private BlockingQueue<byte[]> rxqueue;
	private boolean exit_requested;

	private boolean opened;

	private ExecutorService rxthread = Executors.newCachedThreadPool();

	public SocketConnection(Socket sock, int bufsize, String name) {
		this.rxqueue = new ArrayBlockingQueue<byte[]>(65535);
		this.exit_requested = false;
		this.opened = false;
		this.sock = sock;
		try {
			this.sock.setSoTimeout(100); // for recv
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.bufsize = bufsize;
	}

	public SocketConnection(Socket sock, String name) {
		this.rxqueue = new ArrayBlockingQueue<byte[]>(65535);
		this.exit_requested = false;
		this.opened = false;
		this.sock = sock;
		try {
			this.sock.setSoTimeout(100); // for recv
		} catch (SocketException e) {
			e.printStackTrace();
		}

		this.bufsize = 4095;
	}

	@Override
	public byte[] specific_wait_frame(long timeout) throws Exception {
		if (!this.opened) {
			throw new RuntimeException("Connection is not open");
		}

		boolean timedout = false;
		byte[] frame = null;

		try {
			frame = this.rxqueue.poll(timeout, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			timedout = true;
		}

		if (timedout) {
			throw new Exception(String.format("Did not received frame in time (timeout=%d sec)", timeout));
		}
		return frame;
	}

	@Override
	public void specific_send(byte[] payload) {
		// TODO: self.sock.send(payload)
	}

	@Override
	public void open() {
		this.exit_requested = false;
		this.rxthread.execute(new rxthread_task());
		this.opened = true;
		this.logger.info("Connection opened");
	}

	@Override
	public void close() {
		this.exit_requested = true;
		try {
			this.rxthread.awaitTermination(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		this.opened = false;
		this.logger.info("Connection closed");

	}

	@Override
	public void empty_rxqueue() {
		this.rxqueue.clear();
	}

	private class rxthread_task implements Runnable {

		@Override
		public void run() {
			while (!exit_requested) {
				byte[] data = new byte[10];
				// TODO: receive data from the sock.
				if (data != null) {
					rxqueue.add(data);
				}
			}
		}

	}

	@Override
	public boolean is_open() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void empty_txqueue() {
		// TODO Auto-generated method stub
		
	}

}
