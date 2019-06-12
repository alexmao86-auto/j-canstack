package udsoncan.base;

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines how many bytes of a memorylocation, composed of an address
 * and a memorysize, should be encoded when sent over the underlying protocol.
 * Mainly used by :ref:`ReadMemoryByAddress<ReadMemoryByAddress>`,
 * :ref:`WriteMemoryByAddress<WriteMemoryByAddress>`,
 * :ref:`RequestDownload<RequestDownload>` and
 * :ref:`RequestUpload<RequestUpload>` services
 * 
 * Defined by ISO-14229:2006, Annex G
 * 
 * :param address_format: The number of bits on which an address should be
 * encoded. Possible values are 8, 16, 24, 32, 40 :type address_format: int
 * 
 * :param memorysize_format: The number of bits on which a memory size should be
 * encoded. Possible values are 8, 16, 24, 32 :type memorysize_format: int
 */
public class AddressAndLengthFormatIdentifier {

	static final Map<Integer, Integer> address_map = new HashMap<Integer, Integer>() {
		{
			put(8, 1);
			put(16, 2);
			put(24, 3);
			put(32, 4);
			put(40, 5);
		}
	};
	
	static final Map<Integer, Integer> memsize_map = new HashMap<Integer, Integer>() {
		{
			put(8, 1);
			put(16, 2);
			put(24, 3);
			put(32, 4);
		}
	};

	public int address_format;
	public int memorysize_format;
	public AddressAndLengthFormatIdentifier(int address_format, int memorysize_format) throws Exception {
		
		if(!AddressAndLengthFormatIdentifier.address_map.containsKey(address_format)) {
			throw new Exception("address_format must ba an integer selected from :" + AddressAndLengthFormatIdentifier.address_map.keySet().toString());
		}
		if(!AddressAndLengthFormatIdentifier.memsize_map.containsKey(memorysize_format)) {
			throw new Exception("memorysize_format must ba an integer selected from :" + AddressAndLengthFormatIdentifier.memsize_map.keySet().toString());
		}
		
		this.address_format = address_format;
		this.memorysize_format = memorysize_format;
	}
	
	public int get_byte_as_int() {
		return ((AddressAndLengthFormatIdentifier.memsize_map.get(this.memorysize_format)<<4) | (AddressAndLengthFormatIdentifier.address_map.get(this.address_format))) &0xFF;
	}
	
	/**Byte given alongside a memory address and a length so that they are decoded properly.*/
	public byte get_byte() {
		return (byte)this.get_byte_as_int();
	}
	
	

}
