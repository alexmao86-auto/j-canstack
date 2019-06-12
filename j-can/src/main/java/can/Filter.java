package can; 

public class Filter {
	public long can_id;
	public long can_mask;
	public Boolean extended;

	public Filter(long can_id, long can_mask, Boolean extended) {
		this.can_id = can_id;
		this.can_mask = can_mask;
		this.extended = extended;
	}

	public boolean match(long received_can_id, Boolean received_can_extended) {
		if (this.extended != null) {
			if (received_can_extended != this.extended) {
				return false;
			}
		}
		return (received_can_id & this.can_mask) == (this.can_id & this.can_mask);
	}

	public boolean match(Message msg) {
		if (this.extended != null) {
			if (msg.is_extended_id != this.extended) {
				return false;
			}
		}
		// basically, we compute `msg.arbitration_id & can_mask == can_id & can_mask` by
		// using the shorter, but equivalent from below:
		return (0 == ((this.can_id ^ msg.arbitration_id) & this.can_mask));
	}

}
