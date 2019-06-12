package udsoncan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import can.interfaces.vector.VectorBus;
import isotp.address.Address;
import isotp.address.AddressingMode;
import isotp.protocol.CanStack;
import isotp.protocol.Params;
import isotp.protocol.TransportLayer;
import udsoncan.connections.JavaIsoTpConnection;
import udsoncan.services.ECUReset;
import udsoncan.services.ReadDataByIdentifier;

public class Test1 {

	public static void main(String[] args) throws Exception {
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
//		client.ecu_reset(ECUReset.ResetType.hardReset);
//		client.change_session(2);
//		client.request_seed(1);

		List<Integer> didlist = new ArrayList<Integer>();
		didlist.add(0xF113);
		Response response = client.read_data_by_identifier(didlist);
		Map<Integer, Object> values = ((ReadDataByIdentifier.ResponseData) response.service_data).values;
		for (int k : values.keySet()) {
			System.out.println(String.format("DID:0x%x = [%s]", k, Arrays.toString((byte[]) values.get(k))));
		}
		client.close();
	}

}
