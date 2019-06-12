package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.Baudrate;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.BaseSubfunction;
import udsoncan.services.base.ServiceHelper;

public class LinkControl extends BaseService {
	public final int _sid = 0x87;
	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestSequenceError);
			add(Response.Code.RequestOutOfRange);
		}
	};

	// LinkControl defined subfunctions
	class ControlType extends BaseSubfunction {
		String __pretty_name__ = "control type";
		public static final int verifyBaudrateTransitionWithFixedBaudrate = 1;
		public static final int verifyBaudrateTransitionWithSpecificBaudrate = 2;
		public static final int transitionBaudrate = 3;
	}

	public LinkControl() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Generates a request for LinkControl
	 * 
	 * @param control_type Service subfunction. Allowed values are from 0 to 0x7F
	 * @param baudrate     Required baudrate value when ``control_type`` is either
	 *                     ``verifyBaudrateTransitionWithFixedBaudrate`` (1) or
	 *                     ``verifyBaudrateTransitionWithSpecificBaudrate`` (2)
	 */
	public Request make_request(int control_type, Baudrate baudrate) throws Exception {
		ServiceHelper.validate_int(control_type, 0, 0x7f, "Control types");

		if (ControlType.verifyBaudrateTransitionWithSpecificBaudrate == control_type
				|| ControlType.verifyBaudrateTransitionWithFixedBaudrate == control_type) {
			if (baudrate == null) {
				throw new Exception(String.format(
						"A Baudrate must be provided with control type : \"verifyBaudrateTransitionWithSpecificBaudrate\" (0x%02x) or \"verifyBaudrateTransitionWithFixedBaudrate\" (0x%02x)",
						ControlType.verifyBaudrateTransitionWithSpecificBaudrate,
						ControlType.verifyBaudrateTransitionWithFixedBaudrate));
			} else {
				if (baudrate != null) {
					throw new Exception(String.format(
							"The baudrate parameter is only needed when control type is \"verifyBaudrateTransitionWithSpecificBaudrate\" (0x%02x) or \"verifyBaudrateTransitionWithFixedBaudrate\" (0x%02x)'",
							ControlType.verifyBaudrateTransitionWithSpecificBaudrate,
							ControlType.verifyBaudrateTransitionWithFixedBaudrate));
				}
			}
		}
		if (ControlType.verifyBaudrateTransitionWithSpecificBaudrate == control_type) {
			baudrate = baudrate.make_new_type(Baudrate.Type.Specific);
		}
		if (ControlType.verifyBaudrateTransitionWithFixedBaudrate == control_type
				&& baudrate.baudtype == Baudrate.Type.Specific) {
			baudrate = baudrate.make_new_type(Baudrate.Type.Fixed);
		}

		Request request = new Request(this, control_type);
		if (baudrate != null) {
			request.data = baudrate.get_bytes();
		}
		return request;
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 1) {
			throw new InvalidResponseException(response, "Response data must be at least 1 byte");
		}

		ResponseData service_data = new ResponseData();
		service_data.control_type_echo = response.data[0];

		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(LinkControl.class);
			// TODO Auto-generated constructor stub
		}

		public Byte control_type_echo = null;
	}

}
