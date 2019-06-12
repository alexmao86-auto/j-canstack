package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;

public class DynamicallyDefineDataIdentifier extends BaseService {
	public final int _sid = 0x2C;
	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.SecurityAccessDenied);
		}
	};

	public DynamicallyDefineDataIdentifier() {
		// TODO Auto-generated constructor stub
	}

	public Request make_request() throws Exception {
		throw new Exception("NotImplementedError('Service is not implemented')");
	}

	public void interpret_response(Response response) throws Exception {
		throw new Exception("NotImplementedError('Service is not implemented')");
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(DynamicallyDefineDataIdentifier.class);
			// TODO Auto-generated constructor stub
		}

	}
}
