package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;

public class SecuredDataTransmission extends BaseService {
	public final int _sid = 0x84;
	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.SubFunctionNotSupported);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.GeneralSecurityViolation);
			add(Response.Code.SecuredModeRequested);
			add(Response.Code.InsufficientProtection);
			add(Response.Code.TerminationWithSignatureRequested);
			add(Response.Code.AccessDenied);
			add(Response.Code.VersionNotSupported);
			add(Response.Code.SecuredLinkNotSupported);
			add(Response.Code.CertificateNotAvailable);
			add(Response.Code.AuditTrailInformationNotAvailable);
		}
	};

	public SecuredDataTransmission() {
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
			super(SecuredDataTransmission.class);
			// TODO Auto-generated constructor stub
		}

	}
}
