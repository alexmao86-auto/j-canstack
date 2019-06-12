package udsoncan.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import udsoncan.Request;
import udsoncan.Response;
import udsoncan.base.MemoryLocation;
import udsoncan.base.exceptions.InvalidResponseException;
import udsoncan.services.base.BaseResponseData;
import udsoncan.services.base.BaseService;

public class WriteMemoryByAddress extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.ConditionsNotCorrect);
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.SecurityAccessDenied);
			add(Response.Code.GeneralProgrammingFailure);
		}
	};

	public WriteMemoryByAddress() {
		_sid = 0x3D;
		_use_subfunction = false;
	}

	/**
	 * Generates a request for WriteMemoryByAddress
	 * 
	 * @param memory_location The address and the size of the memory block to write.
	 * @param data            The data to write into memory.
	 * 
	 */
	public Request make_request(MemoryLocation memory_location, byte[] data) throws Exception {
		Request request = new Request(this);
		List<Byte> reqData = new ArrayList<Byte>();
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

	public void interpret_response(Response response, MemoryLocation memory_location) throws Exception {

		byte[] address_bytes = memory_location.get_address_bytes();
		byte[] memorysize_bytes = memory_location.get_memorysize_bytes();

		int expected_response_size = 1 + address_bytes.length + memorysize_bytes.length;

		if (response.data.length < expected_response_size) {
			throw new InvalidResponseException(response,
					"Response data must be at least " + expected_response_size + " byte");
		}

		ResponseData service_data = new ResponseData();
		service_data.alfid_echo = response.data[0];

		int offset = 1;
		int length = address_bytes.length;
		byte[] address_echo = Arrays.copyOfRange(response.data, offset, offset + length);
		offset += length;
		length = memorysize_bytes.length;
		byte[] memorysize_echo = Arrays.copyOfRange(response.data, offset, offset + length);
		service_data.memory_location_echo = new MemoryLocation(address_echo, memorysize_echo);
		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public Byte alfid_echo = null; // AddressAndLengthFormatIdentifier
		public MemoryLocation memory_location_echo = null;

		public ResponseData() throws Exception {
			super(WriteMemoryByAddress.class);
			// TODO Auto-generated constructor stub
		}
	}

}
