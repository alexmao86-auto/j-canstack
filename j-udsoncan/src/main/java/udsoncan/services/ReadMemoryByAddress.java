package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.MemoryLocation;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;

public class ReadMemoryByAddress extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.SecurityAccessDenied);
		}
	};

	public ReadMemoryByAddress() {
		this._sid = 0x23;
		this._use_subfunction = false;
	}

	/**
	 * Generates a request for ReadMemoryByAddress
	 * 
	 * 
	 * @param data Optional additional data to send to the server
	 */
	public Request make_request(MemoryLocation memory_location) throws Exception {
		Request request = new Request(this);
		List<Byte> reqData = new ArrayList<Byte>();
		reqData.add(memory_location.alfid.get_byte());
		for (byte b : memory_location.get_address_bytes())
			reqData.add(b);
		for (byte b : memory_location.get_memorysize_bytes())
			reqData.add(b);
		request.data = new byte[reqData.size()];
		for (int i = 0; i < reqData.size(); i++) {
			request.data[i] = reqData.get(i);
		}
		return request;
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 1) {
			throw new InvalidResponseException(response, "Response data must be at least 1 byte");
		}

		ResponseData service_data = new ResponseData();
		service_data.memory_block = response.data;
		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public byte[] memory_block = null;

		public ResponseData() throws Exception {
			super(ReadMemoryByAddress.class);
			// TODO Auto-generated constructor stub
		}
	}
}
