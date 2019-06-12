package udsoncan.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.DidCodec;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.ServiceHelper;

public class WriteDataByIdentifier extends BaseService {
	

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.SecurityAccessDenied);
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.GeneralProgrammingFailure);
		}
	};

	public WriteDataByIdentifier() {
		_sid = 0x2E;
		_use_subfunction = false;
	}

	/**
	 * Generates a request for WriteDataByIdentifier
	 * 
	 * @param reset_type Service subfunction. Allowed values are from 0 to 0x7F
	 * 
	 */
	public Request make_request(int did, Map<Integer, Object> value, Map<Integer, Integer> didconfig) throws Exception {
		ServiceHelper.validate_int(did, 0, 0xFFFF, "Data Identifier");
		Request request = new Request(this);
		didconfig = ServiceHelper.check_did_config(did, didconfig);
		// Make sure all DIDs are correctly defined in client config
		List<Byte> reqData = new ArrayList<Byte>();
		// encode DID number
		reqData.add((byte) ((did & 0xff00) >> 4));
		reqData.add((byte) ((did & 0x00ff) >> 0));

		DidCodec codec = DidCodec.from_config(didconfig.get(did));
		byte[] codedVal = codec.encode(value);

		request.data = new byte[reqData.size() + codedVal.length];
		for (int i = 0; i < reqData.size(); i++) {
			request.data[i] = reqData.get(i);
		}
		for (int i = 0; i < codedVal.length; i++) {
			request.data[i + reqData.size()] = codedVal[i];
		}
		return request;
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 2) {
			throw new InvalidResponseException(response, "Response data must be at least 2 bytes");
		}

		ResponseData service_data = new ResponseData();
		service_data.did_echo = ((int) response.data[0]) << 4 + response.data[1];
		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {
		/**
		 * Requests subfunction echoed back by the server. This value should always be 0
		 */
		public Integer did_echo = null;

		public ResponseData() throws Exception {
			super(WriteDataByIdentifier.class);
			// TODO Auto-generated constructor stub
		}
	}

}
