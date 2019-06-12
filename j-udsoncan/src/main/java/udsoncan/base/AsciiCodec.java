package udsoncan.base;

public class AsciiCodec extends DidCodec {

	private int string_len;
	
	public AsciiCodec(Integer string_len) throws Exception {
		if(string_len == null) {
			throw new Exception("You must provide a string length to the AsciiCodec");
		}
		this.string_len = string_len;
	}
	
//	@Override
//	public byte[] encode(String string_ascii) throws Exception {
//		byte[] result = null;
//		if(string_ascii.length() != this.string_len) {
//			throw new Exception("String must be "+this.string_len+" long");
//		}
//		
//		return result;
//	}
	
	@Override
	public byte[] encode(String did_value) throws Exception {
		// TODO Auto-generated method stub
		return super.encode(did_value);
	}

	@Override
	public String decode(byte[] string_bin) {
		String result = null;
//		if(string_bin.length() != this.string_len) {
//			throw new Exception("String must be "+this.string_len+" long");
//		}
		
		return result;
	}
	
	public int len() {
		return this.string_len;
	}

}
