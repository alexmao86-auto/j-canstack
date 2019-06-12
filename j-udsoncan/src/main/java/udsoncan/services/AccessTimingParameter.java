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

public class AccessTimingParameter extends BaseService {
	public AccessTimingParameter() {
		this._sid = 0x83;
	}

	// AccessTimingParameter defined subfunctions
	class AccessType extends BaseSubfunction {
		String __pretty_name__ = "access type";
		public static final int readExtendedTimingParameterSet = 1;
		public static final int setTimingParametersToDefaultValues = 2;
		public static final int readCurrentlyActiveTimingParameters = 3;
		public static final int setTimingParametersToGivenValues = 4;
	}

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.SubFunctionNotSupported);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestOutOfRange);

		}
	};

	/**
	 * Generates a request for AccessTimingParameter
	 * 
	 * @param access_type         Service subfunction. Allowed values are from 0 to
	 *                            0x7F
	 * @param timing_param_record Data associated with request. Must be present only
	 *                            when
	 *                            access_type="AccessType.setTimingParametersToGivenValues"
	 *                            (4)
	 */
	public Request make_request(int access_type, byte[] timing_param_record) throws Exception {

		ServiceHelper.validate_int(access_type, 0, 0x7f, "Access types");

		if (timing_param_record != null && access_type != AccessType.setTimingParametersToGivenValues) {
			throw new Exception(
					"timing_param_record can only be set when access_type is \"setTimingParametersToGivenValues\"");
		}
		if (timing_param_record == null && access_type == AccessType.setTimingParametersToGivenValues) {
			throw new Exception(
					"A timing_param_record must be provided when access_type is \"setTimingParametersToGivenValues\"");
		}

		Request request = new Request(this, access_type, false, null);

		if (timing_param_record != null) {
			request.data = timing_param_record;
		}
		return request;
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 1) {
			throw new InvalidResponseException(response, "Response data must be at least 1 byte");
		}

		ResponseData service_data = new ResponseData();
		service_data.access_type_echo = response.data[0];
		if (response.data.length > 1) {
			service_data.timing_param_record = new byte[response.data.length - 1];
			for (int i = 0; i < service_data.timing_param_record.length; i++) {
				service_data.timing_param_record[i] = response.data[i + 1];
			}
		}
		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(AccessTimingParameter.class);
			// TODO Auto-generated constructor stub
		}

		public Byte access_type_echo = null;
		public byte[] timing_param_record = null;
	}
}
