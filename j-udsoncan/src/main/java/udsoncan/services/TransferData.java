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

public class TransferData extends BaseService {

	List<Integer> supported_negative_response = new ArrayList<Integer>() {
		private static final long serialVersionUID = 1724351391841881093L;
		{
			add(Response.Code.IncorrectMessageLegthOrInvalidFormat);
			add(Response.Code.RequestSequenceError);
			add(Response.Code.RequestOutOfRange);
			add(Response.Code.TransferDataSuspended);
			add(Response.Code.GeneralProgrammingFailure);
			add(Response.Code.WrongBlockSequenceCounter);
			add(Response.Code.VoltageTooHigh);
			add(Response.Code.VoltageTooLow);
		}
	};

	public TransferData() {
		_sid = 0x36;
		_use_subfunction = false;

	}

	/**
	 * Generates a request for TransferData
	 * 
	 * @param sequence_number Corresponds to an 8bit counter that should increment
	 *                        for each new block transferred. Allowed values are
	 *                        from 0 to 0xFF
	 * @param data            Optional additional data to send to the server
	 */
	public Request make_request(int sequence_number, byte[] data) throws Exception {
		ServiceHelper.validate_int(sequence_number, 0, 0x7f, "Block sequence counter"); // Not a subfunction!

		Request request = new Request(this);
		List<Byte> reqData = new ArrayList<Byte>();
		reqData.add((byte) sequence_number);
		if (data != null) {
			for (byte b : data)
				reqData.add(b);
		}
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
		ResponseData service_data = new ResponseData();
		service_data.sequence_number_echo = response.data[0];
		if (response.data.length > 1) {
			service_data.parameter_records = Arrays.copyOfRange(response.data, 1, response.data.length);
		}
		response.service_data = service_data;
	}

	public class ResponseData extends BaseResponseData {

		public Byte sequence_number_echo = null;
		public byte[] parameter_records = null;

		public ResponseData() throws Exception {
			super(TransferData.class);
			// TODO Auto-generated constructor stub
		}

	}

}
