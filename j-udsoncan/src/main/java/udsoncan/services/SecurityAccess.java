package udsoncan.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.ServiceHelper;

public class SecurityAccess extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.SubFunctionNotSupported);
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestSequenceError);
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.InvalidKey);
			add(Response.Code.ExceedNumberOfAttempts);
			add(Response.Code.RequiredTimeDelayNotExpired);
		}
	};

	public class Mode {
		public static final int RequestSeed = 0;
		public static final int SendKey = 1;
	}

	public static int normalize_level(int mode, int level) throws Exception {
		validate_mode(mode);
		ServiceHelper.validate_int(level, 0, 0x7f, "Security level");

		if (Mode.RequestSeed == mode) {
			if ((level % 2) != 1) {
				return level - 1;
			}
		} else if (Mode.SendKey == mode) {
			if ((level % 2) != 0) {
				return level + 1;
			}
		}
		return level;
	}

	/**
	 * Generates a request for SecurityAccess
	 * 
	 * @param level Service subfunction. The security level to unlock. Value ranging
	 *              from 0 to 7F For mode=``RequestSeed`` (0), level must be an even
	 *              value. For mode=``SendKey`` (1), level must be an odd value. If
	 *              the even/odd constraint is not respected, the level value will
	 *              be corrected to properly set the LSB.
	 * @param mode  Type of request to perform. ``SecurityAccess.Mode.RequestSeed``
	 *              or ``SecurityAccess.Mode.SendKey``
	 * @param key   When mode=``SendKey``, this value must be provided.
	 * 
	 */
	public Request make_request(int level, int mode, byte[] key) throws Exception {
		validate_mode(mode);
		ServiceHelper.validate_int(level, 0, 0x7f, "Security level");
		Request request = new Request(this, this.normalize_level(mode, level));
		if (Mode.SendKey == mode) {
			request.data = key;
		}
		return request;
	}

	public Request make_request(int level, int mode) throws Exception {
		return this.make_request(level, mode, null);
	}

	private static void validate_mode(int mode) throws Exception {
		if (Mode.RequestSeed != mode && Mode.SendKey != mode) {
			throw new Exception("Given mode must be either be RequestSeed (0) or SendKey (1).");
		}

	}

	public void interpret_response(Response response, int mode) throws Exception {
		validate_mode(mode);
		int minlength = 2;
		if (Mode.RequestSeed != mode) {
			minlength = 1;
		}
		if (response.data.length < minlength) {
			throw new InvalidResponseException(response, "Response data must be at least " + minlength + " byte");
		}

		ResponseData service_data = new ResponseData();
		service_data.echo = response.data[0];
		if (Mode.RequestSeed == mode) {

			service_data.seed = Arrays.copyOfRange(response.data, 1, response.data.length);
		}

		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(SecurityAccess.class);
			// TODO Auto-generated constructor stub
		}

		public byte[] seed = null;
	}

	public SecurityAccess() {
		_sid = 0x27;
	}

}
