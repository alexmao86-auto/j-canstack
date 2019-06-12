package udsoncan.base.exceptions;

import udsoncan.Response;

/**
 * Raised when the server returns a negative response (response code starting by
 * 0x7F). The response that triggered the exception is available in
 * ``e.response``
 * 
 * :param response: The response that triggered the exception :type response:
 * :ref:`Response<Response>`
 */
public class NegativeResponseException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7196404418182973277L;

	public Response response;
	public String detailMessage;

	public NegativeResponseException(Response response, String message) {
		super(NegativeResponseException.make_msg(response) + message);
		this.response = response;
		this.detailMessage = this.make_msg(response);
	}

	static String make_msg(Response response) {
		// TODO: to be implemented later
//		String servicename = response.
//		servicename = response.service.get_name()+" " if response.service is not None else ""
//			return "%sservice execution returned a negative response %s (0x%x)" % (servicename, response.code_name, response.code)
		return "";
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("NegativeResponseException [response=");
		builder.append(response);
		builder.append(", detailMessage=");
		builder.append(detailMessage);
		builder.append("]");
		return builder.toString();
	}
	
	

}
