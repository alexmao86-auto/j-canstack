# j-canstack
java can stack ( raw CAN, tp, UDS)


The java version of:

https://github.com/pylessard/python-udsoncan

https://github.com/pylessard/python-can-isotp

https://github.com/hardbyte/python-can/

Just for fan, not finished.

## Example
    VectorBus bus = new VectorBus(new int[] { 0 }, null, null, false, 125000, null, null, null, false, null, null,
				null, null, null, null, null, null);
		Address address = new Address(AddressingMode.Normal_11bits, null, null, null, 0x7a7, 0x7af);
		TransportLayer isotp_layer = new CanStack(bus, address);
		Params params = new Params();
		params.tx_padding = 0;
		isotp_layer.params = params;
		JavaIsoTpConnection conn = new JavaIsoTpConnection(isotp_layer, "Testing ISOTP");
		Client client = new Client(conn);
		client.open();
		client.ecu_reset(ECUReset.ResetType.hardReset);		
		client.close();
