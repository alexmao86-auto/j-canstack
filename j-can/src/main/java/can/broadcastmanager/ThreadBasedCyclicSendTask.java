package can.broadcastmanager;

import can.BusABC;
import can.Message;

public class ThreadBasedCyclicSendTask implements Runnable, RestartableCyclicTaskABC, ModifiableCyclicTaskABC {

	private Message message;
	private long can_id;
	private long arbitration_id;
	private Long period;
	private Long duration;
	private Thread thread;
	private boolean stopped;
	private BusABC bus;
	private Long end_time;
	private Boolean lock;

	public ThreadBasedCyclicSendTask(BusABC bus, Boolean lock, Message message, Long period, Long duration) {
		this.bus = bus;
		this.message = message;
		this.can_id = message.arbitration_id;
		this.arbitration_id = message.arbitration_id;
		this.period = period;
		this.duration = duration;

		this.thread = null;

		if (duration != null) {
			this.end_time = System.currentTimeMillis() + this.duration;
		}
		this.start();
	}

	@Override
	public void run() {
		while (!this.stopped) {
			long started;
//			synchronized (bus) {
			synchronized (this.lock) {
				started = System.currentTimeMillis();
				try {
					this.bus.send(this.message);
				} catch (Exception e) {
					break;
				}
			}

			if (this.end_time != null && (System.currentTimeMillis() >= this.end_time)) {
				break;
			}

			long delay = this.period - (System.currentTimeMillis() - started);
			try {
				Thread.sleep(delay);
			} catch (InterruptedException e) {
			}

		}
	}

	@Override
	public void stop() {
		this.stopped = true;
	}

	@Override
	public void start() {
		this.stopped = false;
		if (this.thread == null || !this.thread.isAlive()) {
			this.thread = new Thread(this);
			this.thread.setName(String.format("Cyclic send task for 0x%X", this.message.arbitration_id));
			this.thread.setDaemon(true);
			this.thread.start();
		}
	}

	@Override
	public void modify_data(Message message) {
		this.message = message;
	}

}
