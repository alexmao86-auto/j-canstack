package udsoncan.services;

import java.util.ArrayList;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.DataFormatIdentifier;
import udsoncan.base.MemoryLocation;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;

public class RequestUpload extends BaseService {
	public final int _sid = 0x35;
	public boolean _use_subfunction = false;

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.SecurityAccessDenied);
			add(Response.Code.UploadDownloadNotAccepted);
		}
	};

	public RequestUpload() {
		// TODO Auto-generated constructor stub
	}

	public static DataFormatIdentifier normalize_data_format_identifier(DataFormatIdentifier dfi) throws Exception {
		if (dfi == null) {
			dfi = new DataFormatIdentifier();
		}
		return dfi;
	}
	
	
	/**
	 * Generates a request for RequestUpload
	 * 
	 * @param memory_location The address and the size of the memory block to be
	 *                        read.
	 * @param dfi             Optional :ref:`DataFormatIdentifier
	 *                        <DataFormatIdentifier>` defining the compression and
	 *                        encryption scheme of the data. If not specified, the
	 *                        default value of 00 will be used, specifying no
	 *                        encryption and no compression
	 */
	public Request make_request(MemoryLocation memory_location, DataFormatIdentifier dfi) throws Exception {
		dfi = normalize_data_format_identifier(dfi);

		Request request = new Request(this);
		List<Byte> reqData = new ArrayList<Byte>();
		reqData.add(dfi.get_byte());
		reqData.add(memory_location.alfid.get_byte());
		for (byte b : memory_location.get_address_bytes())
			reqData.add(b);
		for (byte b : memory_location.get_memorysize_bytes())
			reqData.add(b);

		request.data = new byte[reqData.size()];
		for (int i = 0; i < reqData.size(); i++) {
			request.data[i] = reqData.get(i);
		}
		return request;
	}

	public void interpret_response(Response response) throws Exception {
		if (response.data.length < 1) {
			throw new InvalidResponseException(response, "Response data must be at least 1 byte");
		}

		int lfid = response.data[0] >> 4;
		if (lfid > 8) {
			throw new Exception(String.format("This client does not support number bigger than %d bits", 8 * 8));
		}
		if (response.data.length < (lfid + 1)) {
			throw new InvalidResponseException(response,
					String.format(
							"Length of data (%d) is too short to contains the number of block of given length (%d)",
							response.data.length, lfid + 1));
		}
		byte[] todecode = { 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 };
		for (int i = 1; i < (lfid + 1); i++) {
			todecode[todecode.length - 1 - i] = response.data[lfid + 1 - i];
		}

		ResponseData service_data = new ResponseData();
		for (int i = 0; i < 8; i++) {
			service_data.max_length += ((long) todecode[i]) << (7 - i);
		}
		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public Long max_length = null;

		public ResponseData() throws Exception {
			super(RequestUpload.class);
			// TODO Auto-generated constructor stub
		}

	}
}
