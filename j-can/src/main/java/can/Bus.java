package can;

import java.util.List;
import java.util.Map;

/** Bus wrapper with configuration loading. */
public class Bus extends BusABC {

	public Bus(Object channel, List<Filter> can_filters, String[] args) {
		super(channel, can_filters, args);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Map<String, Object> _recv_internal(Long time_left) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void send(Message msg, Long timeout) {
		// TODO Auto-generated method stub

	}

	@Override
	public void send(Message msg) {
		// TODO Auto-generated method stub

	}

}
