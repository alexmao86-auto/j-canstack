package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.ServiceHelper;

public class ClearDiagnosticInformation extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestOutOfRange);
		}
	};

	public ClearDiagnosticInformation() {
		this._sid = 0x14;
		this._use_subfunction = false;
		this._no_response_data = true;
	}

	/**
	 * Generates a request for ClearDiagnosticInformation
	 * 
	 * @param group DTC mask ranging from 0 to 0xFFFFFF. 0xFFFFFF means all DTCs
	 */
	public Request make_request(int group) throws Exception {
		ServiceHelper.validate_int(group, 0, 0xFFFFFF, "Group of DTC");

		Request request = new Request(this);

		byte hb = (byte) ((group >> 16) & 0xFF);
		byte mb = (byte) ((group >> 8) & 0xFF);
		byte lb = (byte) ((group >> 0) & 0xFF);
		request.data = new byte[] { hb, mb, lb };

		return request;

	}

	public void interpret_response(Response response) throws Exception {
		response.service_data = new ResponseData();
	}

	class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(ClearDiagnosticInformation.class);
		}

	}
}
