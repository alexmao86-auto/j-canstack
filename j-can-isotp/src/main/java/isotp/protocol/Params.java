package isotp.protocol;

import java.lang.reflect.Field;

import isotp.errors.IsoTpError;
import isotp.errors.ValueError;

public class Params {
	// fields
	public int stmin;
	public int blocksize;
	public Boolean squash_stmin_requirement;
	public Long rx_flowcontrol_timeout;
	public Long rx_consecutive_frame_timeout;
	public Integer tx_padding;
	public Integer wftmax;
	public Integer ll_data_length;

	public Params() {
		this.stmin = 0;
		this.blocksize = 8;
		this.squash_stmin_requirement = false;
		this.rx_flowcontrol_timeout = 1000l;
		this.rx_consecutive_frame_timeout = 1000l;
		this.tx_padding = null;
		this.wftmax = 0;
		this.ll_data_length = 8;
	}

	public Params(Params p) {
		this.stmin = 0;
		this.blocksize = 8;
		this.squash_stmin_requirement = false;
		this.rx_flowcontrol_timeout = 1000l;
		this.rx_consecutive_frame_timeout = 1000l;
		this.tx_padding = null;
		this.wftmax = 0;
		this.ll_data_length = 8;
		if (p != null) {
			this.stmin = p.stmin;
			this.blocksize = p.blocksize;

			if (p.squash_stmin_requirement != null)
				this.squash_stmin_requirement = p.squash_stmin_requirement;
			if (p.rx_flowcontrol_timeout != null)
				this.rx_flowcontrol_timeout = p.rx_flowcontrol_timeout;
			if (p.rx_consecutive_frame_timeout != null)
				this.rx_consecutive_frame_timeout = p.rx_consecutive_frame_timeout;
			if (p.tx_padding != null)
				this.tx_padding = p.tx_padding;
			if (p.wftmax != null)
				this.wftmax = p.wftmax;
			if (p.ll_data_length != null)
				this.ll_data_length = p.ll_data_length;
		}

	}

//	public Params(int stmin, int blocksize, Boolean squash_stmin_requirement, Long rx_flowcontrol_timeout,
//			Long rx_consecutive_frame_timeout, Integer tx_padding, Integer wftmax, Integer ll_data_length)
//			throws Exception {
//		this.stmin = stmin;
//		this.blocksize = blocksize;
//		this.squash_stmin_requirement = squash_stmin_requirement;
//		this.rx_flowcontrol_timeout = rx_flowcontrol_timeout;
//		this.rx_consecutive_frame_timeout = rx_consecutive_frame_timeout;
//		this.tx_padding = tx_padding;
//		this.wftmax = wftmax;
//		this.ll_data_length = ll_data_length;
//
//		this.validate();
//	}

	public void set(String key, Object value) throws Exception {
		Field field = Params.class.getField(key);
		field.set(this, value);
		this.validate();
	}

	private void validate() throws Exception {
		if (this.rx_flowcontrol_timeout < 0) {
			throw new ValueError("rx_flowcontrol_timeout must be positive integer");
		}

		if (this.rx_consecutive_frame_timeout < 0) {
			throw new ValueError("rx_consecutive_frame_timeout must be positive integer");
		}

		if (this.tx_padding != null) {
			if (this.tx_padding < 0 || this.tx_padding > 0xFF) {
				throw new ValueError("tx_padding must be an integer between 0x00 and 0xFF");
			}
		}

		if (this.stmin < 0 || this.stmin > 0xFF) {
			throw new ValueError("stmin must be an integer between 0x00 and 0xFF");
		}

		if (this.blocksize < 0 || this.blocksize > 0xFF) {
			throw new ValueError("blocksize must be an integer between 0x00 and 0xFF");
		}

		if (this.wftmax < 0) {
			throw new ValueError("wftmax must be and integer equal or greater than 0");
		}

		if (this.ll_data_length < 0) {
			throw new ValueError("ll_data_length must be at least 4 bytes");
		}

	}

}