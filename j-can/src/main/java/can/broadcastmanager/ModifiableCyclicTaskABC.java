package can.broadcastmanager;

import can.Message; 

/** Adds support for modifying a periodic message */
public interface ModifiableCyclicTaskABC extends CyclicTask {
	/**
	 * Update the contents of this periodically sent message without altering the
	 * timing
	 * 
	 * @param message The message with the new :attr:`can.Message.data`. Note: The
	 *                arbitration ID cannot be changed.
	 */
	public void modify_data(Message message);
}
