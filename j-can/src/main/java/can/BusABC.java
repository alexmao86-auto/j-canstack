package can;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import can.broadcastmanager.ThreadBasedCyclicSendTask;

/**
 * The CAN Bus Abstract Base Class that serves as the basis for all concrete
 * interfaces.
 * 
 * This class may be used as an iterator over the received messages.
 */
public abstract class BusABC {
	public Logger logger = LoggerFactory.getLogger(BusABC.class);

	/** a string describing the underlying bus and/or channel */
	public String channel_info = "unknown";

	/** Log level for received messages */
	private static final int RECV_LOGGING_LEVEL = 9;

	public List<Filter> _filters;
	public List<ThreadBasedCyclicSendTask> _periodic_tasks;

	public Boolean _lock_send_periodic;
	public BusState busState = BusState.ACTIVE;

	/**
	 * Construct and open a CAN bus instance of the specified type.
	 * 
	 * Subclasses should call though this method with all given parameters as it
	 * handles generic tasks like applying filters.
	 * 
	 * @param channel     The can interface identifier. Expected type is backend
	 *                    dependent.
	 * @param can_filters See :meth:`~can.BusABC.set_filters` for details.
	 * @param args        Any backend dependent configurations are passed in this
	 *                    dictionary
	 */
	public BusABC(Object channel, List<Filter> can_filters, String[] args) {
		this._periodic_tasks = new ArrayList<ThreadBasedCyclicSendTask>();
		this.set_filters(can_filters);
	}

	public BusABC() {
		this._periodic_tasks = new ArrayList<ThreadBasedCyclicSendTask>();
	}

	/**
	 * Apply filtering to all messages received by this Bus.
	 * 
	 * All messages that match at least one filter are returned. If `filters` is
	 * `None` or a zero length sequence, all messages are matched.
	 * 
	 * Calling without passing any filters will reset the applied filters to `None`.
	 * 
	 * @param can_filters A iterable of dictionaries each containing a "can_id", a
	 *                    "can_mask", and an optional "extended" key.A filter
	 *                    matches, when ``<received_can_id> & can_mask == can_id &
	 *                    can_mask``. If ``extended`` is set as well, it only
	 *                    matches messages where ``<received_is_extended> ==
	 *                    extended``. Else it matches every messages based only on
	 *                    the arbitration ID and mask.
	 */
	public void set_filters(List<Filter> filters) {
		this._filters = filters;
		this._apply_filters(this._filters);
	}

	/**
	 * Hook for applying the filters to the underlying kernel or hardware if
	 * supported/implemented by the interface.
	 * 
	 * {@link can.BusABC.set_filters}
	 */
	public void _apply_filters(List<Filter> filters) {

	}

	public String toString() {
		return this.channel_info;
	}

	/**
	 * Block waiting for a message from the Bus.
	 * 
	 * @param timeout seconds to wait for a message or None to wait indefinitely,
	 *                unit=millisecond
	 * @return None on timeout or a :class:`can.Message` object.
	 * @throws Exception
	 */
	public Message recv(Long timeout) throws Exception {

		long start = System.currentTimeMillis();
		Long time_left = timeout;

		Map<String, Object> newMessage;
		while (true) {
			// try to get a message
			newMessage = this._recv_internal(time_left);

			Message msg = (Message) newMessage.get("Message");
			boolean already_filtered = (boolean) newMessage.get("already_filtered");
			// return it, if it matches
			if ((msg != null) && (already_filtered || this._match_filters(msg))) {
				logger.debug("Received: " + msg.toString());
				return msg;
			} else if (timeout == null) {
				// if not, and timeout is None, try indefinitely
				continue;
			} else {
				// try next one only if there still is time, and with reduced timeout
				time_left = timeout - (System.currentTimeMillis() - start);
				if (time_left > 0) {
					continue;
				} else {
					return null;
				}
			}
		}
	}

	/**
	 * Read a message from the bus and tell whether it was filtered. This methods
	 * may be called by :meth:`~can.BusABC.recv` to read a message multiple times if
	 * the filters set by :meth:`~can.BusABC.set_filters` do not match and the call
	 * has not yet timed out.
	 * 
	 * New implementations should always override this method instead of
	 * :meth:`~can.BusABC.recv`, to be able to take advantage of the software based
	 * filtering provided by :meth:`~can.BusABC.recv` as a fallback. This method
	 * should never be called directly.
	 * 
	 * @param timeout: milliseconds to wait for a messages
	 * 
	 * @return: 1. a message that was read or None on timeout 2. a bool that is True
	 *          if message filtering has already been done and else False
	 * @throws Exception
	 * 
	 * @exception can.CanError: if an error occurred while reading
	 */
	public abstract Map<String, Object> _recv_internal(Long timeout) throws Exception;

	/**
	 * Transmit a message to the CAN bus.
	 * 
	 * Override this method to enable the transmit path.
	 * 
	 * @param msg     A message object.
	 * @param timeout If > 0, wait up to this many seconds for message to be ACK'ed
	 *                or for transmit queue to be ready depending on driver
	 *                implementation. If timeout is exceeded, an exception will be
	 *                raised. Might not be supported by all interfaces. None blocks
	 *                indefinitly.
	 * 
	 * @exception can.CanError if the message could not be sents
	 */
	public abstract void send(Message msg, Long timeout);

	public void send(Message msg) {
		this.send(msg, null);
	}

	/**
	 * Start sending a message at a given period on this bus.
	 * 
	 * The task will be active until one of the following conditions are met:
	 * 
	 * - the (optional) duration expires - the Bus instance goes out of scope - the
	 * Bus instance is shutdown - :meth:`BusABC.stop_all_periodic_tasks()` is called
	 * - the task's :meth:`CyclicTask.stop()` method is called.
	 * 
	 * @param msg        Message to transmit
	 * @param period     Period in seconds between each message
	 * @param duration   The duration to keep sending this message at given rate. If
	 *                   no duration is provided, the task will continue
	 *                   indefinitely.
	 * @param store_task If True (the default) the task will be attached to this Bus
	 *                   instance. Disable to instead manage tasks manually.
	 * @return A started task instance. Note the task can be stopped (and depending
	 *         on the backend modified) by calling the :meth:`stop` method.
	 *         can.broadcastmanager.CyclicSendTaskABC
	 */
	public ThreadBasedCyclicSendTask send_periodic(Message msg, Long period, Long duration, boolean store_task) {
		ThreadBasedCyclicSendTask task = this._send_periodic_internal(msg, period, duration);

		if (store_task) {
			this._periodic_tasks.add(task);
		}

		return task;
	}

	/**
	 * Stop sending any messages that were started using bus.send_periodic
	 * 
	 * @param remove_tasks Stop tracking the stopped tasks
	 */
	public void stop_all_periodic_tasks(boolean remove_tasks) {
		for (int i = 0; i < this._periodic_tasks.size(); i++) {

			if (remove_tasks) {
				this._periodic_tasks.remove(i).stop();
			} else {
				this._periodic_tasks.get(i).stop();
			}
		}
	}

	public void stop_all_periodic_tasks() {
		this.stop_all_periodic_tasks(true);
	}

	/**
	 * Default implementation of periodic message sending using threading. Override
	 * this method to enable a more efficient backend specific approach.
	 */
	private ThreadBasedCyclicSendTask _send_periodic_internal(Message msg, Long period, Long duration) {
		if (this._lock_send_periodic == null) {
			this._lock_send_periodic = new Boolean(true);
		}
		ThreadBasedCyclicSendTask task = new ThreadBasedCyclicSendTask(this, this._lock_send_periodic, msg, period,
				duration);
		return task;
	}

	/**
	 * Checks whether the given message matches at least one of the current filters.
	 * See :meth:`~can.BusABC.set_filters` for details on how the filters work.
	 * 
	 * This method should not be overridden.
	 * 
	 * @param msg the message to check if matching
	 * @return whether the given message matches at least one filter
	 */
	public boolean _match_filters(Message msg) {
		// if no filters are set, all messages are matched
		if (this._filters == null)
			return true;
		if (this._filters.isEmpty()) {
			return true;
		}

		for (int i = 0; i < this._filters.size(); i++) {
			Filter _filter = this._filters.get(i);
			// check if this filter even applies to the message
			if (_filter.match(msg)) {
				return true;
			}
		}

		// nothing matched
		return false;
	}

	/** Modify the filters of this bus */
	public List<Filter> filters() {
		return this._filters;
	}

	/** filters.setter */
	public void filters(List<Filter> filters) {
		this.set_filters(filters);
	}

	/** Discard every message that may be queued in the output buffer(s).s */
	public void flush_tx_buffer() {

	}

	/**
	 * Called to carry out any interface specific cleanup required in shutting down
	 * a bus.
	 */
	public void shutdown() {

	}

	/**
	 * Return the current state of the hardware
	 * 
	 * @return
	 */
	public BusState state() {
		return BusState.ACTIVE;
	}

	/**
	 * Set the new state of the hardware
	 * 
	 * @throws Exception
	 */
	public void state(BusState new_state) throws Exception {
		throw new Exception("NotImplementedException");
	}

	/**
	 * Detect all configurations/channels that this interface could currently
	 * connect with.
	 * 
	 * This might be quite time consuming.
	 * 
	 * May not to be implemented by every interface on every platform.
	 * 
	 * @return an iterable of dicts, each being a configuration suitable for usage
	 *         in the interface's bus constructor
	 * @throws Exception
	 */
	static void _detect_available_configs() throws Exception {
		throw new Exception("NotImplementedException");
	}
}
