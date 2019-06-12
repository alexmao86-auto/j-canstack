package udsoncan.base;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Represents a link speed in bit per seconds (or symbol per seconds to be more
 * accurate). This class is used by the :ref:`LinkControl<LinkControl>` service
 * that controls the underlying protocol speeds.
 * 
 * The class can encode the baudrate in 2 different fashions : **Fixed** or
 * **Specific**.
 * 
 * Some standard baudrate values are defined within ISO-14229:2006 Annex B.3
 * 
 * :param baudrate: The baudrate to be used. :type baudrate: int
 * 
 * :param baudtype: Tells how the baudrate shall be encoded. 4 values are
 * possible:
 * 
 * - ``Baudrate.Type.Fixed`` (0) : Will encode the baudrate in a single byte
 * Fixed fashion. `baudrate` should be a supported value such as 9600, 19200,
 * 125000, 250000, etc. - ``Baudrate.Type.Specific`` (1) : Will encode the
 * baudrate in a three-byte Specific fashion. `baudrate` can be any value
 * ranging from 0 to 0xFFFFFF - ``Baudrate.Type.Identifier`` (2) : Will encode
 * the baudrate in a single byte Fixed fashion. `baudrate` should be the byte
 * value to encode if the user wants to use a custom type. -
 * ``Baudrate.Type.Auto`` (3) : Let the class guess the type.
 * 
 * - If ``baudrate`` is a known standard value (19200, 38400, etc), then Fixed
 * shall be used - If ``baudrate`` is an integer that fits in a single byte,
 * then Identifier shall be used - If ``baudrate`` is none of the above, then
 * Specific will be used. :type baudtype: int
 */
public class Baudrate {

	public static final Map<Integer, Integer> baudrate_map = new HashMap<Integer, Integer>() {
		{
			put(9600, 0x01);
			put(19200, 0x02);
			put(38400, 0x03);
			put(57600, 0x04);
			put(115200, 0x05);
			put(125000, 0x10);
			put(250000, 0x11);
			put(500000, 0x12);
			put(1000000, 0x13);
		}
	};

	public class Type {
		public static final int Fixed = 0; // When baudrate is a predefined value from standard
		public static final int Specific = 1; // When using custom baudrate
		public static final int Identifier = 2; // Baudrate implied by baudrate ID
		public static final int Auto = 3; // Let the class decide the type
	}

	public int baudrate;
	public int baudtype = Type.Auto;

	public Baudrate(int baudrate, int baudtype) throws Exception {
		if (baudrate < 0) {
			throw new Exception("baudrate must be an integer greater than 0");
		}

		if (baudtype == Type.Auto) {
			if (baudrate_map.containsKey(baudrate)) {
				this.baudtype = Type.Fixed;
			} else {
				if (baudrate < 0xFF) {
					this.baudtype = Type.Identifier;
				} else {
					this.baudtype = Type.Specific;
				}
			}
		} else {
			this.baudtype = baudtype;
		}

		if (this.baudtype == Type.Specific) {
			if (baudrate > 0xFFFFFF) {
				throw new Exception("Baudrate value cannot be bigger than a 24 bits value.");
			}
		} else if (this.baudtype == Type.Identifier) {
			if (baudrate > 0xFF) {
				throw new Exception("Baudrate ID must be an integer between 0 and 0xFF");
			}
		} else if (this.baudtype == Type.Fixed) {
			if (!baudrate_map.containsKey(baudrate)) {
				throw new Exception("baudrate must be part of the supported baudrate list defined by UDS standard");
			}
		} else {
			throw new Exception("Unknown baudtype : " + this.baudtype);
		}

		this.baudrate = baudrate;

	}

	// internal helper to change the type of this baudrate
	public Baudrate make_new_type(int baudtype) throws Exception {
		if (baudtype != Type.Fixed && baudtype != Type.Specific) {
			throw new Exception("Baudrate value cannot be bigger than a 24 bits value.");
		}
		return new Baudrate(this.effective_baudrate(), baudtype);
	}

	// Returns the baudrate in Symbol Per Seconds if available, otherwise value
	// given by the user.
	public int effective_baudrate() throws Exception {
		if (this.baudtype == Type.Identifier) {
			if (baudrate_map.containsValue(this.baudrate)) {
				Iterator<Entry<Integer, Integer>> it = baudrate_map.entrySet().iterator();
				int k = 0;
				while (it.hasNext()) {
					Map.Entry<Integer, Integer> entry = (Entry<Integer, Integer>) it.next();
					if (entry.getValue() == this.baudrate) {
						k = entry.getKey();
						break;
					}

				}
				return k;
			} else {
				throw new Exception("Unknown effective baudrate, this could indicate a bug.");
			}
		} else {
			return this.baudrate;
		}
	}

	// TODO: to complete it
	public byte[] get_bytes() {
		byte[] bs = null;
		if (this.baudtype == Type.Fixed) {
//			return baudrate_map.get(this.baudrate);
		}

		if (this.baudtype == Type.Specific) {

		}
		return bs;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String baudtype_str = "";

		switch (this.baudtype) {
		case Type.Fixed:
			baudtype_str = "Fixed";
			break;
		case Type.Specific:
			baudtype_str = "Specific";
			break;
		case Type.Identifier:
			baudtype_str = "Defined by identifier";
			break;

		}
		try {
			builder.append(this.effective_baudrate());
			builder.append("Bauds, ");
			builder.append(baudtype_str);
			builder.append(" format.");
		} catch (Exception e) {
		}
		return builder.toString();
	}

}
