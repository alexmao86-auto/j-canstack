package can.broadcastmanager;

/** Adds support for restarting a stopped cyclic task */
public interface RestartableCyclicTaskABC extends CyclicTask {

	/** Restart a stopped periodic task. */
	public void start();
}
