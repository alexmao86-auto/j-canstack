package udsoncan;

import org.junit.Test;

import can.interfaces.vector.VectorBus;
import isotp.address.Address;
import isotp.address.AddressingMode;
import isotp.protocol.CanStack;
import isotp.protocol.Params;
import isotp.protocol.TransportLayer;
import junit.framework.TestCase;
import udsoncan.connections.JavaIsoTpConnection;
import udsoncan.services.ECUReset;

public class ClientTest extends TestCase {

	public ClientTest() {
		// TODO Auto-generated constructor stub
	}

	public ClientTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Test
	public void test_client() throws Exception {

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
	}

	

}
