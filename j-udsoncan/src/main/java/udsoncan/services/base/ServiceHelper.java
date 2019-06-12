package udsoncan.services.base;

import java.util.List;
import java.util.Map;

import udsoncan.base.DidCodec;

public class ServiceHelper {

	public static void validate_int(int value, int min, int max, String name) throws Exception {
		if(value < min || value>max) {
			throw new Exception(name + " must be an integer between 0x"+String.format("%x", min)+" and 0x" + String.format("0x", max));
		}
	}
	
	//Make sure that the actual client configuration contains valid definitions for given Data Identifiers
	public static DidCodec check_did_config(DidCodec didCodec) {
		//TODO: to be implemented later.
		return didCodec;
	}
	
	//Make sure that the actual client configuration contains valid definitions for given Input/Output Data Identifiers
	public static DidCodec check_io_config(DidCodec didCodec) {
		//TODO: to be implemented later.
				return didCodec;
	}

	public static Map<Integer, Integer> check_did_config(int didlist, Map<Integer, Integer> didconfig) {
		return didconfig;
		// TODO Auto-generated method stub
		
	}

	public static void check_did_config(List<Integer> didlist, Map<Integer, Integer> didconfig) {
		// TODO Auto-generated method stub
		
	}
}
