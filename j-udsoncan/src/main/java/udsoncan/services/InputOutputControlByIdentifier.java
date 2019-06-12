package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.IOMasks;
import udsoncan.base.IOValues;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.BaseSubfunction;
import udsoncan.services.base.ServiceHelper;

public class InputOutputControlByIdentifier extends BaseService {
	public final int _sid = 0x2F;
	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.SecurityAccessDenied);
		}
	};

	// InputOutputControlByIdentifier defined control parameters as defined by
	// ISO-14229:2006, Annex E
	class ControlParam extends BaseSubfunction {
		String __pretty_name__ = "control parameter";
		public static final int returnControlToECU = 0;
		public static final int resetToDefault = 1;
		public static final int freezeCurrentState = 2;
		public static final int shortTermAdjustment = 3;
	}

	public InputOutputControlByIdentifier() {
		// TODO Auto-generated constructor stub
	}

	public Request make_request(int did, Byte control_param, IOValues values, IOMasks masks, Integer ioconfig)
			throws Exception {
		ServiceHelper.validate_int(did, 0, 0xffff, "DID");

		if (control_param != null) {
			if (control_param < 0 || control_param > 3) {
				throw new Exception(String.format(
						"control_param must either be returnControlToECU(0), resetToDefault(1), freezeCurrentState(2), shortTermAdjustment(3). %d given.",
						control_param));
			}
		}

		// value and masks to IOValues/IOMasks type, in Java strong type language, no
		// need

		if (values == null && masks != null) {
			throw new Exception("An IOValue must be given if a IOMask is provided.");
		}

		Request request = new Request(this);

		List<Byte> reqData = new ArrayList<Byte>();
		reqData.add((byte) ((did & 0xff00) >> 4));
		reqData.add((byte) ((did & 0x00ff) >> 0));
		// This parameter is optional according to standard
		if (control_param != null) {
			reqData.add(control_param);
		}

		request.data = new byte[reqData.size()];
		for (int i = 0; i < reqData.size(); i++) {
			request.data[i] = reqData.get(i);
		}
		return request;
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(InputOutputControlByIdentifier.class);
			// TODO Auto-generated constructor stub
		}

		public Byte did_echo = null;
		public Byte control_param_echo = null;
		public Byte decoded_data = null;
	}
}
