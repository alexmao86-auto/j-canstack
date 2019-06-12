package udsoncan.base;

/**
 * Defines the compression and encryption method of a specific chunk of data.
 * Mainly used by the :ref:`RequestUpload<RequestUpload>` and
 * :ref:`RequestDownload<RequestDownload>` services
 * 
 * :param compression: Value between 0 and 0xF specifying the compression
 * method. Only the value 0 has a meaning defined by UDS standard and it is `No
 * compression`. All other values are ECU manufacturer specific. :type
 * compression: int
 * 
 * :param encryption: Value between 0 and 0xF specifying the encryption method.
 * Only the value 0 has a meaning defined by UDS standard and it is `No
 * encryption`. All other values are ECU manufacturer specific. :type
 * encryption: int
 */
public class DataFormatIdentifier {
	private int compression = 0;
	private int encryption = 0;

	public DataFormatIdentifier(int compression, int encryption) throws Exception {
		if (compression < 0 || compression > 0xF || encryption < 0 || encryption > 0xF) {
			throw new Exception("compression and encryption method must each be an integer between 0 and 0xF");
		}
		this.compression = compression;
		this.encryption = encryption;
	}

	public DataFormatIdentifier() throws Exception {
		this(0, 0);
	}

	public int get_byte_as_int() {
		return ((this.compression & 0xF) << 4 | (this.encryption & 0xF));
	}

	public byte get_byte() {
		return (byte) this.get_byte_as_int();
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DataFormatIdentifier [compression=0x");
		builder.append(String.format("%x", this.compression));
		builder.append(", encryption=0x");
		builder.append(String.format("%x", this.encryption));
		builder.append("]");
		return builder.toString();
	}

}
