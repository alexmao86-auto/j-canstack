package udsoncan.base;

import java.util.HashMap;
import java.util.Map;

/**
 * Used for IO Control service. Allows comprehensive one-liner.
 * 
 * This class saves a function argument so they can be passed to a callback
 * function.
 * 
 * @param args: Arguments 
 * @param kwargs: Named arguments
 */
public class IOValues {
	public String[] args;
	public Map<String, String>kwargs = new HashMap<String, String>();
	
	public IOValues(String[] args, Map<String, String> kwargs) {
		this.args=args;
		this.kwargs=kwargs;
	}
}
