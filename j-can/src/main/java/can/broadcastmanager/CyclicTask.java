package can.broadcastmanager; 

/** Abstract Base for all cyclic tasks. */
public interface CyclicTask {

	/** Cancel this periodic task */
	public void stop();
}
