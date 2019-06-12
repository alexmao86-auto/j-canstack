package udsoncan;

import java.util.HashMap;
import java.util.Map;

public class Client_config {

	public static Map<String, Object> default_client_config = new HashMap<String, Object>() {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		{
			put("exception_on_negative_response", true);
			put("exception_on_invalid_response", true);
			put("exception_on_unexpected_response", true);
			put("security_algo", null);
			put("security_algo_params", null);
			put("tolerate_zero_padding", true);
			put("ignore_all_zero_dtc", true);
			put("dtc_snapshot_did_size", 2);
			put("server_address_format", null);
			put("server_memorysize_format", null);
			put("data_identifiers", null);
			put("input_output", null);
			put("request_timeout", 5000);
			put("p2_timeout", 1000);
			put("p2_star_timeout", 5000);
		}

	};

}
