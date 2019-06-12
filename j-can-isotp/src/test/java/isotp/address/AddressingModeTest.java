package isotp.address;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import isotp.TransportLayerBaseTest;
import isotp.errors.IsoTpError;
import isotp.protocol.CanMessage;
import isotp.protocol.Params;
import isotp.protocol.TransportLayer;
import junit.framework.TestCase;

public class AddressingModeTest extends TransportLayerBaseTest {
	Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	protected void setUp() throws Exception {
		this.init_test_case();
	}

	@Test
	public void test_create_address() throws Exception {
		new Address(AddressingMode.Normal_11bits, null, null, null, 1, 2);
		new Address(AddressingMode.Normal_29bits, null, null, null, 1, 2);
		new Address(AddressingMode.NormalFixed_29bits, 1, 2, null, null, null);
		new Address(AddressingMode.Extended_11bits, 3, null, null, 1, 2);
		new Address(AddressingMode.Extended_29bits, 3, null, null, 1, 2);
		new Address(AddressingMode.Mixed_11bits, null, null, 3, 1, 2);
		new Address(AddressingMode.Mixed_29bits, 1, 2, 3, 1, 2);
	}

	@Test
	public void test_single_frame_only_function_tatype() throws Exception {
		int tatype = TargetAddressType.Functional;

		Address address = new Address(AddressingMode.Normal_11bits, null, null, null, 1, 2);
		TransportLayer layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, null);
		layer.send(this.make_payload(7), tatype);
		try {
			layer.send(this.make_payload(8), tatype);
		} catch (Exception e) {
			logger.warn(e.toString());
		}

		address = new Address(AddressingMode.Normal_29bits, null, null, null, 1, 2);
		layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, null);
		layer.send(this.make_payload(7), tatype);
		try {
			layer.send(this.make_payload(8), tatype);
		} catch (IsoTpError e) {
			logger.warn(e.toString());
		}

		address = new Address(AddressingMode.NormalFixed_29bits, 2, 1, null, null, null);
		layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, null);
		layer.send(this.make_payload(7), tatype);
		try {
			layer.send(this.make_payload(8), tatype);
		} catch (IsoTpError e) {
			logger.warn(e.toString());
		}

		address = new Address(AddressingMode.Extended_11bits, 3, null, null, 1, 2);
		layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, null);
		layer.send(this.make_payload(6), tatype);
		try {
			layer.send(this.make_payload(7), tatype);
		} catch (IsoTpError e) {
			logger.warn(e.toString());
		}

		address = new Address(AddressingMode.Extended_29bits, 3, null, null, 1, 2);
		layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, null);
		layer.send(this.make_payload(6), tatype);
		try {
			layer.send(this.make_payload(7), tatype);
		} catch (IsoTpError e) {
			logger.warn(e.toString());
		}
		address = new Address(AddressingMode.Mixed_11bits, null, null, 3, 1, 2);
		layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, null);
		layer.send(this.make_payload(6), tatype);
		try {
			layer.send(this.make_payload(7), tatype);
		} catch (IsoTpError e) {
			logger.warn(e.toString());
		}
		address = new Address(AddressingMode.Mixed_29bits, 2, 1, 3, 1, 2);
		layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, null);
		layer.send(this.make_payload(6), tatype);
		try {
			layer.send(this.make_payload(7), tatype);
		} catch (IsoTpError e) {
			logger.warn(e.toString());
		}

	}

	@Test
	public void test_11bits_normal_basic() throws Exception {
		int rxid = 0x123;
		int txid = 0x456;

		Address address = new Address(AddressingMode.Normal_11bits, null, null, null, txid, rxid);
//		CanMessage(int arbitration_id, int dlc, byte[] data, boolean extended_id)
		TestCase.assertTrue(address.is_for_me.check(new CanMessage(rxid, null, null, null)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, null, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid + 1, null, null, null)));

		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Physical), txid);
		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Functional), txid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Physical), rxid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Functional), rxid);
	}

	@Test
	public void test_11bits_normal_through_layer() throws Exception {
		int physical = TargetAddressType.Physical;
		int functional = TargetAddressType.Functional;
		int rxid = 0x123;
		int txid = 0x456;
		Params params = new Params();
		params.set("stmin", 0);
		params.set("blocksize", 0);
		Address address = new Address(AddressingMode.Normal_11bits, null, null, null, txid, rxid);
		TransportLayer layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, params);
		CanMessage msg;

		// Receive Single frame - Physical
		byte[] data = { 0x03, 0x01, 0x02, 0x03 };
		this.simulate_rx_msg(new CanMessage(rxid, null, data, false));
		layer.process();
		byte[] frame = layer.recv();
		TestCase.assertNotNull(frame);
		byte[] expect = { 0x01, 0x02, 0x03 };
		assertArrayEquals(expect, frame);

		// Receive Single frame - Functional
		layer.reset();
		data = new byte[] { 0x03, 0x01, 0x02, 0x03 };
		this.simulate_rx_msg(new CanMessage(rxid, null, data, false));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		expect = new byte[] { 0x01, 0x02, 0x03 };
		assertArrayEquals(expect, frame);

		// Receive multiframe - Physical
		layer.reset();
		data = new byte[] { 0x10, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 };
		this.simulate_rx_msg(new CanMessage(rxid, null, data, false));
		layer.process();
		this.assert_sent_flow_control(0, 0, null, null, null);
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { 0x21, 0x07, 0x08 }, false));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		expect = new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 };
		assertArrayEquals(expect, frame);

		// Transmit single frame - Physical / Functional
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x03, 0x04, 0x05, 0x06 });
		TestCase.assertFalse(msg.is_extended_id);

		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, functional);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x03, 0x04, 0x05, 0x06 });
		TestCase.assertFalse(msg.is_extended_id);

		// Transmit multiframe - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x10, 0x08, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 });
		TestCase.assertFalse(msg.is_extended_id);

		this.simulate_rx_msg(new CanMessage(rxid, null, this.make_flow_control_data(0, 0, 0, null), false));
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x21, 0x0A, 0x0B });
		TestCase.assertFalse(msg.is_extended_id);
	}

	@Test
	public void test_29bits_normal_basic() throws Exception {
		int rxid = 0x123456;
		int txid = 0x789ABC;

		Address address = new Address(AddressingMode.Normal_29bits, null, null, null, txid, rxid);
		TestCase.assertTrue(address.is_for_me.check(new CanMessage(rxid, null, null, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, null, false)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid + 1, null, null, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid + 1, null, null, true)));

		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Physical), txid);
		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Functional), txid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Physical), rxid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Functional), rxid);
	}

	public void test_29bits_normal_through_layer() throws Exception {
		int physical = TargetAddressType.Physical;
		int functional = TargetAddressType.Functional;
		int rxid = 0x123456;
		int txid = 0x789ABC;
		Params params = new Params();
		params.set("stmin", 0);
		params.set("blocksize", 0);
		Address address = new Address(AddressingMode.Normal_29bits, null, null, null, txid, rxid);
		TransportLayer layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, params);
		CanMessage msg;
		byte[] frame;

		// Receive Single frame - Physical
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { 0x03, 0x01, 0x02, 0x03 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03 });

		// Receive Single frame - Functional
		layer.reset();
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { 0x03, 0x01, 0x02, 0x03 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03 });

		// Receive multiframe - Physical
		layer.reset();
		this.simulate_rx_msg(
				new CanMessage(rxid, null, new byte[] { 0x10, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 }, true));
		layer.process();
		this.assert_sent_flow_control(0, 0, null, null, null);
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { 0x21, 0x07, 0x08 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 });

		// Transmit single frame - Physical / Functional
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x03, 0x04, 0x05, 0x06 });
		TestCase.assertTrue(msg.is_extended_id);

		// Transmit multiframe - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x10, 0x08, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 });
		TestCase.assertTrue(msg.is_extended_id);

		this.simulate_rx_msg(new CanMessage(rxid, null, this.make_flow_control_data(0, 0, 0, null), true));
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x21, 0x0A, 0x0B });
		TestCase.assertTrue(msg.is_extended_id);

		layer.reset();
	}

	@Test
	public void test_29bits_normal_fixed() throws Exception {
		int ta = 0x55;
		int sa = 0xAA;
		int rxid_physical = 0x18DAAA55;
		int rxid_functional = 0x18DBAA55;
		int txid_physical = 0x18DA55AA;
		int txid_functional = 0x18DB55AA;

		Address address = new Address(AddressingMode.NormalFixed_29bits, ta, sa, null, null, null);
		TestCase.assertTrue(address.is_for_me.check(new CanMessage(rxid_physical, null, null, true)));
		TestCase.assertTrue(address.is_for_me.check(new CanMessage(rxid_functional, null, null, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(txid_physical, null, null, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(txid_functional, null, null, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage((rxid_physical) & 0x7FF, null, null, false)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid_physical + 1, null, null, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage((rxid_physical + 1) & 0x7FF, null, null, false)));

		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Physical), txid_physical);
		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Functional), txid_functional);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Physical), rxid_physical);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Functional), rxid_functional);
	}

	@Test
	public void test_29bits_normal_fixed_through_layer() throws Exception {
		int physical = TargetAddressType.Physical;
		int functional = TargetAddressType.Functional;
		int ta = 0x55;
		int sa = 0xAA;
		int rxid_physical = 0x18DAAA55;
		int rxid_functional = 0x18DBAA55;
		int txid_physical = 0x18DA55AA;
		int txid_functional = 0x18DB55AA;

		Params params = new Params();
		params.set("stmin", 0);
		params.set("blocksize", 0);
		Address address = new Address(AddressingMode.NormalFixed_29bits, ta, sa, null, null, null);
		TransportLayer layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, params);
		CanMessage msg;
		byte[] frame;

		// Receive Single frame - Physical
		this.simulate_rx_msg(new CanMessage(rxid_physical, null, new byte[] { 0x03, 0x01, 0x02, 0x03 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03 });

		// Receive Single frame - Functional
		layer.reset();
		this.simulate_rx_msg(new CanMessage(rxid_functional, null, new byte[] { 0x03, 0x01, 0x02, 0x03 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03 });

		// Receive multiframe - Physical
		layer.reset();
		this.simulate_rx_msg(new CanMessage(rxid_functional, null,
				new byte[] { 0x10, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06 }, true));
		layer.process();
		this.assert_sent_flow_control(0, 0, null, null, null);
		this.simulate_rx_msg(new CanMessage(rxid_functional, null, new byte[] { 0x21, 0x07, 0x08 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 });

		// Transmit single frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid_physical);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x03, 0x04, 0x05, 0x06 });
		TestCase.assertTrue(msg.is_extended_id);

		// Transmit single frame - functional
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, functional);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid_functional);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x03, 0x04, 0x05, 0x06 });
		TestCase.assertTrue(msg.is_extended_id);

		// Transmit multiframe frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid_physical);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x10, 0x08, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09 });
		TestCase.assertTrue(msg.is_extended_id);

		this.simulate_rx_msg(new CanMessage(rxid_physical, null, this.make_flow_control_data(0, 0, 0, null), true));
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid_physical);
		Assert.assertArrayEquals(msg.data, new byte[] { 0x21, 0x0A, 0x0B });
		TestCase.assertTrue(msg.is_extended_id);

	}

	@Test
	public void test_11bits_extended() throws Exception {
		int rxid = 0x456;
		int txid = 0x123;
		int ta = 0xAA;
		int sa = 0x55;

		Address address = new Address(AddressingMode.Extended_11bits, ta, sa, null, txid, rxid);
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, null, false)));// no data
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(txid, null, null, false)));// No data, wrong id
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) ta }, false)));// wrong
																													// id
		TestCase.assertTrue(address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) sa }, false)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) sa }, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid + 1, null, new byte[] { (byte) sa }, false)));
		TestCase.assertFalse(
				address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) (sa + 1) }, false)));

		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Physical), txid);
		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Functional), txid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Physical), rxid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Functional), rxid);
	}

	@Test
	public void test_11bits_extended_through_layer() throws Exception {
		int rxid = 0x456;
		int txid = 0x123;
		int ta = 0xAA;
		int sa = 0x55;
		int physical = TargetAddressType.Physical;
		int functional = TargetAddressType.Functional;

		Address address = new Address(AddressingMode.Extended_11bits, ta, sa, null, txid, rxid);
		Params params = new Params();
		params.set("stmin", 0);
		params.set("blocksize", 0);
		TransportLayer layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, params);
		CanMessage msg;
		byte[] frame;

		// Receive Single frame - Physical / Functional
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { (byte) sa, 0x03, 0x01, 0x02, 0x03 }, false));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03 });

		// Receive multiframe - Physical
		layer.reset();
		this.simulate_rx_msg(
				new CanMessage(rxid, null, new byte[] { (byte) sa, 0x10, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05 }, false));
		layer.process();
		this.assert_sent_flow_control(0, 0, new byte[] { (byte) ta }, null, null);
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { (byte) sa, 0x21, 0x06, 0x07, 0x08 }, false));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 });

		// Transmit single frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertFalse(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ta, 0x03, 0x04, 0x05, 0x06 });

		// Transmit single frame - functional
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, functional);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertFalse(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ta, 0x03, 0x04, 0x05, 0x06 });

		// Transmit multi-frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertFalse(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ta, 0x10, 0x08, 0x04, 0x05, 0x06, 0x07, 0x08 });

		this.simulate_rx_msg(
				new CanMessage(rxid, null, this.make_flow_control_data(0, 0, 0, new byte[] { (byte) sa }), false));
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertFalse(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ta, 0x21, 0x09, 0x0A, 0x0B });

	}

	@Test
	public void test_29bits_extended() throws Exception {
		int rxid = 0x456;
		int txid = 0x123;
		int ta = 0xAA;
		int sa = 0x55;

		Address address = new Address(AddressingMode.Extended_29bits, ta, sa, null, txid, rxid);
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, null, true)));// no data
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(txid, null, null, true)));// No data, wrong id
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) ta }, true)));// wrong
																													// id
		TestCase.assertTrue(address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) sa }, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) sa }, false)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid + 1, null, new byte[] { (byte) sa }, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) (sa + 1) }, true)));

		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Physical), txid);
		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Functional), txid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Physical), rxid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Functional), rxid);
	}

	@Test
	public void test_29bits_extended_through_layer() throws Exception {
		int rxid = 0x456;
		int txid = 0x123;
		int ta = 0xAA;
		int sa = 0x55;
		int physical = TargetAddressType.Physical;
		int functional = TargetAddressType.Functional;

		Address address = new Address(AddressingMode.Extended_29bits, ta, sa, null, txid, rxid);
		Params params = new Params();
		params.set("stmin", 0);
		params.set("blocksize", 0);
		TransportLayer layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, params);
		CanMessage msg;
		byte[] frame;

		// Receive Single frame - Physical / Functional
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { (byte) sa, 0x03, 0x01, 0x02, 0x03 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03 });

		// Receive multiframe - Physical
		layer.reset();
		this.simulate_rx_msg(
				new CanMessage(rxid, null, new byte[] { (byte) sa, 0x10, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05 }, true));
		layer.process();
		this.assert_sent_flow_control(0, 0, new byte[] { (byte) ta }, null, null);
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { (byte) sa, 0x21, 0x06, 0x07, 0x08 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 });

		// Transmit single frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertTrue(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ta, 0x03, 0x04, 0x05, 0x06 });

		// Transmit single frame - functional
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, functional);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertTrue(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ta, 0x03, 0x04, 0x05, 0x06 });

		// Transmit multi-frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertTrue(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ta, 0x10, 0x08, 0x04, 0x05, 0x06, 0x07, 0x08 });

		this.simulate_rx_msg(
				new CanMessage(rxid, null, this.make_flow_control_data(0, 0, 0, new byte[] { (byte) sa }), true));
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertTrue(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ta, 0x21, 0x09, 0x0A, 0x0B });
	}

	@Test
	public void test_11bits_mixed() throws Exception {
		int rxid = 0x456;
		int txid = 0x123;
		int ae = 0x99;

		Address address = new Address(AddressingMode.Mixed_11bits, null, null, ae, txid, rxid);
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, null, false)));// no data
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(txid, null, null, false)));// No data, wrong id
		TestCase.assertTrue(address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) ae }, false)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) ae }, true)));
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid + 1, null, new byte[] { (byte) ae }, false)));
		TestCase.assertFalse(
				address.is_for_me.check(new CanMessage(rxid, null, new byte[] { (byte) (ae + 1) }, false)));

		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Physical), txid);
		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Functional), txid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Physical), rxid);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Functional), rxid);
	}

	@Test
	public void test_11bits_mixed_through_layer() throws Exception {
		int rxid = 0x456;
		int txid = 0x123;
		int ae = 0x99;
		int ta = 0xAA;
		int sa = 0x55;
		int physical = TargetAddressType.Physical;
		int functional = TargetAddressType.Functional;

		Address address = new Address(AddressingMode.Mixed_11bits, null, null, ae, txid, rxid);
		Params params = new Params();
		params.set("stmin", 0);
		params.set("blocksize", 0);
		TransportLayer layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, params);
		CanMessage msg;
		byte[] frame;

		// Receive Single frame - Physical / Functional
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { (byte) ae, 0x03, 0x01, 0x02, 0x03 }, false));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03 });

		// Receive multiframe - Physical
		layer.reset();
		this.simulate_rx_msg(
				new CanMessage(rxid, null, new byte[] { (byte) ae, 0x10, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05 }, false));
		layer.process();
		this.assert_sent_flow_control(0, 0, new byte[] { (byte) ae }, null, null);
		this.simulate_rx_msg(new CanMessage(rxid, null, new byte[] { (byte) ae, 0x21, 0x06, 0x07, 0x08 }, false));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 });

		// Transmit single frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertFalse(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ae, 0x03, 0x04, 0x05, 0x06 });

		// Transmit single frame - functional
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, functional);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertFalse(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ae, 0x03, 0x04, 0x05, 0x06 });

		// Transmit multi-frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertFalse(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ae, 0x10, 0x08, 0x04, 0x05, 0x06, 0x07, 0x08 });

		this.simulate_rx_msg(
				new CanMessage(rxid, null, this.make_flow_control_data(0, 0, 0, new byte[] { (byte) ae }), false));
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid);
		TestCase.assertFalse(msg.is_extended_id);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ae, 0x21, 0x09, 0x0A, 0x0B });
	}

	@Test
	public void test_29bits_mixed() throws Exception {
		int ta = 0x55;
		int sa = 0xAA;
		int ae = 0x99;
		int rxid_physical = 0x18CEAA55;
		int rxid_functional = 0x18CDAA55;
		int txid_physical = 0x18CE55AA;
		int txid_functional = 0x18CD55AA;

		Address address = new Address(AddressingMode.Mixed_29bits, ta, sa, ae, null, null);
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid_physical, null, null, true)));// No data
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(rxid_functional, null, null, true)));// No data
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(txid_physical, null, null, false)));// No data
		TestCase.assertFalse(address.is_for_me.check(new CanMessage(txid_functional, null, null, true)));// No data

		TestCase.assertTrue(
				address.is_for_me.check(new CanMessage(rxid_physical, null, new byte[] { (byte) ae }, true)));
		TestCase.assertFalse(
				address.is_for_me.check(new CanMessage(rxid_physical, null, new byte[] { (byte) ae }, false)));
		TestCase.assertTrue(
				address.is_for_me.check(new CanMessage(rxid_functional, null, new byte[] { (byte) ae }, true)));
		TestCase.assertFalse(
				address.is_for_me.check(new CanMessage(rxid_functional, null, new byte[] { (byte) ae }, false)));
		TestCase.assertFalse(
				address.is_for_me.check(new CanMessage(txid_physical, null, new byte[] { (byte) ae }, true)));
		TestCase.assertFalse(
				address.is_for_me.check(new CanMessage(txid_functional, null, new byte[] { (byte) ae }, true)));

		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Physical), txid_physical);
		TestCase.assertEquals(address.get_tx_arbitration_id(TargetAddressType.Functional), txid_functional);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Physical), rxid_physical);
		TestCase.assertEquals(address.get_rx_arbitration_id(TargetAddressType.Functional), rxid_functional);
	}

	@Test
	public void test_29bits_mixed_through_layer() throws Exception {
		int physical = TargetAddressType.Physical;
		int functional = TargetAddressType.Functional;
		int ta = 0x55;
		int sa = 0xAA;
		int ae = 0x99;
		int rxid_physical = 0x18CEAA55;
		int rxid_functional = 0x18CDAA55;
		int txid_physical = 0x18CE55AA;
		int txid_functional = 0x18CD55AA;

		Params params = new Params();
		params.set("stmin", 0);
		params.set("blocksize", 0);
		Address address = new Address(AddressingMode.Mixed_29bits, ta, sa, ae, null, null);
		TransportLayer layer = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, null, params);
		CanMessage msg;
		byte[] frame;

		// Receive Single frame - Physical
		this.simulate_rx_msg(
				new CanMessage(rxid_physical, null, new byte[] { (byte) ae, 0x03, 0x01, 0x02, 0x03 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03 });

		// Receive Single frame - Functional
		layer.reset();
		this.simulate_rx_msg(
				new CanMessage(rxid_functional, null, new byte[] { (byte) ae, 0x03, 0x01, 0x02, 0x03 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03 });

		// Receive multiframe - Physical
		layer.reset();
		this.simulate_rx_msg(new CanMessage(rxid_functional, null,
				new byte[] { (byte) ae, 0x10, 0x08, 0x01, 0x02, 0x03, 0x04, 0x05 }, true));
		layer.process();
		this.assert_sent_flow_control(0, 0, new byte[] { (byte) ae }, null, null);
		this.simulate_rx_msg(
				new CanMessage(rxid_functional, null, new byte[] { (byte) ae, 0x21, 0x06, 0x07, 0x08 }, true));
		layer.process();
		frame = layer.recv();
		TestCase.assertNotNull(frame);
		Assert.assertArrayEquals(frame, new byte[] { 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08 });

		// Transmit single frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid_physical);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ae, 0x03, 0x04, 0x05, 0x06 });
		TestCase.assertTrue(msg.is_extended_id);

		// Transmit single frame - Functional
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06 }, functional);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid_functional);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ae, 0x03, 0x04, 0x05, 0x06 });
		TestCase.assertTrue(msg.is_extended_id);

		// Transmit multiframe frame - Physical
		layer.reset();
		layer.send(new byte[] { 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b }, physical);
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid_physical);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ae, 0x10, 0x08, 0x04, 0x05, 0x06, 0x07, 0x08 });
		TestCase.assertTrue(msg.is_extended_id);

		this.simulate_rx_msg(new CanMessage(rxid_physical, null,
				this.make_flow_control_data(0, 0, 0, new byte[] { (byte) ae }), true));
		layer.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, txid_physical);
		Assert.assertArrayEquals(msg.data, new byte[] { (byte) ae, 0x21, 0x09, 0x0A, 0x0B });
		TestCase.assertTrue(msg.is_extended_id);

	}

}
