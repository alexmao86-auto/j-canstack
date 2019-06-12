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

public class ControlDTCSetting extends BaseService {
	public final int _sid = 0x85;
	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.SubFunctionNotSupported);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.SecurityAccessDenied);

		}
	};

	// ControlDTCSetting defined subfunctions
	class SettingType extends BaseSubfunction {
		String __pretty_name__ = "setting type";
		public static final int on = 1;
		public static final int off = 2;
//		vehicleManufacturerSpecific = (0x40, 0x5F)	# To be able to print textual name for logging only.
//		systemSupplierSpecific = (0x60, 0x7E)		# To be able to print textual name for logging only.

	}

	public ControlDTCSetting() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Generates a request for ControlDTCSetting
	 * 
	 * @param setting_type Service subfunction. Allowed values are from 0 to 0x7F
	 * @param data         Optional additional data sent with the request called
	 *                     `DTCSettingControlOptionRecord`
	 */
	public Request make_request(int setting_type, byte[] data) throws Exception {
		ServiceHelper.validate_int(setting_type, 0, 0x7f, "Setting type");
		Request request = new Request(this, setting_type, data);
		return request;
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 1) {
			throw new InvalidResponseException(response, "Response data must be at least 1 byte");
		}

		ResponseData service_data = new ResponseData();
		service_data.setting_type_echo = response.data[0];

		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(ControlDTCSetting.class);
			// TODO Auto-generated constructor stub
		}

		public Byte setting_type_echo = null;
	}

}
