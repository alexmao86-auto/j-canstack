package udsoncan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import udsoncan.services.base.BaseService;
import udsoncan.services.base.ServiceFactory;

/**
 * Represents a UDS Request.
 * 
 */
public class Request {

	/**
	 * The service for which to make the request. This parameter must be a class
	 * that extends :class:`udsoncan.services.BaseService
	 */
	public BaseService service = null;

	/**
	 * The service subfunction. This value may be ignored if the given service does
	 * not supports subfunctions
	 */
	public Integer subfunction = null;

	/**
	 * Indicates that the server should not send a response if the response code is
	 * positive. This parameter has effect only when the given service supports
	 * subfunctions
	 */
	public boolean suppress_positive_response = false;

	/**
	 * The service data appended after service ID and payload
	 */
	public byte[] data = null;

	public Request(BaseService service, Integer subfunction, Boolean suppress_positive_response, byte[] data)
			throws Exception {

		if (service != null) {
			this.service = service;
		}

		if (subfunction != null) {
			this.subfunction = subfunction;
		}

		if (suppress_positive_response != null) {
			this.suppress_positive_response = suppress_positive_response;
		}

		if (this.suppress_positive_response && !this.service.use_subfunction()) {
			throw new Exception("Cannot suppress positive response for service" + this.service.get_name()
					+ ". This service does not have a subfunction");
		}

		if (data != null) {
			this.data = data;
		}
	}

	public Request(BaseService service, int subfunction, Boolean suppress_positive_response) throws Exception {
		this(service, subfunction, suppress_positive_response, null);
	}

	public Request(BaseService service, int subfunction, byte[] data) throws Exception {
		this(service, subfunction, null, data);
	}

	public Request(BaseService service, int subfunction) throws Exception {
		this(service, subfunction, null, null);
	}

	public Request(BaseService service, byte[] data) throws Exception {
		this(service, null, null, data);
	}

	public Request(BaseService service) throws Exception {
		this(service, null, null, null);
	}

	/**
	 * Generates a payload to be given to the underlying protocol. This method is
	 * meant to be used by a UDS client
	 * 
	 * @param suppress_positive_response
	 * @return: A payload to be sent through the underlying protocol
	 * @throws Exception
	 */
	public byte[] get_payload(boolean suppress_positive_response) throws Exception {
		List<Byte> payload = new ArrayList<Byte>();
		int requestid = this.service.request_id();
		payload.add((byte) requestid);
		if (this.service.use_subfunction()) {
			int sf = this.subfunction;
			if (suppress_positive_response) {
				sf |= 0x80;
			} else {
				sf &= ~0x80;
			}
			payload.add((byte) sf);
		} else {
			if (suppress_positive_response || this.suppress_positive_response) {
				throw new Exception("Cannot suppress positive response for service" + this.service.get_name()
						+ ". This service does not have a subfunction");
			}
		}

		if (this.data != null) {
			for (byte b : this.data) {
				payload.add(b);
			}
		}

		byte[] byteArray = new byte[payload.size()];
		for (int i = 0; i < payload.size(); i++)
			byteArray[i] = payload.get(i);
		return byteArray;
	}

	public byte[] get_payload() throws Exception {
		if (this.suppress_positive_response) {
			return this.get_payload(true);
		} else {
			return this.get_payload(false);
		}
	}

	// from_payload(cls, payload):
	/**
	 * Creates a ``Request`` object from a payload coming from the underlying
	 * protocols. This method is meant to be used by a UDS server
	 * 
	 * @param payload The payload of data to parse
	 */
	public Request(byte[] payload) {
		if (payload.length >= 1) {
			this.service = ServiceFactory.getService(payload[0]);

			// Invalid service ID will make service None
			if (this.service != null) {
				int offset = 0;
				if (this.service.use_subfunction()) {
					offset++;
					if (payload.length >= (offset + 1)) {
						this.subfunction = payload[1] & 0x7F;
						if ((payload[1] & 0x80) > 0) {
							this.suppress_positive_response = true;
						} else {
							this.suppress_positive_response = false;
						}
					}
				}
				if (payload.length > (offset + 1)) {
					this.data = Arrays.copyOfRange(payload, offset + 1, payload.length);
				}
			}

		}
	}

	public Request() {
	}

	public static Request from_payload(byte[] payload) {
		Request req = new Request();
		if (payload.length >= 1) {
			req.service = ServiceFactory.getService(payload[0]);

			// Invalid service ID will make service None
			if (req.service != null) {
				int offset = 0;
				if (req.service.use_subfunction()) {
					offset++;
					if (payload.length >= (offset + 1)) {
						req.subfunction = payload[1] & 0x7F;
						if ((payload[1] & 0x80) > 0) {
							req.suppress_positive_response = true;
						} else {
							req.suppress_positive_response = false;
						}
					}
				}
				if (payload.length > (offset + 1)) {
					req.data = Arrays.copyOfRange(payload, offset + 1, payload.length);
				}
			}
		}
		return req;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<Request: [");
		builder.append(this.service.get_name());
		builder.append("] ");
		if (this.service.use_subfunction()) {
			builder.append(String.format("subfunction=%d", this.subfunction));
		}
		builder.append("- ");
		builder.append(String.format("%d data bytes ", this.data.length));
		if (this.suppress_positive_response) {
			builder.append("[SuppressPosResponse] ");
		}
		builder.append(String.format("at 0x%x>", this.hashCode()));
		return builder.toString();
	}

	public int length() {
		try {
			return this.get_payload().length;
		} catch (Exception e) {
			return 0;
		}
	}

}
