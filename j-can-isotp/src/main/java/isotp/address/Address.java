package isotp.address;

import java.util.Arrays;

import isotp.protocol.CanMessage;

/**
 * Represents the addressing information (N_AI) of the IsoTP layer. Will define
 * what messages will be received and how to craft transmitted message to reach
 * a specific party.
 * 
 * Parameters must be given according to the addressing mode. When not needed, a
 * parameter may be left unset or set to ``None``.
 * 
 * Both the :class:`TransportLayer<isotp.TransportLayer>` and the
 * :class:`isotp.socket<isotp.socket>` expects this address object
 */
public class Address {
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Address [addressing_mode=");
		builder.append(addressing_mode);
		builder.append(", target_address=");
		builder.append(target_address);
		builder.append(", source_address=");
		builder.append(source_address);
		builder.append(", address_extension=");
		builder.append(address_extension);
		builder.append(", txid=");
		builder.append(txid);
		builder.append(", rxid=");
		builder.append(rxid);
		builder.append(", is_29bits=");
		builder.append(is_29bits);
		builder.append(", tx_arbitration_id_physical=");
		builder.append(tx_arbitration_id_physical);
		builder.append(", tx_arbitration_id_functional=");
		builder.append(tx_arbitration_id_functional);
		builder.append(", rx_arbitration_id_physical=");
		builder.append(rx_arbitration_id_physical);
		builder.append(", rx_arbitration_id_functional=");
		builder.append(rx_arbitration_id_functional);
		builder.append(", tx_payload_prefix=");
		builder.append(Arrays.toString(tx_payload_prefix));
		builder.append(", rx_prefix_size=");
		builder.append(rx_prefix_size);
		builder.append(", rxmask=");
		builder.append(rxmask);
		builder.append(", is_for_me=");
		builder.append(is_for_me);
		builder.append("]");
		return builder.toString();
	}

	public int addressing_mode = AddressingMode.Normal_11bits;
	public Integer target_address;
	public Integer source_address;
	public Integer address_extension;
	public Integer txid;
	public Integer rxid;
	public boolean is_29bits = false;

	public Integer tx_arbitration_id_physical;
	public Integer tx_arbitration_id_functional;
	public Integer rx_arbitration_id_physical;
	public Integer rx_arbitration_id_functional;

//	public List<Byte> tx_payload_prefix; // bytearray()
	public byte[] tx_payload_prefix;
	public int rx_prefix_size = 0;

	public int rxmask = 0;
	public Is_for_me is_for_me = null;

	public Address(int addressing_mode, Integer target_address, Integer source_address, Integer address_extension,
			Integer txid, Integer rxid) throws Exception {
		this.addressing_mode = addressing_mode;
		this.target_address = target_address;
		this.source_address = source_address;
		this.address_extension = address_extension;
		this.txid = txid;
		this.rxid = rxid;

		this.is_29bits = AddressingMode.is29bits(addressing_mode);

		this.validate();

		// From here, input is good. Do some precomputing for speed optimization without
		// bothering about types or values
		this.tx_arbitration_id_physical = this._get_tx_arbitraton_id(TargetAddressType.Physical);
		this.tx_arbitration_id_functional = this._get_tx_arbitraton_id(TargetAddressType.Functional);

		this.rx_arbitration_id_physical = this._get_rx_arbitration_id(TargetAddressType.Physical);
		this.rx_arbitration_id_functional = this._get_rx_arbitration_id(TargetAddressType.Functional);

		if (this.addressing_mode == AddressingMode.Extended_11bits
				|| this.addressing_mode == AddressingMode.Extended_29bits) {
			tx_payload_prefix = new byte[1];
			tx_payload_prefix[0] = this.target_address.byteValue();
			this.rx_prefix_size = 1;
		} else if (this.addressing_mode == AddressingMode.Mixed_11bits
				|| this.addressing_mode == AddressingMode.Mixed_29bits) {
			tx_payload_prefix = new byte[1];
			tx_payload_prefix[0] = this.address_extension.byteValue();
			this.rx_prefix_size = 1;
		} else {
			tx_payload_prefix = new byte[0];
		}

		if (this.addressing_mode == AddressingMode.NormalFixed_29bits) {
			this.rxmask = 0x18DA0000;
		} else if (this.addressing_mode == AddressingMode.Mixed_29bits) {
			this.rxmask = 0x18CD0000;
		}

		switch (this.addressing_mode) {
		case AddressingMode.Normal_11bits:
		case AddressingMode.Normal_29bits:
			this.is_for_me = new Is_for_me() {
				// _is_for_me_normal
				@Override
				public boolean check(CanMessage msg) {
					if (Address.this.is_29bits == msg.is_extended_id) {
						return msg.arbitration_id == rxid;
					}
					return false;
				}
			};
			break;
		case AddressingMode.NormalFixed_29bits:
			this.is_for_me = new Is_for_me() {
				// _is_for_me_normalfixed
				@Override
				public boolean check(CanMessage msg) {
					if (Address.this.is_29bits == msg.is_extended_id) {
						int a = (msg.arbitration_id >> 16) & 0xFF;
						return (a >= 218 && a <= 219)
								&& ((msg.arbitration_id & 0xFF00) >> 8 == Address.this.source_address)
								&& ((msg.arbitration_id & 0xFF) == Address.this.target_address);
					}
					return false;
				}
			};
			break;
		case AddressingMode.Extended_11bits:
		case AddressingMode.Extended_29bits:
			this.is_for_me = new Is_for_me() {
				// _is_for_me_extended
				@Override
				public boolean check(CanMessage msg) {
					if (Address.this.is_29bits == msg.is_extended_id) {
						if (msg.data != null) {
							if (msg.data.length > 0) {
								return (msg.arbitration_id == Address.this.rxid)
										&& (msg.data[0] == Address.this.source_address.byteValue());
							}
						}
					}
					return false;
				}
			};
			break;
		case AddressingMode.Mixed_11bits:
			this.is_for_me = new Is_for_me() {
				// _is_for_me_mixed_11bits
				@Override
				public boolean check(CanMessage msg) {
					if (Address.this.is_29bits == msg.is_extended_id) {
						System.out.println("I'm checking Mixed_11bits with Address.this.address_extension = "
								+ Address.this.address_extension + " and msg=" + msg.toString());
						if (msg.data != null) {
							if (msg.data.length > 0) {

								return (msg.arbitration_id == Address.this.rxid)
										&& (msg.data[0] == Address.this.address_extension.byteValue());
							}
						}
					}
					return false;
				}
			};
			break;
		case AddressingMode.Mixed_29bits:
			this.is_for_me = new Is_for_me() {
				// _is_for_me_mixed_29bits
				@Override
				public boolean check(CanMessage msg) {
					if (Address.this.is_29bits == msg.is_extended_id) {
						if (msg.data != null) {
							if (msg.data.length > 0) {
								int a = (msg.arbitration_id >> 16) & 0xFF;
								return (a >= 205 && a <= 206)
										&& ((msg.arbitration_id & 0xFF00) >> 8 == Address.this.source_address)
										&& ((msg.arbitration_id & 0xFF) == Address.this.target_address)
										&& (msg.data[0] == Address.this.address_extension.byteValue());
							}
						}
					}
					return false;
				}
			};
			break;
		default:
			break;
		}
	}

	private Integer _get_rx_arbitration_id(int address_type) {
		int bits23_16;
		switch (this.addressing_mode) {
		case AddressingMode.Normal_11bits:
		case AddressingMode.Normal_29bits:
			return this.rxid;
		case AddressingMode.NormalFixed_29bits:

			if (address_type == TargetAddressType.Physical) {
				bits23_16 = 0xDA0000;
			} else {
				bits23_16 = 0xDB0000;
			}
			return (0x18000000 | bits23_16 | (this.source_address << 8) | this.target_address);
		case AddressingMode.Extended_11bits:
		case AddressingMode.Extended_29bits:
		case AddressingMode.Mixed_11bits:
			return this.rxid;
		case AddressingMode.Mixed_29bits:
			if (address_type == TargetAddressType.Physical) {
				bits23_16 = 0xCE0000;
			} else {
				bits23_16 = 0xCD0000;
			}
			return (0x18000000 | bits23_16 | (this.source_address << 8) | this.target_address);
		default:
			break;
		}
		return -1;
	}

	private Integer _get_tx_arbitraton_id(int address_type) {
		int bits23_16;
		switch (this.addressing_mode) {
		case AddressingMode.Normal_11bits:
		case AddressingMode.Normal_29bits:
			return this.txid != null ? this.txid : -1;
		case AddressingMode.NormalFixed_29bits:

			if (address_type == TargetAddressType.Physical) {
				bits23_16 = 0xDA0000;
			} else {
				bits23_16 = 0xDB0000;
			}
			return (0x18000000 | bits23_16 | (this.target_address << 8) | this.source_address);
		case AddressingMode.Extended_11bits:
		case AddressingMode.Extended_29bits:
		case AddressingMode.Mixed_11bits:
			return this.txid != null ? this.txid : -1;
		case AddressingMode.Mixed_29bits:
			if (address_type == TargetAddressType.Physical) {
				bits23_16 = 0xCE0000;
			} else {
				bits23_16 = 0xCD0000;
			}
			return (0x18000000 | bits23_16 | (this.target_address << 8) | this.source_address);
		default:
			break;
		}
		return -1;
	}

	private void validate() throws Exception {

		switch (this.addressing_mode) {
		case AddressingMode.Normal_11bits:
		case AddressingMode.Normal_29bits:
			if (this.rxid == null || this.txid == null) {
				throw new Exception("txid and rxid must be specified for Normal addressing mode (11 bits ID)");
			}
			if (this.rxid == this.txid) {
				throw new Exception("txid and rxid must be different for Normal addressing mode");
			}
			break;
		case AddressingMode.NormalFixed_29bits:
			if (this.target_address == null || this.source_address == null) {
				throw new Exception(
						"target_address and source_address must be specified for Normal Fixed addressing (29 bits ID)");
			}
			break;
		case AddressingMode.Extended_11bits:
		case AddressingMode.Extended_29bits:
			if (this.target_address == null || this.rxid == null || this.txid == null) {
				throw new Exception("target_address, rxid and txid must be specified for Extended addressing mode");
			}
			if (this.rxid == this.txid) {
				throw new Exception("txid and rxid must be different!");
			}
			break;
		case AddressingMode.Mixed_11bits:
			if (this.address_extension == null || this.rxid == null || this.txid == null) {
				throw new Exception("rxid, txid and address_extension must be specified for Mixed addressing mode");
			}
			break;
		case AddressingMode.Mixed_29bits:
			if (this.target_address == null || this.source_address == null || this.address_extension == null) {
				throw new Exception(
						"target_address, source_address and address_extension must be specified for Mixed addressing mode");
			}
			break;
		default:
			throw new Exception("Addressing mode is not valid");
		}

		if (this.target_address != null) {
			if (this.target_address < 0 || this.target_address > 0xFF) {
				throw new Exception("target_address must be an integer between 0x00 and 0xFF");
			}
		}
		if (this.source_address != null) {
			if (this.source_address < 0 || this.source_address > 0xFF) {
				throw new Exception("source_address must be an integer between 0x00 and 0xFF");
			}
		}
		if (this.address_extension != null) {
			if (this.address_extension < 0 || this.address_extension > 0xFF) {
				throw new Exception("address_extension must be an integer between 0x00 and 0xFF");
			}
		}
		if (this.txid != null) {
			if (this.txid < 0) {
				throw new Exception("txid must be greater than 0");
			} else if (!this.is_29bits && this.txid > 0x7FF) {
				throw new Exception("txid must be smaller than 0x7FF for 11 bits identifier");

			}
		}
		if (this.rxid != null) {
			if (this.rxid < 0) {
				throw new Exception("rxid must be greater than 0");
			} else if (!this.is_29bits && this.rxid > 0x7FF) {
				throw new Exception("rxid must be smaller than 0x7FF for 11 bits identifier");

			}
		}
	}

	public int get_tx_arbitration_id(int address_type) {
		if (address_type == TargetAddressType.Physical) {
			return this.tx_arbitration_id_physical;
		} else {
			return this.tx_arbitration_id_functional;
		}
	}

	public int get_tx_arbitration_id() {
		return this.get_tx_arbitration_id(TargetAddressType.Physical);
	}

	public int get_rx_arbitration_id(int address_type) {
		if (address_type == TargetAddressType.Physical) {
			return this.rx_arbitration_id_physical;
		} else {
			return this.rx_arbitration_id_functional;
		}
	}

	public int get_rx_arbitration_id() {
		return this.get_rx_arbitration_id(TargetAddressType.Physical);
	}

}
