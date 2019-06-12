package isotp.address;

import isotp.protocol.CanMessage;

public interface Is_for_me {
	public boolean check(CanMessage msg);
}
