package udsoncan;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import udsoncan.base.DataFormatIdentifier;
import udsoncan.base.DataIdentifier;
import udsoncan.base.MemoryLocation;
import udsoncan.base.Routine;
import udsoncan.base.Security_algo;
import udsoncan.base.exceptions.ConfigError;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.base.exceptions.TimeoutException;
import udsoncan.base.exceptions.UnexpectedResponseException;
import udsoncan.connections.BaseConnection;
import udsoncan.services.DiagnosticSessionControl;
import udsoncan.services.ECUReset;
import udsoncan.services.ReadDataByIdentifier;
import udsoncan.services.RequestDownload;
import udsoncan.services.RequestTransferExit;
import udsoncan.services.RequestUpload;
import udsoncan.services.RoutineControl;
import udsoncan.services.SecurityAccess;
import udsoncan.services.TesterPresent;
import udsoncan.services.TransferData;
import udsoncan.services.WriteDataByIdentifier;

/**
 * Object that interacts with a UDS server. It builds a service request, sends
 * it to the server, receives and parses its response, detects communication
 * anomalies and logs what it is doing for further debugging.
 */
public class Client {
	public class SuppressPositiveResponse {
		public boolean enabled = false;
	}

	// ---------Fields-----BEGIN--------
	public Logger logger;

	/** The underlying protocol interface. */
	public BaseConnection conn;

	/** The reference to Client_config */
	public Map<String, Object> config;

	/**
	 * Maximum amount of time to wait for a response. This parameter exists for
	 * backward compatibility only. For detailed timeout handling, see :ref:`Client
	 * configuration<config_timeouts>`
	 */
	public int request_timeout;

	public SuppressPositiveResponse suppress_positive_response;

	public Response last_response;
	// ---------Fields------END-------

	public Client(BaseConnection conn, Map<String, Object> config, Integer request_timeout) {
		this.conn = conn;

		this.config = new HashMap<String, Object>();
		if (config != null) {
			this.config.putAll(config);
		} else {
			this.config.putAll(Client_config.default_client_config);
		}

		if (request_timeout != null) {
			// For backward compatibility
			this.request_timeout = request_timeout;
			this.config.put("request_timeout", this.request_timeout);
		}

		this.suppress_positive_response = new SuppressPositiveResponse();
		this.last_response = null;

		this.refresh_config();
	}

	public Client(BaseConnection conn, Map<String, Object> config) {
		this(conn, config, null);
	}

	public Client(BaseConnection conn) {
		this(conn, null, null);
	}

	public void open() {
		if (!this.conn.is_open()) {
			this.conn.open();
		}
	}

	public void close() {
		this.conn.close();
	}

	public void configure_logger() {
		String logger_name;
		if (this.config.containsKey("logger_name")) {
			logger_name = String.format("UdsClient[%s]", this.config.get("logger_name"));
		} else {
			logger_name = "UdsClient";
		}
		this.logger = LoggerFactory.getLogger(logger_name);
	}

	public void set_config(String key, Object value) {
		this.config.put(key, value);
		this.refresh_config();
	}

	public void refresh_config() {
		this.configure_logger();
		for (String k : Client_config.default_client_config.keySet()) {
			if (!this.config.containsKey(k)) {
				this.config.put(k, Client_config.default_client_config.get(k));
			}
		}

	}

	public String service_log_prefix(Object service) {
		if (service instanceof Class)
			return String.format("%s", ((Class) service).getSimpleName());
		return String.format("%s", service.getClass().getSimpleName());
	}

	///////////////////////////////////////////////////////////////////////////

	public Response change_session(int newsession) throws Exception {
		DiagnosticSessionControl service = new DiagnosticSessionControl();
		Request req = service.make_request(newsession);
		String named_newsession = String.format("%s (0x%02x)", DiagnosticSessionControl.Session.get_name(newsession),
				newsession);
		logger.info(String.format("%s - Switching session to %s",
				this.service_log_prefix(DiagnosticSessionControl.class), named_newsession));

		Response response = this.send_request(req);
		if (response == null)
			return null;

		service.interpret_response(response);

		if (newsession != response.service_data.echo) {
			throw new UnexpectedResponseException(response, String.format(
					"Response subfunction received from server (0x%02x) does not match the requested subfunction (0x%02x)",
					response.service_data.echo, newsession));
		}
		return last_response;

	}

	public Response request_seed(int level) throws Exception {
		SecurityAccess service = new SecurityAccess();
		Request req = service.make_request(level, SecurityAccess.Mode.RequestSeed);

		logger.info(String.format("%s - Requesting seed to unlock security access level 0x%02x",
				this.service_log_prefix(DiagnosticSessionControl.class), req.subfunction));

		Response response = this.send_request(req);
		if (response == null)
			return null;

		service.interpret_response(response, SecurityAccess.Mode.RequestSeed);
		int expected_level = SecurityAccess.normalize_level(SecurityAccess.Mode.RequestSeed, level);
		int received_level = response.service_data.echo;
		if (expected_level != received_level) {
			throw new UnexpectedResponseException(response, String.format(
					"Response subfunction received from server (0x%02x) does not match the requested subfunction (0x%02x)",
					received_level, expected_level));
		}
		logger.debug(String.format("Received seed [%s]",
				Arrays.toString(((SecurityAccess.ResponseData) response.service_data).seed)));
		return response;

	}

	public Response send_key(int level, byte[] key) throws Exception {
		SecurityAccess service = new SecurityAccess();
		Request req = service.make_request(level, SecurityAccess.Mode.SendKey, key);

		logger.info(String.format("%s - Sending key to unlock security access level 0x%02x",
				this.service_log_prefix(DiagnosticSessionControl.class), req.subfunction));

		logger.debug(String.format("\tKey to send [%s]", Arrays.toString(key)));

		Response response = this.send_request(req);
		if (response == null)
			return null;

		service.interpret_response(response, SecurityAccess.Mode.SendKey);
		int expected_level = SecurityAccess.normalize_level(SecurityAccess.Mode.RequestSeed, level);
		int received_level = response.service_data.echo;
		if (expected_level != received_level) {
			throw new UnexpectedResponseException(response, String.format(
					"Response subfunction received from server (0x%02x) does not match the requested subfunction (0x%02x)",
					received_level, expected_level));
		}

		return response;
	}

	public Response unlock_security_access(int level) throws Exception {
		if (!this.config.containsKey("security_algo") || !(this.config.get("security_algo") instanceof Security_algo)) {
			throw new Exception("Client configuration does not provide a security algorithm");
		}

		byte[] seed = ((SecurityAccess.ResponseData) this.request_seed(level).service_data).seed;
		Object params = null;
		if (this.config.containsKey("security_algo_params")) {
			params = this.config.get("security_algo_params");
		}
		byte[] key = ((Security_algo) this.config.get("security_algo")).calKey(seed, params);
		return this.send_key(level, key);

	}

	public Response ecu_reset(int reset_type) throws Exception {
		ECUReset service = new ECUReset();
		Request req = service.make_request(reset_type);

		logger.info(String.format("%s - Requesting reset of type 0x%02x (%s)", this.service_log_prefix(service),
				reset_type, ECUReset.ResetType.get_name(ECUReset.ResetType.class, reset_type)));

		Response response = this.send_request(req);
		if (response == null)
			return null;

		service.interpret_response(response);

		if (response.service_data.echo != reset_type) {
			throw new UnexpectedResponseException(response, String.format(
					"Response subfunction received from server (0x%02x) does not match the requested subfunction (0x%02x)",
					response.service_data.echo, reset_type));
		}

		if (response.service_data.echo == ECUReset.ResetType.enableRapidPowerShutDown
				&& ((ECUReset.ResponseData) response.service_data).powerdown_time != 0xFF) {
			logger.info(String.format("Server will shutdown in %d seconds.",
					((ECUReset.ResponseData) response.service_data).powerdown_time));
		}

		return response;
	}

	/** Sends a TesterPresent request to keep the session active. */
	public Response tester_present() throws Exception {
		TesterPresent service = new TesterPresent();
		Request req = service.make_request();
		logger.info(String.format("%s - Sending TesterPresent request", this.service_log_prefix(service)));

		Response response = this.send_request(req);
		if (response == null)
			return null;

		service.interpret_response(response);

		if (response.service_data.echo != req.subfunction.byteValue()) {
			throw new UnexpectedResponseException(response, String.format(
					"Response subfunction received from server (0x%02x) does not match the requested subfunction (0x%02x)",
					response.service_data.echo, req.subfunction.byteValue()));
		}

		return response;

	}

	/**
	 * Requests a value associated with a data identifier (DID) through the
	 * :ref:`ReadDataByIdentifier<ReadDataByIdentifier>` service.
	 */

	// unchecked added for this.config.get("didconfig") map casting
	@SuppressWarnings("unchecked")
	public Response read_data_by_identifier(List<Integer> didlist) throws Exception {
		ReadDataByIdentifier service = new ReadDataByIdentifier();
		didlist = service.validate_didlist_input(didlist);
		Request req = service.make_request(didlist, (Map<Integer, Integer>) this.config.get("data_identifiers"));

		if (didlist.size() == 1) {
			logger.info(String.format("%s - Reading data identifier : 0x%04x (%s)", this.service_log_prefix(service),
					didlist.get(0), DataIdentifier.name_from_id(didlist.get(0))));
		} else {
			logger.info(String.format("%s - Reading %d data identifier : %s", this.service_log_prefix(service),
					didlist.size(), didlist.toString()));
		}

		Response response = this.send_request(req);
		if (response == null)
			return null;

		try {
			service.interpret_response(response, didlist, (Map<Integer, Integer>) this.config.get("didconfig"),
					(Boolean) this.config.get("tolerate_zero_padding"));
		} catch (ConfigError e) {
			if (didlist.contains(e.key)) {
				throw e;
			} else {
				throw new UnexpectedResponseException(response, String.format(
						"Server returned values for data identifier 0x%04x that was not requested and no Codec was defined for it. Parsing must be stopped.",
						e.key));
			}
		}

		Set<Integer> set_request_didlist = new HashSet<Integer>(didlist);
		Set<Integer> set_response_didlist = new HashSet<Integer>(
				((ReadDataByIdentifier.ResponseData) response.service_data).values.keySet());

		Set<Integer> extra_did = new HashSet<Integer>(set_response_didlist);
		extra_did.removeAll(set_request_didlist);
		Set<Integer> missing_did = new HashSet<Integer>(set_request_didlist);
		missing_did.removeAll(set_response_didlist);

		if (extra_did.size() > 0) {
			throw new UnexpectedResponseException(response,
					String.format(
							"Server returned values for %d data identifier that were not requested. Dids are : %s",
							extra_did.size(), extra_did.toString()));
		}

		if (missing_did.size() > 0) {
			throw new UnexpectedResponseException(response,
					String.format("%d data identifier values are missing from server response. Dids are : %s",
							missing_did.size(), missing_did.toString()));
		}

		return response;
	}

	public byte[] read_data_by_identifier_first(List<Integer> didlist) throws Exception {
		ReadDataByIdentifier service = new ReadDataByIdentifier();
		didlist = service.validate_didlist_input(didlist);
		Response response = this.read_data_by_identifier(didlist);
		Collection<Object> values = ((ReadDataByIdentifier.ResponseData) response.service_data).values.values();
		if (values.size() > 0 && didlist.size() > 0) {
			return (byte[]) ((ReadDataByIdentifier.ResponseData) response.service_data).values.entrySet().iterator()
					.next().getValue();
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public Response write_data_by_identifier(int did, Map<Integer, Object> value) throws Exception {
		WriteDataByIdentifier service = new WriteDataByIdentifier();
		Request req = service.make_request(did, value, (Map<Integer, Integer>) this.config.get("data_identifiers"));
		logger.info(String.format("%s - Writing data identifier 0x%04x (%s)", this.service_log_prefix(service), did,
				DataIdentifier.name_from_id(did)));
		Response response = this.send_request(req);
		if (response == null)
			return null;

		service.interpret_response(response);

		if (did != response.service_data.echo) {
			throw new UnexpectedResponseException(response, String.format(
					"Server returned a response for data identifier 0x%04x while client requested for did 0x%04x",
					response.service_data.echo, did));
		}
		return response;
	}

	public Response routine_control(int routine_id, int control_type, byte[] data) throws Exception {
		RoutineControl service = new RoutineControl();
		Request req = service.make_request(routine_id, control_type, data);
		int payload_length = 0;
		if (data != null)
			payload_length = data.length;
		String action;
		switch (control_type) {
		case RoutineControl.ControlType.startRoutine:
			action = "Starting routine ID";
			break;
		case RoutineControl.ControlType.stopRoutine:
			action = "Stoping routine ID";
			break;
		case RoutineControl.ControlType.requestRoutineResults:
			action = "Requesting result for routine ID";
			break;
		default:
			action = "ISOSAEReserved action for routine ID";
			break;
		}

		logger.info(String.format("%s - ControlType=0x%02x - %s 0x%04x (%s) with a payload of %d bytes",
				this.service_log_prefix(service), control_type, action, routine_id, Routine.name_from_id(routine_id),
				payload_length));
		if (data != null) {
			logger.debug(String.format("\tPayload data : %s", Arrays.toString(data)));
		}

		Response response = this.send_request(req);
		if (response == null)
			return null;

		service.interpret_response(response);

		if (control_type != response.service_data.echo) {
			throw new UnexpectedResponseException(response,
					String.format("Control type of response (0x%02x) does not match request control type (0x%02x)",
							response.service_data.echo, control_type));
		}

		if (routine_id != ((RoutineControl.ResponseData) response.service_data).routine_id_echo) {
			throw new UnexpectedResponseException(response, String.format(
					"Response received from server (ID = 0x%04x) does not match the requested routine ID (0x%04x)",
					((RoutineControl.ResponseData) response.service_data).routine_id_echo, routine_id));
		}
		return response;
	}

	public Response start_routine(int routine_id, byte[] data) throws Exception {
		return this.routine_control(routine_id, RoutineControl.ControlType.startRoutine, data);
	}

	public Response stop_routine(int routine_id, byte[] data) throws Exception {
		return this.routine_control(routine_id, RoutineControl.ControlType.stopRoutine, data);
	}

	public Response get_routine_result(int routine_id, byte[] data) throws Exception {
		return this.routine_control(routine_id, RoutineControl.ControlType.requestRoutineResults, data);
	}

	public Response request_upload_download(Class service_cls, MemoryLocation memory_location, DataFormatIdentifier dfi)
			throws Exception {
		dfi = RequestUpload.normalize_data_format_identifier(dfi);

		if (this.config.containsKey("server_address_format")) {
			memory_location.set_format_if_none((Integer) this.config.get("server_address_format"), null);
		}
		if (this.config.containsKey("server_memorysize_format")) {
			memory_location.set_format_if_none(null, (Integer) this.config.get("server_memorysize_format"));
		}

		Method method = service_cls.getMethod("make_request", MemoryLocation.class, DataFormatIdentifier.class);
		Request request = (Request) method.invoke(null, memory_location, dfi);

		String action = "";
		if (service_cls.equals(RequestDownload.class)) {
			action = "Requesting a download (client to server)";
		} else if (service_cls.equals(RequestUpload.class)) {
			action = "Requesting an upload (server to client)";
		}

		logger.info(String.format("%s - %s for memory location [%s] and DataFormatIdentifier 0x%02x (%s)",
				this.service_log_prefix(service_cls), action, memory_location.toString(), dfi.get_byte_as_int(),
				dfi.toString()));

		Response response = this.send_request(request);
		if (response == null)
			return null;

		Object service = service_cls.newInstance();
		method = service_cls.getMethod("interpret_response", Response.class);
		method.invoke(service, response);

		return response;
	}

	public Response request_download(MemoryLocation memory_location, DataFormatIdentifier dfi) throws Exception {
		return this.request_upload_download(RequestDownload.class, memory_location, dfi);
	}

	public Response request_upload(MemoryLocation memory_location, DataFormatIdentifier dfi) throws Exception {
		return this.request_upload_download(RequestUpload.class, memory_location, dfi);
	}

	public Response transfer_data(int sequence_number, byte[] data) throws Exception {
		TransferData service = new TransferData();
		Request request = service.make_request(sequence_number, data);
		int data_len = 0;
		if (data != null)
			data_len = data.length;
		logger.info(String.format("%s - Sending a block of data with SequenceNumber=%d that is %d bytes long.",
				this.service_log_prefix(TransferData.class), sequence_number, data_len));

		if (data != null) {
			logger.debug(String.format("Data to transfer : %s", Arrays.toString(data)));
		}

		Response response = this.send_request(request);
		if (response == null)
			return null;
		service.interpret_response(response);

		if (sequence_number != response.service_data.echo) {
			throw new UnexpectedResponseException(response, String.format(
					"Block sequence number of response (0x%02x) does not match request block sequence number (0x%02x)",
					response.service_data.echo, sequence_number));
		}

		return response;
	}

	public Response request_transfer_exit(byte[] data) throws Exception {
		RequestTransferExit service = new RequestTransferExit();
		Request request = service.make_request(data);
		logger.info(String.format("%s - Sending exit request", this.service_log_prefix(service)));
		Response response = this.send_request(request);
		if (response == null)
			return null;

		service.interpret_response(response);

		return response;
	}
	///////////////////////////////////////////////////////////////////////////

	// Basic transmission of requests. This will need to be improved
	public Response send_request(Request request, int timeout) throws Exception {
		long overall_timeout;
		long single_request_timeout;
		long overall_timeout_time;
		boolean using_p2_star;
		boolean override_suppress_positive_response;
		byte[] payload;
		boolean done_receiving = false;
		Response response = null;

		if (timeout < 0) {
			overall_timeout = (int) this.config.get("request_timeout");
			int p2timeout = (int) this.config.get("p2_timeout");
			single_request_timeout = overall_timeout < p2timeout ? overall_timeout : p2timeout;
		} else {
			overall_timeout = timeout;
			single_request_timeout = timeout;
		}
		overall_timeout_time = System.currentTimeMillis() + overall_timeout;
		using_p2_star = false; // Will switch to true when Nrc 0x78 will be received the first time.

		this.conn.empty_rxqueue();
		this.logger.debug("Sending request to server");
		override_suppress_positive_response = false;
		if (this.suppress_positive_response.enabled && request.service.use_subfunction()) {
			payload = request.get_payload(true);
			override_suppress_positive_response = true;
		} else {
			payload = request.get_payload();
		}

		if (this.suppress_positive_response.enabled && !request.service.use_subfunction()) {
			this.logger.warn(String.format("SuppressPositiveResponse cannot be used for service %s. Ignoring",
					request.service.get_name()));
		}

		this.conn.send(payload);

		if (request.suppress_positive_response || override_suppress_positive_response) {
			return null;
		}

		while (!done_receiving) {
			done_receiving = true;
			this.logger.debug("Waiting for server response");

			String timeout_type_used = "";
			long timeout_value = 0;
			String timeout_name_to_report;
			try {

				if ((System.currentTimeMillis() + single_request_timeout) < overall_timeout_time) {
					timeout_type_used = "single_request";
					timeout_value = single_request_timeout;
				} else {
					timeout_type_used = "overall";
					timeout_value = (int) ((overall_timeout_time - System.currentTimeMillis()));
				}

				payload = this.conn.wait_frame(timeout_value, true);
			} catch (TimeoutException e) {
				if (timeout_type_used.equals("single_request")) {
					if (using_p2_star)
						timeout_name_to_report = "P2* timeout";
					else
						timeout_name_to_report = "P2 timeout";
				} else if (timeout_type_used.equals("overall")) {
					timeout_name_to_report = "Global request timeout";
				} else {
					// Shouldn't go here.
					timeout_name_to_report = "timeout";
				}
				throw new TimeoutException(
						String.format("Did not receive response in time. %s time has expired (timeout=%d msec)",
								timeout_name_to_report, timeout_value));
			} catch (Exception e) {
				throw e;
			}

			response = Response.from_payload(payload);
			this.last_response = response;
			this.logger.debug("Received response from server");

			if (!response.valid) {
				throw new InvalidResponseException(response);
			}

			if (response.service.response_id() != request.service.response_id()) {
				String msg = String.format(
						"\"Response gotten from server has a service ID different than the request service ID. Received=0x%02x, Expected=0x%02x\"",
						response.service.response_id(), request.service.response_id());
				throw new UnexpectedResponseException(response, msg);
			}

			if (!response.positive) {
				if (!request.service.is_supported_negative_response(response.code)) {
					this.logger.warn(String.format(
							"Given response code \"%s\" (0x%02x) is not a supported negative response code according to UDS standard.",
							response.code_name, response.code));

				}

				if (Response.Code.RequestCorrectlyReceived_ResponsePending == response.code) {
					done_receiving = false;
					if (!using_p2_star) {
						// Received a 0x78 NRC: timeout is now set to P2*
						single_request_timeout = (long) this.config.get("p2_star_timeout");
						using_p2_star = true;
						this.logger.debug(String.format(
								"\"Server requested to wait with response code %s (0x%02x), single request timeout is now set to P2* (%d milliseconds)",
								response.code_name, response.code, single_request_timeout));
					}
				}
			}
		}

		logger.info(String.format("Received positive response for service %s (0x%02x) from server.",
				response.service.get_name(), response.service.request_id()));
		return response;
	}

	public Response send_request(Request request) throws Exception {
		return this.send_request(request, -1);
	}

}
