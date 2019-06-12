package udsoncan.services.base;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Response;

public abstract class BaseService {

	static List<Integer> always_valid_negative_response = new ArrayList<Integer>() {
		{
			add(Response.Code.GeneralReject);
			add(Response.Code.ServiceNotSupported);
			add(Response.Code.ResponseTooLong);
			add(Response.Code.BusyRepeatRequest);
			add(Response.Code.NoResponseFromSubnetComponent);
			add(Response.Code.FailurePreventsExecutionOfRequestedAction);
			add(Response.Code.SecurityAccessDenied);// ISO-14229:2006 Table A.1: "Besides the mandatory use of this
													// negative response code as specified in the applicable services
													// within ISO 14229, this negative response code can also be used
													// for any case where security is required and is not yet granted to
													// perform the required service."
			add(Response.Code.RequestCorrectlyReceived_ResponsePending);
			add(Response.Code.ServiceNotSupportedInActiveSession);
		}
	};

	List<Integer> supported_negative_response;

	public int _sid;
	public boolean _use_subfunction = true;
	public boolean _no_response_data = false;

	// Returns the service ID used for a client request
	public int request_id() {
		return this._sid;
	}

	// Returns the service ID used for a server response
	public int response_id() {
		return this._sid + 0x40;
	}

	// Returns an instance of the service identified by the service ID (Request)
	public void from_request_id(Integer given_id) {
		// TODO: to be implemented later
	}

	// Returns an instance of the service identified by the service ID (Response)
	public void from_response_id(Integer given_id) {
		// TODO: to be implemented later
	}

	// Default subfunction ID for service that does not implement subfunction_id().
	public int subfunction_id() {
		return 0;
	}

	// Tells if this service includes a subfunction byte
	public boolean use_subfunction() {
		return this._use_subfunction;
	}

	public boolean has_response_data() {
		return !_no_response_data;
	}

	// Returns the service name. Shortcut that works on class and instances
	public String get_name() {
		return this.getClass().getName();
	}

	// Tells if the given response code is expected for this service according to
	// UDS standard.
	public boolean is_supported_negative_response(Integer code) {
		boolean supported = false;
		if (this.supported_negative_response.contains(code)) {
			supported = true;
		}
		if (BaseService.always_valid_negative_response.contains(code)) {
			supported = true;
		}

		// As specified by Annex A, negative response code ranging above 0x7F can be
		// used anytime if the service can return ConditionNotCorrect
		if (code >= 0x80 && code < 0xFF
				&& this.supported_negative_response.contains(Response.Code.ConditionsNotCorrect)) {
			supported = true;
		}

		// ISO-14229:2006 Table A.1 : "This response code shall be supported by each
		// diagnostic service with a subfunction parameter"
		if (code == Response.Code.SubFunctionNotSupportedInActiveSession && this.use_subfunction()) {
			supported = true;
		}
		return supported;
	}
	

}
