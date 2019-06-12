package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;

public class TesterPresent extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.SubFunctionNotSupported);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
		}
	};

	public TesterPresent() {
		this._sid = 0x3E;
	}

	/**
	 * Generates a request for TesterPresent
	 * 
	 * 
	 * @param data Optional additional data to send to the server
	 */
	public Request make_request() throws Exception {
		Request request = new Request(this, 0);
		return request;
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 1) {
			throw new InvalidResponseException(response, "Response data must be at least 1 byte");
		}

		ResponseData service_data = new ResponseData();
		service_data.echo = response.data[0];
		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {
		/**
		 * Requests subfunction echoed back by the server. This value should always be 0
		 */

		public ResponseData() throws Exception {
			super(TesterPresent.class);
			// TODO Auto-generated constructor stub
		}
	}
}
