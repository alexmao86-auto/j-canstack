package udsoncan.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.BaseSubfunction;
import udsoncan.services.base.ServiceHelper;

public class RoutineControl extends BaseService {
	
	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.SubFunctionNotSupported);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestSequenceError);
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.SecurityAccessDenied);
			add(Response.Code.GeneralProgrammingFailure);

		}
	};

	// RoutineControl defined subfunctions
	public class ControlType extends BaseSubfunction {
		String __pretty_name__ = "control type";
		public static final int startRoutine = 1;
		public static final int stopRoutine = 2;
		public static final int requestRoutineResults = 3;
	}

	public RoutineControl() {
		 _sid = 0x31;
	}

	/**
	 * Generates a request for RoutineControl
	 * 
	 * @param reset_type Service subfunction. Allowed values are from 0 to 0x7F
	 * 
	 */
	public Request make_request(int routine_id, int control_type, byte[] data) throws Exception {
		ServiceHelper.validate_int(routine_id, 0, 0xffff, "Routine ID");
		ServiceHelper.validate_int(control_type, 0, 0x7f, "Routine control type");
		Request request = new Request(this, control_type);
		List<Byte> reqData = new ArrayList<Byte>();
		reqData.add((byte) ((routine_id & 0xFF00) >> 4));
		reqData.add((byte) ((routine_id & 0x00FF) >> 0));
		if (data != null) {
			for (byte b : data)
				reqData.add(b);
		}
		request.data = new byte[reqData.size()];
		for (int i = 0; i < reqData.size(); i++) {
			request.data[i] = reqData.get(i);
		}
		return request;
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 3) {
			throw new InvalidResponseException(response, "Response data must be at least 3 bytes");
		}

		ResponseData service_data = new ResponseData();
		service_data.echo = response.data[0];
		service_data.routine_id_echo = ((int) response.data[1]) << 4 + response.data[2];
		if (response.data.length > 3) {
			service_data.routine_status_record = Arrays.copyOfRange(response.data, 3, response.data.length);
		}
		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(RoutineControl.class);
			// TODO Auto-generated constructor stub
		}

		public Integer routine_id_echo = null;
		public byte[] routine_status_record = null;
	}
}
