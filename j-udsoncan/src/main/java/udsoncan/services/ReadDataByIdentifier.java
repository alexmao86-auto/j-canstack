package udsoncan.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.DidCodec;
import udsoncan.base.exceptions.ConfigError;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;
import udsoncan.services.base.ServiceHelper;

public class ReadDataByIdentifier extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.SecurityAccessDenied);
			add(Response.Code.RequestOutOfRange);
		}
	};

	public ReadDataByIdentifier() {
		this._sid = 0x22;
		this._use_subfunction = false;
	}

	/**
	 * Generates a request for ReadDataByIdentifier
	 * 
	 * @param reset_type Service subfunction. Allowed values are from 0 to 0x7F
	 * 
	 */
	public Request make_request(List<Integer> didlist, Map<Integer, Integer> didconfig) throws Exception {
		didlist = this.validate_didlist_input(didlist);
		ServiceHelper.check_did_config(didlist, didconfig);

		Request request = new Request(this);

		// Encode list of DID
		List<Byte> reqData = new ArrayList<Byte>();
		for (int did : didlist) {
			reqData.add((byte) ((did & 0xff00) >> 8));
			reqData.add((byte) ((did & 0x00ff) >> 0));
		}
		request.data = new byte[reqData.size()];
		for (int i = 0; i < reqData.size(); i++) {
			request.data[i] = reqData.get(i);
		}
		return request;
	}

	/**
	 * @param response              The received response to interpret
	 * @param didlist               List of data identifiers used for the request.
	 * @param didconfig             Definition of DID codecs. Dictionary mapping a
	 *                              DID (int) to a valid :ref:`DidCodec<DidCodec>`
	 *                              class or pack/unpack string
	 * @param tolerate_zero_padding Ignore trailing zeros in the response data
	 *                              avoiding raising false
	 * @throws Exception
	 */
	public void interpret_response(Response response, List<Integer> didlist, Map<Integer, Integer> didconfig,
			Boolean tolerate_zero_padding) throws Exception {
		if (tolerate_zero_padding == null) {
			tolerate_zero_padding = true;
		}
		didlist = this.validate_didlist_input(didlist);
		ServiceHelper.check_did_config(didlist, didconfig);

		ResponseData service_data = new ResponseData();
		service_data.values = new LinkedHashMap<Integer, Object>();
		// Parsing algorithm to extract DID value
		int offset = 0;
		while (true) {
			StringBuffer sb = new StringBuffer();
			for (byte b : response.data) {
				sb.append(String.format("%X ", b));
			}
			System.out.println(String.format("Response data = [%s].", sb.toString()));

			if (response.data.length <= offset) {
				break;
			}
			if (response.data.length <= (offset + 1)) {
				if (tolerate_zero_padding && response.data[response.data.length - 1] == 0) {
					// One extra byte, but it's a 0 and we accept that. So we're done
					break;
				}
				throw new InvalidResponseException(response, "Response given by server is incomplete.");
			}
			// Get the DID number
			int did = 0xff00&(((int) response.data[offset]) << 8) + response.data[offset + 1];
			
			// We read two zeros and that is not a DID but we accept that. So we're done.
			if (did == 0 && !didconfig.containsKey(did) && tolerate_zero_padding) {
				byte[] a = Arrays.copyOfRange(response.data, offset, response.data.length);
				byte[] a2 = new byte[response.data.length - offset];
				if (Arrays.equals(a, a2)) {
					break;
				}
			}

			// Already checked in check_did_config. Paranoid check
			if (!didconfig.containsKey(did)) {
				throw new ConfigError(did, String.format(
						"Actual data identifier configuration contains no definition for data identifier 0x%04x", did));
			}

			DidCodec codec = DidCodec.from_config(didconfig.get(did));
			offset += 2;

			if (response.data.length < (offset + codec.len())) {
				throw new InvalidResponseException(response, String.format(
						"Value for data identifier 0x%04x was incomplete according to definition in configuration",
						did));
			}
			byte[] subpayload = Arrays.copyOfRange(response.data, offset, offset + codec.len());
			offset += codec.len();
			Object val = codec.decode(subpayload);
			service_data.values.put(did, val);
		}

		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public ResponseData() throws Exception {
			super(ReadDataByIdentifier.class);
			// TODO Auto-generated constructor stub
		}

		public Map<Integer, Object> values = null;
	}

	public List<Integer> validate_didlist_input(List<Integer> dids) throws Exception {
		for (int did : dids) {
			ServiceHelper.validate_int(did, 0, 0xFFFF, "Data Identifier");
		}
		return dids;
	}

	public int validate_didlist_input(int dids) throws Exception {
		ServiceHelper.validate_int(dids, 0, 0xFFFF, "Data Identifier");
		return dids;
	}

}
