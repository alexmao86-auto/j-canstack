package udsoncan;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.ServiceFactory;

/**
 * Represents a server Response to a client Request
 * 
 * :param service: The service implied by this response. :type service: class
 * 
 * :param code: The response code :type code: int
 * 
 * :param data: The response data encoded after the service and response code
 * :type data: bytes
 * 
 * .. data:: valid
 * 
 * (boolean) True if the response content is valid. Only ``invalid_reason`` is
 * guaranteed to have a meaningful value if this value is False
 * 
 * .. data:: invalid_reason
 * 
 * (string) String explaining why the response is invalid.
 * 
 * .. data:: service
 * 
 * (class) The response target :ref:`service<Services>` class
 * 
 * .. data:: positive
 * 
 * (boolean) True if the response code is 0 (PositiveResponse), False otherwise
 * 
 * .. data:: code
 * 
 * (int) The response code.
 * 
 * .. data:: code_name
 * 
 * (string) The response code name.
 * 
 * 
 * .. data:: data
 * 
 * (bytes) The response data. All the payload content, except the service number
 * and the response code
 * 
 * 
 * .. data:: service_data
 * 
 * (object) The content of ``data`` interpreted by a service; can be any type of
 * content.
 * 
 * 
 * .. data:: original_payload
 * 
 * (bytes) When the response is built with `Response.from_payload`, this
 * property contains a copy of the payload used. None otherwise.
 * 
 * .. data:: unexpected
 * 
 * (boolean) Indicates that the response was unexpected. Set by an external
 * source such as the :ref:`Client<Client>` object
 * 
 */
public class Response {
	public static class Code {
		public static final int PositiveResponse = 0;
		public static final int GeneralReject = 0x10;
		public static final int ServiceNotSupported = 0x11;
		public static final int SubFunctionNotSupported = 0x12;
		public static final int IncorrectMessageLegthOrInvalidFormat = 0x13;
		public static final int ResponseTooLong = 0x14;
		public static final int BusyRepeatRequest = 0x21;
		public static final int ConditionsNotCorrect = 0x22;
		public static final int RequestSequenceError = 0x24;
		public static final int NoResponseFromSubnetComponent = 0x25;
		public static final int FailurePreventsExecutionOfRequestedAction = 0x26;
		public static final int RequestOutOfRange = 0x31;
		public static final int SecurityAccessDenied = 0x33;
		public static final int InvalidKey = 0x35;
		public static final int ExceedNumberOfAttempts = 0x36;
		public static final int RequiredTimeDelayNotExpired = 0x37;
		public static final int UploadDownloadNotAccepted = 0x70;
		public static final int TransferDataSuspended = 0x71;
		public static final int GeneralProgrammingFailure = 0x72;
		public static final int WrongBlockSequenceCounter = 0x73;
		public static final int RequestCorrectlyReceived_ResponsePending = 0x78;
		public static final int SubFunctionNotSupportedInActiveSession = 0x7E;
		public static final int ServiceNotSupportedInActiveSession = 0x7F;
		public static final int RpmTooHigh = 0x81;
		public static final int RpmTooLow = 0x82;
		public static final int EngineIsRunning = 0x83;
		public static final int EngineIsNotRunning = 0x84;
		public static final int EngineRunTimeTooLow = 0x85;
		public static final int TemperatureTooHigh = 0x86;
		public static final int TemperatureTooLow = 0x87;
		public static final int VehicleSpeedTooHigh = 0x88;
		public static final int VehicleSpeedTooLow = 0x89;
		public static final int ThrottlePedalTooHigh = 0x8A;
		public static final int ThrottlePedalTooLow = 0x8B;
		public static final int TransmissionRangeNotInNeutral = 0x8C;
		public static final int TransmissionRangeNotInGear = 0x8D;
		public static final int ISOSAEReserved = 0x8E;
		public static final int BrakeSwitchNotClosed = 0x8F;
		public static final int ShifterLeverNotInPark = 0x90;
		public static final int TorqueConverterClutchLocked = 0x91;
		public static final int VoltageTooHigh = 0x92;
		public static final int VoltageTooLow = 0x93;

		// Defined by ISO-15764. Offset of 0x38 is defined within UDS standard
		// (ISO-14229)
		public static final int GeneralSecurityViolation = 0x38 + 0;
		public static final int SecuredModeRequested = 0x38 + 1;
		public static final int InsufficientProtection = 0x38 + 2;
		public static final int TerminationWithSignatureRequested = 0x38 + 3;
		public static final int AccessDenied = 0x38 + 4;
		public static final int VersionNotSupported = 0x38 + 5;
		public static final int SecuredLinkNotSupported = 0x38 + 6;
		public static final int CertificateNotAvailable = 0x38 + 7;
		public static final int AuditTrailInformationNotAvailable = 0x38 + 8;

		// Returns the name of the response code as a string
		public static String get_name(Integer given_id) {
			if (given_id == null)
				return "Invalid id";
			Class<Code> clazz = Code.class;
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				try {
					if (field.getInt(clazz) == given_id) {
						return field.getName();
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
			return "Unknown id";
		}

		// Tells if a code is a negative code
		public static boolean is_negative(Integer given_id) throws IllegalAccessException {
			if (given_id == null || given_id == Code.PositiveResponse) {
				return false;
			}
			Class<Code> clazz = Code.class;
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (field.getInt(clazz) == given_id) {
					return true;
				}
			}
			return false;
		}
	}

	public BaseService service;
	public boolean positive;
	public int code;
	public byte[] data = new byte[] {};
	public String code_name;
	public boolean valid;
	public String invalid_reason;
	public BaseResponseData service_data;
	public byte[] original_payload;
	public boolean unexpected;

	public Response(BaseService service, Integer code, byte[] data) throws Exception {
		this.service = service;
		if (code != null) {
			if (code < 0 || code > 0xFF) {
				throw new Exception("Response code must be an integer between 0 and 0xFF");
			}
			this.code = code;
			this.code_name = Response.Code.get_name(code);
			if (!Response.Code.is_negative(code)) {
				this.positive = true;
			}
		}
		if (data != null) {
			this.data = data;
		}
		if (this.service != null) {
			this.valid = true;
			this.invalid_reason = "";
		} else {
			this.valid = false;
			this.invalid_reason = "Object not initialized";
		}

		this.unexpected = false;
	}

	public Response(BaseService service, int code) throws Exception {
		this(service, code, null);
	}

	public Response() throws Exception {
		this(null, null, null);
	}

	// used by server
	/**
	 * Generates a payload to be given to the underlying protocol. This method is
	 * meant to be used by a UDS server
	 * 
	 * :return: A payload to be sent through the underlying protocol :rtype: bytes
	 */
	public byte[] get_payload() {

		List<Byte> payload = new ArrayList<>();
		if (this.positive) {
			payload.add((byte) this.service.response_id());
		} else {
			payload.add((byte) 0x7F);
			payload.add((byte) this.service.request_id());
			payload.add((byte) this.code);
		}

		if (this.data != null && this.service.has_response_data()) {
			for (byte b : this.data) {
				payload.add(b);
			}
		}

		byte[] byteArray = new byte[payload.size()];
		for (int i = 0; i < payload.size(); i++)
			byteArray[i] = payload.get(i);
		return byteArray;
	}

	// Analyzes a TP frame and builds a Response object. Used by client
	// from_payload
	/**
	 * Creates a ``Response`` object from a payload coming from the underlying
	 * protocol. This method is meant to be used by a UDS client
	 * 
	 * :param payload: The payload of data to parse :type payload: bytes
	 * 
	 * :return: A :ref:`Response<Response>` object with populated fields :rtype:
	 * :ref:`Response<Response>`
	 * 
	 * @throws Exception
	 */
	public static Response from_payload(byte[] payload) throws Exception {
		Response response = new Response();

		response.original_payload = payload;// may be useful for debugging

		if (payload.length < 1) {
			response.valid = false;
			response.invalid_reason = "Payload is empty";
			return response;
		}
		int data_start = 0;
		if (payload[0] != 0x7F) {
			// positive
			response.service = ServiceFactory.getService(payload[0]);
			if (response.service == null) {
				response.valid = false;
				response.invalid_reason = "Payload first byte is not a know service response ID.";
				return response;
			}

			data_start = 1;
			response.positive = true;
			if (payload.length < (data_start + 1) && response.service.has_response_data()) {
				response.valid = false;
				response.positive = false;
				response.invalid_reason = "Payload must be at least 2 bytes long (service and response)";
				return response;
			}
			response.code = Response.Code.PositiveResponse;
			response.code_name = Response.Code.get_name(response.code);

		} else {// negative
			response.positive = false;
			data_start = 3;

			if (payload.length < 2) {
				response.valid = false;
				response.invalid_reason = "Incomplete invalid response service (7Fxx)";
				return response;
			}

			response.service = ServiceFactory.getService(payload[1]);// Request id, not response id

			if (response.service == null) {
				response.valid = false;
				response.invalid_reason = "Payload first byte is not a know service response ID.";
				return response;
			}

			if (payload.length < data_start) {
				response.valid = false;
				response.invalid_reason = "Response code missing";
				return response;
			}
			response.code = payload[2];
			response.code_name = Response.Code.get_name(response.code);
		}
		response.valid = true;
		response.invalid_reason = "";
		if (payload.length > data_start) {
			response.data = Arrays.copyOfRange(payload, data_start, payload.length);
		}

		return response;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("<");
		if (this.positive) {
			builder.append(Response.Code.get_name(Response.Code.PositiveResponse));
		} else {
			builder.append("NegativeResponse(");
			builder.append(this.code_name);
			builder.append("NegativeResponse)");
		}
		builder.append(this.data.length);
		builder.append(" data bytes at 0x");
		builder.append(String.format("%x>", this.hashCode()));
		return builder.toString();
	}

	public int length() {
		return this.get_payload().length;
	}

}
