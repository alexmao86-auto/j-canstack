package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.CommunicationType;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.BaseSubfunction;
import udsoncan.services.base.ServiceHelper;

public class CommunicationControl extends BaseService {
	

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.SubFunctionNotSupported);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestOutOfRange);

		}
	};

	// CommunicationControl defined subfunctions
	class ControlType extends BaseSubfunction {
		String __pretty_name__ = "control type";
		public static final int enableRxAndTx = 0;
		public static final int enableRxAndDisableTx = 1;
		public static final int disableRxAndEnableTx = 2;
		public static final int disableRxAndTx = 3;
	}

	public CommunicationControl() {
		this._sid = 0x28;
	}

	public CommunicationType normalize_communication_type(int communication_type) throws Exception {
		return CommunicationType.from_byte((byte) communication_type);
	}

	/**
	 * Generates a request for CommunicationControl
	 * 
	 * @param control_type       Service subfunction. Allowed values are from 0 to
	 *                           0x7F
	 * @param communication_type The communication type requested.
	 */
	public Request make_request(int control_type, CommunicationType communication_type) throws Exception {
		ServiceHelper.validate_int(control_type, 0, 0x7f, "Control type");
		Request request = new Request(this, control_type);
		request.data = new byte[1];
		request.data[0] = communication_type.get_byte();
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
			super(CommunicationControl.class);
			// TODO Auto-generated constructor stub
		}

		public Byte control_type_echo = null;
	}

}
