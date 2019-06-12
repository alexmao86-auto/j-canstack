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

public class DiagnosticSessionControl extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.SubFunctionNotSupported);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
		}
	};

	// DiagnosticSessionControl defined subfunctions
	public class Session extends BaseSubfunction {
		String __pretty_name__ = "session";
		public static final int defaultSession = 1;
		public static final int programmingSession = 2;
		public static final int extendedDiagnosticSession = 3;
		public static final int safetySystemDiagnosticSession = 4;
	}

	public DiagnosticSessionControl() {
		this._sid = 0x10;
	}

	/**
	 * Generates a request for DiagnosticSessionControl service
	 * 
	 * @param session Service subfunction. Allowed values are from 0 to 0x7F
	 */
	public Request make_request(int session) throws Exception {
		ServiceHelper.validate_int(session, 0, 0x7F, "Session number");

		return new Request(this, session);
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 1) {
			throw new InvalidResponseException(response, "Response data must be at least 1 bytes");
		}
		ResponseData service_data = new ResponseData();
		service_data.echo = response.data[0];
		if (response.data.length > 1) {
			service_data.session_param_records = new byte[response.data.length - 1];
			for (int i = 0; i < service_data.session_param_records.length; i++) {
				service_data.session_param_records[i] = response.data[i + 1];
			}
		}
		response.service_data = service_data;

		// return response; //???
	}

	class ResponseData extends BaseResponseData {

		public byte[] session_param_records = null;

		public ResponseData() throws Exception {
			super(DiagnosticSessionControl.class);
		}

	}

}
