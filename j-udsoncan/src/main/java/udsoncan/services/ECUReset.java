package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.BaseSubfunction;
import udsoncan.services.base.ServiceHelper;

public class ECUReset extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.SubFunctionNotSupported);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.SecurityAccessDenied);

		}
	};

	// ECUReset defined subfunctions
	public class ResetType extends BaseSubfunction {
		public static final String __pretty_name__ = "reset type";
		public static final int hardReset = 1;
		public static final int keyOffOnReset = 2;
		public static final int softReset = 3;
		public static final int enableRapidPowerShutDown = 4;
		public static final int disableRapidPowerShutDown = 5;
	}

	public ECUReset() {
		this._sid = 0x11;
	}

	/**
	 * Generates a request for ECUReset
	 * 
	 * @param reset_type Service subfunction. Allowed values are from 0 to 0x7F
	 * 
	 */
	public Request make_request(int reset_type) throws Exception {
		ServiceHelper.validate_int(reset_type, 0, 0x7f, "Reset types");
		Request request = new Request(this, reset_type);
		return request;
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 1) {
			throw new InvalidResponseException(response, "Response data must be at least 1 byte");
		}

		ResponseData service_data = new ResponseData();
		service_data.echo = response.data[0];
		if (ResetType.enableRapidPowerShutDown == service_data.echo) {
			if (response.data.length < 2) {
				throw new InvalidResponseException(response,
						"Response data is missing a second byte for reset type \"enableRapidPowerShutDown\"");
			}
			service_data.powerdown_time = response.data[1];
		}

		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(ECUReset.class);
			// TODO Auto-generated constructor stub
		}

		public Byte powerdown_time = null;
	}

}
