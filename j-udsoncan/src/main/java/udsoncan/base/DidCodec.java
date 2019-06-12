package udsoncan.base;

import java.util.Map;

/**
 * This class defines how to encode/decode a Data Identifier value to/from a
 * binary payload. <br>
 * <br>
 * One should extend this class and override the ``encode``, ``decode``,
 * ``__len__`` methods as they will be used to generate or parse binary
 * payloads.
 * 
 * - ``encode`` Must receive any java object and must return a bytes payload -
 * ``decode`` Must receive a bytes payload and may return any java object -
 * ``__len__`` Must return the length of the bytes payload <br>
 * <br>
 * If a data can be processed by a pack string, then this class may be used as
 * is, without being extended. <br>
 * <br>
 * :param packstr: A pack string used with struct.pack / struct.unpack. :type
 * packstr: string
 * 
 */
public class DidCodec {

//	private String packstr;
	private Class packClass;

	public DidCodec() {
	}

	public DidCodec(Class packClass) {
		this.packClass = packClass;
	}

	public byte[] encode(Object did_value) throws Exception {
		if (this.packClass == null) {
			throw new Exception("Cannot encode DID to binary payload. Codec has no \"encode\" implementation");
		}
		// todo: according packstr encode did_value and return byte[]
		return null;
	}

	public Object decode(byte[] did_payload) throws Exception {
		if (this.packClass == null) {
			throw new Exception("Cannot decode DID from binary payload. Codec has no \"decode\" implementation");
		}
		// TODO: according packstr decode did_payload and return byte[]
		return null;
	}

	public int len() throws Exception {
		if (this.packClass == null) {
			throw new Exception("Cannot tell the payload size. Codec has no \"len\" implementation");
		}

		return 0;
	}

	public static DidCodec from_config(Integer integer) {
		// TODO Auto-generated method stub
		return null;
	}

}
