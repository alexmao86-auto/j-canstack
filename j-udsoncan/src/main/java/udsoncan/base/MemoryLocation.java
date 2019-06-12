package udsoncan.base;

import java.math.BigInteger;

/**
 * This class defines a memory block location including : address, size,
 * AddressAndLengthFormatIdentifier (address format and memory size format)
 * 
 * :param address: A memory address pointing to the beginning of the memory
 * block :type address: int
 * 
 * :param memorysize: The size of the memory block :type memorysize: int
 * 
 * :param address_format: The number of bits on which an address should be
 * encoded. Possible values are 8, 16, 24, 32, 40. If ``None`` is specified, the
 * smallest size required to store the given address will be used :type
 * address_format: int or None
 * 
 * :param memorysize_format: The number of bits on which a memory size should be
 * encoded. Possible values are 8, 16, 24, 32 If ``None`` is specified, the
 * smallest size required to store the given memorysize will be used :type
 * memorysize_format: int or None
 */
public class MemoryLocation {

	public int address;
	public int memorysize;
	public Integer address_format;
	public Integer memorysize_format;
	public AddressAndLengthFormatIdentifier alfid;

	public MemoryLocation(int address, int memorysize, Integer address_format, Integer memorysize_format)
			throws Exception {
		this.address = address;
		this.memorysize = memorysize;
		this.address_format = address_format;
		this.memorysize_format = memorysize_format;

		if (address_format == null) {
			this.address_format = this.autosize_address(address);
		}

		if (memorysize_format == null) {
			this.memorysize_format = this.autosize_memorysize(address);
		}

		this.alfid = new AddressAndLengthFormatIdentifier(address_format, memorysize_format);
	}

	public void set_format_if_none(Integer address_format, Integer memorysize_format) throws Exception {
		Integer previous_address_format = this.address_format;
		Integer previous_memorysize_format = this.memorysize_format;
		try {
			if(address_format!=null) {
				if(this.address_format==null) {
					this.address_format = address_format;
				}
			}
			if(memorysize_format!=null) {
				if(this.memorysize_format==null) {
					this.memorysize_format = memorysize_format;
				}
			}
			if(this.address_format!=null) 
				address_format = this.address_format;
			else 
				address_format = this.autosize_address(this.address);
			if(this.memorysize_format!=null) 
				memorysize_format = this.memorysize_format;
			else 
				memorysize_format = this.autosize_memorysize(this.memorysize);
			
			this.alfid = new AddressAndLengthFormatIdentifier(address_format, memorysize_format);
		} catch (Exception e) {
			this.address_format = previous_address_format;
			this.memorysize_format = previous_memorysize_format;
			throw e;
		}
	}
	


	// TODO: add a constructor with byte[], byte[] arguments
	public MemoryLocation(byte[] address_bytes, byte[] memorysize_bytes) {

	}

	/** # Finds the smallest size that fits the address */
	private Integer autosize_address(long address) throws Exception {
		int fmt = (BigInteger.valueOf(address).bitLength() / 8 + 1) * 8;

		if (fmt > 40) {
			throw new Exception("address size must be smaller or equal than 40 bits");
		}
		return fmt;
	}

	/** Finds the smallest size that fits the memory size */
	private Integer autosize_memorysize(long address) throws Exception {
		int fmt = (BigInteger.valueOf(address).bitLength() / 8 + 1) * 8;
		if (fmt > 32) {
			throw new Exception("memory size must be smaller or equal than 32 bits");
		}
		return null;
	}

	public byte[] get_address_bytes() {
		byte[] result = null;
		int n = AddressAndLengthFormatIdentifier.address_map.get(this.alfid.address_format);
		// TODO: data = struct.pack('>q', self.address) //big-endian long
		// return data[-n:] how to implement this?
		return result;
	}

	public byte[] get_memorysize_bytes() {
		byte[] result = null;
		int n = AddressAndLengthFormatIdentifier.memsize_map.get(this.alfid.memorysize_format);
		// TODO: data = struct.pack('>q', self.memorysize) //big-endian long
		// return data[-n:] how to implement this?
		return result;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Address=0x");
		builder.append(String.format("%x", this.address));
		builder.append("(");
		builder.append(String.format("%d", this.alfid.address_format));
		builder.append("bits), Size=0x");
		builder.append(String.format("%x", this.memorysize));
		builder.append("(");
		builder.append(String.format("%d", this.alfid.memorysize_format));
		builder.append(" bits)");
		return builder.toString();
	}

}
