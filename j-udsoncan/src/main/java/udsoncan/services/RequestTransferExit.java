package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;

public class RequestTransferExit extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.RequestSequenceError);
		}
	};

	public RequestTransferExit() {
		_sid = 0x37;
		_use_subfunction = false;
		_no_response_data = true;
	}

	/**
	 * Generates a request for RequestTransferExit
	 * 
	 * 
	 * @param data Optional additional data to send to the server
	 */
	public Request make_request(byte[] data) throws Exception {
		Request request = new Request(this, data);
		return request;
	}

	public void interpret_response(Response response) throws Exception {

		ResponseData service_data = new ResponseData();
		service_data.parameter_records = response.data;
		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public byte[] parameter_records = null;

		public ResponseData() throws Exception {
			super(RequestTransferExit.class);
			// TODO Auto-generated constructor stub
		}

	}
}
