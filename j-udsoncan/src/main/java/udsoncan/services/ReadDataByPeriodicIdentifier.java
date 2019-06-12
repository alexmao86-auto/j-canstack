package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;

public class ReadDataByPeriodicIdentifier extends BaseService {
	public final int _sid = 0x2A;
	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.SecurityAccessDenied);
		}
	};

	public Request make_request() throws Exception {
		throw new Exception("NotImplementedError('Service is not implemented')");
	}

	public void interpret_response(Response response) throws Exception {
		throw new Exception("NotImplementedError('Service is not implemented')");
	}

	public ReadDataByPeriodicIdentifier() {
		// TODO Auto-generated constructor stub
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(ReadDataByPeriodicIdentifier.class);
			// TODO Auto-generated constructor stub
		}

	}

}
