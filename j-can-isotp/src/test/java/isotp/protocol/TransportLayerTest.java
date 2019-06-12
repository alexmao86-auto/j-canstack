package isotp.protocol;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

import isotp.TransportLayerBaseTest;
import isotp.address.Address;
import isotp.address.AddressingMode;
import isotp.errors.ConsecutiveFrameTimeoutError;
import isotp.errors.FlowControlTimeoutError;
import isotp.errors.InvalidCanDataError;
import isotp.errors.MaximumWaitFrameReachedError;
import isotp.errors.ReceptionInterruptedWithFirstFrameError;
import isotp.errors.ReceptionInterruptedWithSingleFrameError;
import isotp.errors.UnexpectedConsecutiveFrameError;
import isotp.errors.UnexpectedFlowControlError;
import isotp.errors.UnsuportedWaitFrameError;
import isotp.errors.ValueError;
import isotp.errors.WrongSequenceNumberError;
import junit.framework.TestCase;

//Check the behaviour of the transport layer. Sequenece of CAN frames, timings, etc.
public class TransportLayerTest extends TransportLayerBaseTest {
	int RXID = 0x456;
	int TXID = 0x123;

	public TransportLayerTest() {
		super();
	}

	@Override
	protected void setUp() throws Exception {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:SSS");

		System.out.println("Test Start @" + dateFormat.format(calendar.getTime()) + ": " + this.getName());
		super.setUp();

		Params params = new Params();
		params.set("stmin", 1);
		params.set("blocksize", 8);
		params.set("squash_stmin_requirement", false);
		params.set("rx_flowcontrol_timeout", 1000l);
		params.set("rx_consecutive_frame_timeout", 1000l);
		params.set("wftmax", 0);

		Address address = new Address(AddressingMode.Normal_11bits, null, null, null, this.TXID, this.RXID);
		this.stack = new TransportLayer(this.stack_rxfn, this.stack_txfn, address, this.error_handler, params);
		this.init_test_case();
	}

	@Override
	protected void tearDown() throws Exception {
		Calendar calendar = Calendar.getInstance();
		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss:SSS");
		System.out.println("End @ " + dateFormat.format(calendar.getTime()));
		super.tearDown();
	}

	public void simulate_rx(byte[] data, int rxid) {
		this.simulate_rx_msg(new CanMessage(rxid, null, data, null));
	}

	public void simulate_rx(byte[] data) {
		this.simulate_rx_msg(new CanMessage(this.RXID, null, data, null));
	}

	public void simulate_rx_flowcontrol(int flow_status, int stmin, int blocksize, byte[] prefix) {
		byte[] data;
		int index = 0;
		int datalen = 8;

		if (prefix != null) {
			datalen = 3 + prefix.length;
		}
		data = new byte[datalen];

		if (prefix != null) {
			System.arraycopy(prefix, 0, data, 0, prefix.length);
			index = prefix.length;
		}

		data[index++] = (byte) (0x30 | (flow_status & 0xF));
		data[index++] = (byte) blocksize;
		data[index++] = (byte) stmin;

		this.simulate_rx(data);
	}

	// Make sure we can receive a single frame
	@Test
	public void test_receive_single_sf() throws Exception {
		this.simulate_rx(new byte[] { 0x05, 0x11, 0x22, 0x33, 0x44, 0x55 });
		this.stack.process();
		Assert.assertArrayEquals(this.rx_isotp_frame(), new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55 });
	}

	// Make sure we can receive multiple single frame
	@Test
	public void test_receive_multiple_sf() throws Exception {
		this.stack.process();
		this.stack.process();

		this.simulate_rx(new byte[] { 0x05, 0x11, 0x22, 0x33, 0x44, 0x55 });
		this.stack.process();
		Assert.assertArrayEquals(this.rx_isotp_frame(), new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55 });

		TestCase.assertNull(this.rx_isotp_frame());
		this.stack.process();
		TestCase.assertNull(this.rx_isotp_frame());

		this.simulate_rx(new byte[] { 0x05, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE });
		this.stack.process();
		Assert.assertArrayEquals(this.rx_isotp_frame(),
				new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE });

		TestCase.assertNull(this.rx_isotp_frame());
		this.stack.process();
		TestCase.assertNull(this.rx_isotp_frame());
	}

	@Test
	public void test_receive_multiple_sf_single_process_call() throws Exception {
		this.simulate_rx(new byte[] { 0x05, 0x11, 0x22, 0x33, 0x44, 0x55 });
		this.simulate_rx(new byte[] { 0x05, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE });
		this.stack.process(); // Call process once
		Assert.assertArrayEquals(this.rx_isotp_frame(), new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55 });
		Assert.assertArrayEquals(this.rx_isotp_frame(),
				new byte[] { (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE });
		TestCase.assertNull(this.rx_isotp_frame());
	}

	@Test
	public void test_receive_multiframe() throws Exception {
		int payload_size = 10;
		byte[] payload = this.make_payload(payload_size);
		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 6);
		this.simulate_rx(data);
		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 4);
		this.simulate_rx(data);

		this.stack.process();
		Assert.assertArrayEquals(this.rx_isotp_frame(), payload);
		TestCase.assertNull(this.rx_isotp_frame());
	}

	@Test
	public void test_receive_2_multiframe() throws Exception {
		int payload_size = 10;
		byte[] payload = this.make_payload(payload_size);

		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 6);
		this.simulate_rx(data);
		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 4);
		this.simulate_rx(data);

		data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 6);
		this.simulate_rx(data);
		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 4);
		this.simulate_rx(data);

		this.stack.process();
		Assert.assertArrayEquals(this.rx_isotp_frame(), payload);
		Assert.assertArrayEquals(this.rx_isotp_frame(), payload);
		TestCase.assertNull(this.rx_isotp_frame());
	}

	@Test
	public void test_receive_multiframe_check_flowcontrol() throws Exception {
		this.stack.params.set("stmin", 0x02);
		this.stack.params.set("blocksize", 0x05);

		int payload_size = 10;
		byte[] payload = this.make_payload(payload_size);
		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 6);
		this.simulate_rx(data);
		this.stack.process();

		this.assert_sent_flow_control(2, 5, null, null, null);
		TestCase.assertNull(this.rx_isotp_frame());
		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 4);
		this.simulate_rx(data);
		this.stack.process();
		Assert.assertArrayEquals(this.rx_isotp_frame(), payload);
		TestCase.assertNull(this.rx_isotp_frame());
	}

	@Test
	public void test_receive_multiframe_flowcontrol_padding() throws Exception {
		int padding_byte = 0x22;
		this.stack.params.set("tx_padding", padding_byte);
		this.stack.params.set("stmin", 0x02);
		this.stack.params.set("blocksize", 0x05);
		int payload_size = 10;

		byte[] payload = this.make_payload(payload_size);
		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 6);
		this.simulate_rx(data);
		this.stack.process();

		this.assert_sent_flow_control(2, 5, null, (byte) padding_byte, null);
		TestCase.assertNull(this.rx_isotp_frame());
		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 4);
		this.simulate_rx(data);
		this.stack.process();
		Assert.assertArrayEquals(this.rx_isotp_frame(), payload);
		TestCase.assertNull(this.rx_isotp_frame());
	}

	@Test
	public void test_long_multiframe_2_flow_control() throws Exception {
		int payload_size = 30;
		byte[] payload = this.make_payload(payload_size);
		this.stack.params.set("stmin", 0x05);
		this.stack.params.set("blocksize", 0x03);

		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 6);
		this.simulate_rx(data);
		this.stack.process();
		this.assert_sent_flow_control(5, 3, null, null, null);
		TestCase.assertNull(this.rx_isotp_frame());

		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 7);
		this.simulate_rx(data);
		this.stack.process();
		TestCase.assertNull(this.rx_isotp_frame());

		data = new byte[8];
		data[0] = 0x22;
		System.arraycopy(payload, 13, data, 1, 7);
		this.simulate_rx(data);
		this.stack.process();
		TestCase.assertNull(this.rx_isotp_frame());

		data = new byte[8];
		data[0] = 0x23;
		System.arraycopy(payload, 20, data, 1, 7);
		this.simulate_rx(data);
		this.stack.process();
		TestCase.assertNull(this.rx_isotp_frame());

		data = new byte[8];
		data[0] = 0x24;
		System.arraycopy(payload, 27, data, 1, 3);
		this.simulate_rx(data);
		this.stack.process();
		Assert.assertThat(this.rx_isotp_frame(), is(payload));
		TestCase.assertNull(this.rx_isotp_frame());
	}

	@Test
	public void test_receive_multiframe_bad_seqnum() throws Exception {

		int payload_size = 10;
		byte[] payload = this.make_payload(payload_size);

		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 6);
		this.simulate_rx(data);

		data = new byte[8];
		data[0] = 0x22; // Bad Sequence number
		System.arraycopy(payload, 6, data, 1, 4);
		this.simulate_rx(data);

		this.stack.process();
		TestCase.assertNull(this.rx_isotp_frame());
		TestCase.assertNull(this.get_tx_can_msg()); // Do not send flow control
		this.assert_error_triggered(WrongSequenceNumberError.class.getName());
	}

	@Test
	public void test_receive_timeout_consecutive_frame_after_first_frame() throws Exception {
		this.stack.params.set("rx_consecutive_frame_timeout", 200l);

		int payload_size = 10;
		byte[] payload = this.make_payload(payload_size);

		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 6);
		this.simulate_rx(data);
		this.stack.process();

		Thread.sleep(200); // Should stop receivving after 200 msec

		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 4);
		this.simulate_rx(data);
		this.stack.process();

		TestCase.assertNull(this.rx_isotp_frame());// no message received indeed
		this.assert_error_triggered(ConsecutiveFrameTimeoutError.class.getName());
		this.assert_error_triggered(UnexpectedConsecutiveFrameError.class.getName());
	}

	@Test
	public void test_receive_recover_timeout_consecutive_frame() throws Exception {
		this.stack.params.set("rx_consecutive_frame_timeout", 200L);

		int payload_size = 10;
		byte[] payload1 = this.make_payload(payload_size);
		byte[] payload2 = this.make_payload(payload_size, 1);
		Assert.assertThat(payload1, not(payload2));

		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload1, 0, data, 2, 6);
		this.simulate_rx(data);
		this.stack.process();

		// TODO: The test result is not stable, sometimes it can pass and sometimes it
		// will fail!!!
		// see why it show this behavior

		/*
		 * junit.framework.AssertionFailedError: Expected: <null> but was: [B@5e0cd11 at
		 * junit.framework.Assert.fail(Assert.java:57) at
		 * junit.framework.Assert.assertTrue(Assert.java:22) at
		 * junit.framework.Assert.assertNull(Assert.java:277) at
		 * junit.framework.Assert.assertNull(Assert.java:268) at
		 * junit.framework.TestCase.assertNull(TestCase.java:438) at
		 * isotp.protocol.TransportLayerTest.
		 * test_receive_recover_timeout_consecutive_frame(TransportLayerTest.java:351)
		 */
		Thread.sleep(200); // Should stop receivving after 200 msec

		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload1, 6, data, 1, 4);
		this.simulate_rx(data);
		this.stack.process();
		TestCase.assertNull(this.rx_isotp_frame());// no message received indeed

		data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload2, 0, data, 2, 6);
		this.simulate_rx(data);

		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload2, 6, data, 1, 4);
		this.simulate_rx(data);
		this.stack.process();
		Assert.assertThat(this.rx_isotp_frame(), is(payload2));// Correctly received

		this.assert_error_triggered(ConsecutiveFrameTimeoutError.class.getName());
		this.assert_error_triggered(UnexpectedConsecutiveFrameError.class.getName());
	}

	@Test
	public void test_receive_multiframe_interrupting_another() throws Exception {
		this.stack.params.set("rx_consecutive_frame_timeout", 200L);

		int payload_size = 10;
		byte[] payload1 = this.make_payload(payload_size);
		byte[] payload2 = this.make_payload(payload_size, 1);
		Assert.assertThat(payload1, not(payload2));

		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload1, 0, data, 2, 6);
		this.simulate_rx(data);

		data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload2, 0, data, 2, 6);
		this.simulate_rx(data); // New frame interrupting previous

		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload2, 6, data, 1, 4);
		this.simulate_rx(data);

		this.stack.process();
		Assert.assertThat(this.rx_isotp_frame(), is(payload2));
		TestCase.assertNull(this.rx_isotp_frame());
		this.assert_error_triggered(ReceptionInterruptedWithFirstFrameError.class.getName());
	}

	@Test
	public void test_receive_single_frame_interrupt_multiframe_then_recover() throws Exception {
		this.stack.params.set("rx_consecutive_frame_timeout", 200L);

		int payload_size1 = 16;
		int payload_size2 = 16;
		byte[] payload1 = this.make_payload(payload_size1);
		byte[] payload2 = this.make_payload(payload_size2, 1);
		byte[] sf_payload = this.make_payload(5, 2);

		Assert.assertThat(payload1, not(payload2));

		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size1;
		System.arraycopy(payload1, 0, data, 2, 6);
		this.simulate_rx(data);

		this.stack.process();

		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload1, 6, data, 1, 7);
		this.simulate_rx(data);

		data = new byte[8];
		data[0] = 0x05;
		System.arraycopy(sf_payload, 0, data, 1, 5);
		this.simulate_rx(data);

		data = new byte[8];
		data[0] = 0x10;
		data[1] = (byte) payload_size2;
		System.arraycopy(payload2, 0, data, 2, 6);
		this.simulate_rx(data);

		this.stack.process();

		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload2, 6, data, 1, 7);
		this.simulate_rx(data);

		data = new byte[8];
		data[0] = 0x22;
		System.arraycopy(payload2, 13, data, 1, 3);
		this.simulate_rx(data);

		this.stack.process();

		Assert.assertThat(this.rx_isotp_frame(), is(sf_payload));
		Assert.assertThat(this.rx_isotp_frame(), is(payload2));
		TestCase.assertNull(this.rx_isotp_frame());
		this.assert_error_triggered(ReceptionInterruptedWithSingleFrameError.class.getName());
	}

	@Test
	public void test_receive_4095_multiframe() throws Exception {

		int payload_size = 4095;
		byte[] payload = this.make_payload(payload_size);
		byte[] data = new byte[8];

		data[0] = 0x1F;
		data[1] = (byte) 0xFF;
		System.arraycopy(payload, 0, data, 2, 6);

		this.simulate_rx(data);

		int n = 6;
		int seqnum = 1;
		while (n < 4096) {
			data = new byte[8];
			data[0] = (byte) (0x20 | (seqnum & 0xF));
			System.arraycopy(payload, n, data, 1, (4095 - n) < 6 ? 4095 - n : 7);
			this.simulate_rx(data);
			this.stack.process();
			n += 7;
			seqnum++;
		}
		Assert.assertThat(this.rx_isotp_frame(), is(payload));
		TestCase.assertNull(this.rx_isotp_frame());
	}

	@Test
	public void test_receive_4095_multiframe_check_blocksize() throws Exception {
		for (int blocksize = 1; blocksize <= 10; blocksize++) {
			this.perform_receive_4095_multiframe_check_blocksize(blocksize);
		}
	}

	private void perform_receive_4095_multiframe_check_blocksize(int blocksize) throws Exception {
		int payload_size = 4095;
		this.stack.params.set("blocksize", blocksize);
		this.stack.params.set("stmin", 2);
		byte[] payload = this.make_payload(payload_size);
		byte[] data = new byte[8];

		data[0] = 0x1F;
		data[1] = (byte) 0xFF;
		System.arraycopy(payload, 0, data, 2, 6);

		this.simulate_rx(data);
		this.stack.process();
		this.assert_sent_flow_control(2, blocksize, null, null, String.format("blocksize=%d", blocksize));
		int n = 6;
		int seqnum = 1;
		int block_counter = 0;

		while (n < 4096) {
			data = new byte[8];
			data[0] = (byte) (0x20 | (seqnum & 0xF));
			System.arraycopy(payload, n, data, 1, (4095 - n) < 6 ? 4095 - n : 7);
			this.simulate_rx(data);
			this.stack.process();
			n += 7;
			seqnum++;
			block_counter++;
			if ((block_counter % blocksize == 0) && (n < 4095)) {
				this.assert_sent_flow_control(2, blocksize, null, null, String.format("blocksize=%d", blocksize));
			} else {
				TestCase.assertNull(
						String.format("Sent a message something after block %d but shoud not have. blocksize = %d",
								block_counter, blocksize),
						this.get_tx_can_msg());
			}
		}
		Assert.assertThat(this.rx_isotp_frame(), is(payload));
		TestCase.assertNull(String.format("blocksize=%d", blocksize), this.rx_isotp_frame());

	}

	@Test
	public void test_receive_invalid_can_message() throws Exception {
		for (int i = 4; i <= 0x10; i++) {
			byte[] data = new byte[] { (byte) (i << 4), 0, 0 };
			this.simulate_rx(data);
			this.stack.process();
			this.assert_error_triggered(InvalidCanDataError.class.getName());
			this.clear_errors();
		}
	}

	@Test
	public void test_receive_data_length_12_bytes() throws Exception {
		this.stack.params.set("ll_data_length", 12);
		int payload_size = 30;
		byte[] payload = this.make_payload(payload_size);
		byte[] data = new byte[12];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 10);
		this.simulate_rx(data);
		this.stack.process();

		data = new byte[12];
		data[0] = 0x21;
		System.arraycopy(payload, 10, data, 1, 11);
		this.simulate_rx(data);

		data = new byte[12];
		data[0] = 0x22;
		System.arraycopy(payload, 21, data, 1, 9);
		this.simulate_rx(data);
		this.stack.process();

		Assert.assertArrayEquals(this.rx_isotp_frame(), payload);
	}

	@Test
	public void test_receive_data_length_5_bytes() throws Exception {
		this.stack.params.set("ll_data_length", 5);
		int payload_size = 15;
		byte[] payload = this.make_payload(payload_size);
		byte[] data = new byte[5];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 3);
		this.simulate_rx(data);
		this.stack.process();

		data = new byte[5];
		data[0] = 0x21;
		System.arraycopy(payload, 3, data, 1, 4);
		this.simulate_rx(data);

		data = new byte[5];
		data[0] = 0x22;
		System.arraycopy(payload, 7, data, 1, 4);
		this.simulate_rx(data);

		data = new byte[5];
		data[0] = 0x23;
		System.arraycopy(payload, 11, data, 1, 4);
		this.simulate_rx(data);
		this.stack.process();

		Assert.assertArrayEquals(this.rx_isotp_frame(), payload);
	}

	@Test
	public void test_receive_data_length_12_but_set_8_bytes() throws Exception {
		this.stack.params.set("ll_data_length", 8);
		int payload_size = 30;
		byte[] payload = this.make_payload(payload_size);
		byte[] data = new byte[12];
		data[0] = 0x10;
		data[1] = (byte) payload_size;
		System.arraycopy(payload, 0, data, 2, 10);
		this.simulate_rx(data);
		this.stack.process();

		data = new byte[12];
		data[0] = 0x21;
		System.arraycopy(payload, 10, data, 1, 11);
		this.simulate_rx(data);

		data = new byte[12];
		data[0] = 0x22;
		System.arraycopy(payload, 21, data, 1, 9);
		this.simulate_rx(data);
		this.stack.process();

		Assert.assertThat(this.rx_isotp_frame(), not(payload));
	}

	// ================ Transmission ====================

	CanMessage assert_tx_timing_spin_wait_for_msg(long mintime, long maxtime) throws Exception {
		CanMessage msg = null;
		long diff = 0;
		long t = System.currentTimeMillis();
		while (msg == null) {
			this.stack.process();
			msg = this.get_tx_can_msg();
			diff = System.currentTimeMillis() - t;
			TestCase.assertTrue("Timed out", diff < maxtime);
		}
		TestCase.assertTrue(String.format("Stack sent a message too quickly: diff=%d,mintime=%d", diff, mintime),
				diff > mintime);
		return msg;
	}

	@Test
	public void test_stmin_requirement() throws Exception {
		int stmin = 100;
		int payload_size = 30;
		int blocksize = 3;
		byte[] payload = this.make_payload(payload_size);
		this.tx_isotp_frame(payload);
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();

		byte[] data = new byte[8];
		data[0] = (byte) (0x10 | ((payload_size >> 8) & 0xF));
		data[1] = (byte) (payload_size & 0xFF);
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat("stmin = " + stmin, msg.data, is(data));

		this.simulate_rx_flowcontrol(0, stmin, blocksize, null);
		msg = this.assert_tx_timing_spin_wait_for_msg(95, 1000);
		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 7);
		Assert.assertThat("stmin = " + stmin, msg.data, is(data));

		this.simulate_rx_flowcontrol(0, stmin, blocksize, null);
		msg = this.assert_tx_timing_spin_wait_for_msg(95, 1000);
		data = new byte[8];
		data[0] = 0x22;
		System.arraycopy(payload, 13, data, 1, 7);
		Assert.assertThat("stmin = " + stmin, msg.data, is(data));

		this.simulate_rx_flowcontrol(0, stmin, blocksize, null);
		msg = this.assert_tx_timing_spin_wait_for_msg(95, 1000);
		data = new byte[8];
		data[0] = 0x23;
		System.arraycopy(payload, 20, data, 1, 7);
		Assert.assertThat("stmin = " + stmin, msg.data, is(data));

		this.simulate_rx_flowcontrol(0, stmin, blocksize, null);

		this.simulate_rx_flowcontrol(0, stmin, blocksize, null);
		msg = this.assert_tx_timing_spin_wait_for_msg(95, 1000);
		data = new byte[4];
		data[0] = 0x24;
		System.arraycopy(payload, 27, data, 1, 3);
		Assert.assertThat("stmin = " + stmin, msg.data, is(data));
	}

	@Test
	public void test_send_single_frame() throws Exception {
		for (int i = 1; i < 7; i++) {
			byte[] payload = this.make_payload(i, i);
			TestCase.assertNull(this.get_tx_can_msg());
			this.tx_isotp_frame(payload);
			this.stack.process();
			CanMessage msg = this.get_tx_can_msg();
			TestCase.assertEquals(msg.arbitration_id, this.TXID);
			TestCase.assertEquals(msg.dlc.intValue(), i + 1);
			byte[] data = new byte[payload.length + 1];
			data[0] = (byte) i;
			System.arraycopy(payload, 0, data, 1, payload.length);
			Assert.assertThat(msg.data, is(data));
		}
	}

	@Test
	public void test_padding_single_frame() throws Exception {
		int padding_byte = 0xAA;
		this.stack.params.set("tx_padding", padding_byte);

		for (int i = 1; i < 7; i++) {
			byte[] payload = this.make_payload(i, i);
			TestCase.assertNull(this.get_tx_can_msg());
			this.tx_isotp_frame(payload);
			this.stack.process();
			CanMessage msg = this.get_tx_can_msg();
			TestCase.assertEquals(msg.arbitration_id, this.TXID);
			TestCase.assertEquals(msg.dlc.intValue(), 8);
			byte[] data = new byte[8];
			data[0] = (byte) i;
			System.arraycopy(payload, 0, data, 1, payload.length);
			for (int j = 7; j > payload.length; j--) {
				data[j] = (byte) padding_byte;
			}
			Assert.assertThat(msg.data, is(data));
		}
	}

	@Test
	public void test_padding_single_frame_dl_12_bytes() throws Exception {
		int padding_byte = 0xAA;
		this.stack.params.set("tx_padding", padding_byte);
		this.stack.params.set("ll_data_length", 12);

		for (int i = 1; i <= 11; i++) {
			byte[] payload = this.make_payload(i, i);
			TestCase.assertNull(this.get_tx_can_msg());
			this.tx_isotp_frame(payload);
			this.stack.process();
			CanMessage msg = this.get_tx_can_msg();
			TestCase.assertEquals(msg.arbitration_id, this.TXID);
			TestCase.assertEquals(msg.dlc.intValue(), 12);
			byte[] data = new byte[12];
			data[0] = (byte) i;
			System.arraycopy(payload, 0, data, 1, payload.length);
			for (int j = 11; j > payload.length; j--) {
				data[j] = (byte) padding_byte;
			}
			Assert.assertThat(msg.data, is(data));
		}
	}

	@Test
	public void test_send_multiple_single_frame_one_process() throws Exception {
		byte[][] payloads = new byte[8][];
		for (int i = 1; i < 8; i++) {
			byte[] payload = this.make_payload(i, i);
			this.tx_isotp_frame(payload);
			payloads[i - 1] = payload;
		}
		this.stack.process();
		for (int i = 1; i < 8; i++) {
			CanMessage msg = this.get_tx_can_msg();
			TestCase.assertNotNull(msg);
			TestCase.assertEquals(msg.arbitration_id, this.TXID);
			TestCase.assertEquals(msg.dlc.intValue(), i + 1);

			byte[] data = new byte[payloads[i - 1].length + 1];
			data[0] = (byte) i;
			System.arraycopy(payloads[i - 1], 0, data, 1, payloads[i - 1].length);
			Assert.assertThat(msg.data, is(data));
		}
	}

	@Test
	public void test_send_small_multiframe() throws Exception {
		byte[] payload = this.make_payload(10);
		TestCase.assertNull(this.get_tx_can_msg());
		this.tx_isotp_frame(payload);
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();
		TestCase.assertEquals(msg.arbitration_id, this.TXID);
		TestCase.assertEquals(msg.dlc.intValue(), 8);
		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = 0x0A;
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));
		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
		this.simulate_rx_flowcontrol(0, 0, 8, null);
		this.stack.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, this.TXID);
		TestCase.assertEquals(msg.dlc.intValue(), 5);
		data = new byte[5];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 4);
		Assert.assertThat(msg.data, is(data));
		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
	}

	@Test
	public void test_padding_multi_frame() throws Exception {
		int padding_byte = 0x55;
		this.stack.params.set("tx_padding", padding_byte);

		byte[] payload = this.make_payload(10);
		TestCase.assertNull(this.get_tx_can_msg());
		this.tx_isotp_frame(payload);
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();

		TestCase.assertEquals(msg.arbitration_id, this.TXID);
		TestCase.assertEquals(msg.dlc.intValue(), 8);
		byte[] data = new byte[8];
		data[0] = 0x10;
		data[1] = 0x0A;
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));
		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
		this.simulate_rx_flowcontrol(0, 0, 8, null);
		this.stack.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, this.TXID);
		TestCase.assertEquals(msg.dlc.intValue(), 8);
		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 4);
		for (int i = 5; i < 8; i++)
			data[i] = (byte) padding_byte;
		Assert.assertThat(msg.data, is(data));
		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
	}

	@Test
	public void test_padding_multi_frame_dl_12_bytes() throws Exception {
		int padding_byte = 0x55;
		this.stack.params.set("tx_padding", padding_byte);
		this.stack.params.set("ll_data_length", 12);
		byte[] payload = this.make_payload(15);
		TestCase.assertNull(this.get_tx_can_msg());
		this.tx_isotp_frame(payload);
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();
		TestCase.assertEquals(msg.arbitration_id, this.TXID);
		TestCase.assertEquals(msg.dlc.intValue(), 12);
		byte[] data = new byte[12];
		data[0] = 0x10;
		data[1] = 15;
		System.arraycopy(payload, 0, data, 2, 10);
		Assert.assertThat(msg.data, is(data));
		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
		this.simulate_rx_flowcontrol(0, 0, 8, null);
		this.stack.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		TestCase.assertEquals(msg.arbitration_id, this.TXID);
		TestCase.assertEquals(msg.dlc.intValue(), 12);
		data = new byte[12];
		data[0] = 0x21;
		System.arraycopy(payload, 10, data, 1, 5);
		for (int i = 6; i < 12; i++)
			data[i] = (byte) padding_byte;
		Assert.assertThat(msg.data, is(data));
		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
	}

	@Test
	public void test_send_2_small_multiframe() throws Exception {
		int payload_size = 10;
		CanMessage msg;
		byte[] data;
		byte[] payload1 = this.make_payload(payload_size);
		byte[] payload2 = this.make_payload(payload_size, 1);

		this.tx_isotp_frame(payload1);
		this.tx_isotp_frame(payload2);
		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[8];
		data[0] = 0x10;
		data[1] = 0x0A;
		System.arraycopy(payload1, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));

		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());

		this.simulate_rx_flowcontrol(0, 0, 8, null);
		this.stack.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		data = new byte[5];
		data[0] = 0x21;
		System.arraycopy(payload1, 6, data, 1, 4);
		Assert.assertThat(msg.data, is(data));
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		data = new byte[8];
		data[0] = 0x10;
		data[1] = 0x0A;
		System.arraycopy(payload2, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));
		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());

		this.simulate_rx_flowcontrol(0, 0, 8, null);
		this.stack.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		data = new byte[5];
		data[0] = 0x21;
		System.arraycopy(payload2, 6, data, 1, 4);
		Assert.assertThat(msg.data, is(data));
		msg = this.get_tx_can_msg();
		TestCase.assertNull(msg);
	}

	@Test
	public void test_send_multiframe_flow_control_timeout() throws Exception {
		this.stack.params.set("rx_flowcontrol_timeout", 200L);

		CanMessage msg;
		byte[] data;
		byte[] payload = this.make_payload(10);

		this.tx_isotp_frame(payload);
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[8];
		data[0] = 0x10;
		data[1] = 0x0A;
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));

		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
		Thread.sleep(200);
		this.stack.process();
		msg = this.get_tx_can_msg();
		TestCase.assertNull(msg);
		this.assert_error_triggered(FlowControlTimeoutError.class.getName());
	}

	@Test
	public void test_send_multiframe_flow_control_timeout_recover() throws Exception {
		this.stack.params.set("rx_flowcontrol_timeout", 200L);

		CanMessage msg;
		byte[] data;
		byte[] payload = this.make_payload(10);

		this.tx_isotp_frame(payload);
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[8];
		data[0] = 0x10;
		data[1] = 0x0A;
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));

		TestCase.assertNull(this.get_tx_can_msg());
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
		Thread.sleep(200);

		this.tx_isotp_frame(payload);
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[8];
		data[0] = 0x10;
		data[1] = 0x0A;
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));
		this.simulate_rx_flowcontrol(0, 0, 8, null);
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[5];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 4);
		Assert.assertThat(msg.data, is(data));
		this.assert_error_triggered(FlowControlTimeoutError.class.getName());
	}

	@Test
	public void test_send_unexpected_flow_control() throws Exception {
		this.simulate_rx_flowcontrol(0, 100, 8, null);
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
		this.assert_error_triggered(UnexpectedFlowControlError.class.getName());
	}

	@Test
	public void test_send_respect_wait_frame() throws Exception {
		this.stack.params.set("rx_flowcontrol_timeout", 500L);
		this.stack.params.set("wftmax", 15);

		CanMessage msg;
		byte[] data;
		byte[] payload = this.make_payload(20);

		this.tx_isotp_frame(payload);
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[8];
		data[0] = 0x10;
		data[1] = 20;
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));

		for (int i = 1; i < 10; i++) {
			this.simulate_rx_flowcontrol(1, 0, 1, null);
			this.stack.process();
			Thread.sleep(200);
		}

		this.simulate_rx_flowcontrol(0, 0, 1, null);
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[8];
		data[0] = 0x21;
		System.arraycopy(payload, 6, data, 1, 7);
		System.out.println("msg=" + Arrays.toString(msg.data));
		Assert.assertThat(msg.data, is(data));

		for (int i = 1; i < 10; i++) {
			this.simulate_rx_flowcontrol(1, 0, 1, null);
			this.stack.process();
			Thread.sleep(200);
		}

		this.simulate_rx_flowcontrol(0, 0, 1, null);
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[8];
		data[0] = 0x22;
		System.arraycopy(payload, 13, data, 1, 7);
		Assert.assertThat(msg.data, is(data));

		this.assert_no_error_triggered();
	}

	@Test
	public void test_send_respect_wait_frame_but_timeout() throws Exception {
		this.stack.params.set("rx_flowcontrol_timeout", 500L);
		this.stack.params.set("wftmax", 15);

		CanMessage msg;
		byte[] data;
		byte[] payload = this.make_payload(20);

		this.tx_isotp_frame(payload);
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[8];
		data[0] = 0x10;
		data[1] = 20;
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));

		for (int i = 1; i < 3; i++) {
			this.simulate_rx_flowcontrol(1, 0, 1, null);
			this.stack.process();
			Thread.sleep(200);
		}
		Thread.sleep(500);
		this.simulate_rx_flowcontrol(0, 0, 1, null);
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());

		this.assert_error_triggered(FlowControlTimeoutError.class.getName());
	}

	@Test
	public void test_send_wait_frame_after_first_frame_wftmax_0() throws Exception {
		this.stack.params.set("wftmax", 0);
		byte[] payload = this.make_payload(10);
		this.tx_isotp_frame(payload);
		this.stack.process();
		this.simulate_rx_flowcontrol(1, 0, 8, null);
		this.stack.process();
		Thread.sleep(10);
		this.simulate_rx_flowcontrol(0, 0, 8, null);
		this.stack.process();
		this.assert_error_triggered(UnsuportedWaitFrameError.class.getName());
	}

	@Test
	public void test_send_wait_frame_after_consecutive_frame_wftmax_0() throws Exception {
		this.stack.params.set("wftmax", 0);
		byte[] payload = this.make_payload(20);
		this.tx_isotp_frame(payload);
		this.stack.process();
		this.simulate_rx_flowcontrol(0, 0, 1, null);
		this.stack.process();
		this.simulate_rx_flowcontrol(1, 0, 1, null);
		this.stack.process();
		this.simulate_rx_flowcontrol(0, 0, 1, null);
		this.stack.process();
		this.assert_error_triggered(UnsuportedWaitFrameError.class.getName());
	}

	@Test
	public void test_send_wait_frame_after_first_frame_reach_max() throws Exception {
		this.stack.params.set("wftmax", 5);
		byte[] payload = this.make_payload(20);
		this.tx_isotp_frame(payload);
		this.stack.process();

		this.get_tx_can_msg();
		for (int i = 0; i < 6; i++) {
			this.simulate_rx_flowcontrol(1, 0, 1, null);
			this.stack.process();
			Thread.sleep(200);
		}
		this.simulate_rx_flowcontrol(0, 0, 1, null);
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
		this.assert_error_triggered(MaximumWaitFrameReachedError.class.getName());
		this.assert_error_triggered(UnexpectedFlowControlError.class.getName());
	}

	@Test
	public void test_send_wait_frame_after_conscutive_frame_reach_max() throws Exception {
		this.stack.params.set("wftmax", 5);

		byte[] payload = this.make_payload(20);
		this.tx_isotp_frame(payload);
		this.stack.process();
		this.get_tx_can_msg();
		this.simulate_rx_flowcontrol(0, 0, 1, null);
		this.stack.process();
		this.get_tx_can_msg();
		for (int i = 0; i < 6; i++) {
			this.simulate_rx_flowcontrol(1, 0, 1, null);
			this.stack.process();
			Thread.sleep(200);
		}
		this.simulate_rx_flowcontrol(0, 0, 1, null);
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
		this.assert_error_triggered(MaximumWaitFrameReachedError.class.getName());
		this.assert_error_triggered(UnexpectedFlowControlError.class.getName());
	}

	@Test
	public void test_send_4095_multiframe_zero_stmin() throws Exception {
		this.perform_multiframe_test_no_stmin(4095, 5);
	}

	@Test
	public void test_send_128_multiframe_variable_blocksize() throws Exception {
		for (int i = 1; i < 8; i++) {
			this.perform_multiframe_test_no_stmin(128, i);
		}
	}

	private void perform_multiframe_test_no_stmin(int payload_size, int blocksize) throws Exception {
		int stmin = 0;
		byte[] payload = this.make_payload(payload_size);
		this.tx_isotp_frame(payload);
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();
		TestCase.assertNotNull("blocksize = " + blocksize, msg);
		byte[] data = new byte[8];
		data[0] = (byte) (0x10 | ((payload_size >> 8) & 0xF));
		data[1] = (byte) (payload_size & 0xFF);
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat("blocksize = " + blocksize, msg.data, is(data));

		this.simulate_rx_flowcontrol(0, stmin, blocksize, null);

		int block_counter = 0;
		int seqnum = 1;
		int n = 6;

		this.stack.process();// Call only once, should enqueue all message until next flow control
		while (true) {
			msg = this.get_tx_can_msg();
			if (block_counter < blocksize) {
				TestCase.assertNotNull("blocksize = " + blocksize, msg);
				int newlen = (n + 7) <= payload_size ? 7 : payload_size - n;
				data = new byte[newlen + 1];
				data[0] = (byte) (0x20 | seqnum);
				System.arraycopy(payload, n, data, 1, newlen);
				Assert.assertThat("blocksize = " + blocksize, msg.data, is(data));

				n += 7;
				seqnum = (seqnum + 1) & 0xF;
				block_counter++;
				if (n > payload_size) {
					break;
				}

			} else {
				TestCase.assertNull("blocksize = " + blocksize, msg);
				this.simulate_rx_flowcontrol(0, stmin, blocksize, null);
				this.stack.process();
				block_counter = 0;
			}
		}
	}

	@Test
	public void test_squash_timing_requirement() throws Exception {
		this.stack.params.set("squash_stmin_requirement", true);
		int payload_size = 4095;
		int stmin = 100;
		int blocksize = 8;

		byte[] payload = this.make_payload(payload_size);
		this.tx_isotp_frame(payload);
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);
		byte[] data = new byte[8];
		data[0] = (byte) (0x10 | ((payload_size >> 8) & 0xF));
		data[1] = (byte) (payload_size & 0xFF);
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat("blocksize = " + blocksize, msg.data, is(data));

		this.simulate_rx_flowcontrol(0, stmin, blocksize, null);

		int block_counter = 0;
		int seqnum = 1;
		int n = 6;
		this.stack.process();// Call only once, should enqueue all message until next flow control
		while (true) {
			msg = this.get_tx_can_msg();
			if (block_counter < blocksize) {
				TestCase.assertNotNull("blocksize = " + blocksize, msg);
				int newlen = (n + 7) <= payload_size ? 7 : payload_size - n;
				data = new byte[newlen + 1];
				data[0] = (byte) (0x20 | seqnum);
				System.arraycopy(payload, n, data, 1, newlen);
				Assert.assertThat("blocksize = " + blocksize, msg.data, is(data));

				n += 7;
				seqnum = (seqnum + 1) & 0xF;
				block_counter++;
				if (n > payload_size) {
					break;
				}

			} else {
				TestCase.assertNull(msg);
				this.simulate_rx_flowcontrol(0, stmin, blocksize, null);
				this.stack.process(); // Receive the flow control and enqueue another block of can message.
				block_counter = 0;
			}
		}
	}

	@Test
	public void test_send_nothing_with_empty_payload() throws Exception {
		this.tx_isotp_frame(new byte[] {});
		this.tx_isotp_frame(new byte[] {});
		this.tx_isotp_frame(new byte[] {});
		this.stack.process();
		TestCase.assertNull(this.get_tx_can_msg());
	}

	@Test
	public void test_send_single_frame_after_empty_payload() throws Exception {
		this.tx_isotp_frame(new byte[] {});
		this.tx_isotp_frame(new byte[] { 0x55 });
		this.tx_isotp_frame(new byte[] {});
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();
		TestCase.assertNotNull(msg);

		Assert.assertThat(msg.data, is(new byte[] { 0x01, 0x55 }));
	}

	// Sets blocksize to 0, never sends flow control except after first frame
	@Test
	public void test_send_blocksize_zero() throws Exception {
		byte[] payload = this.make_payload(4095);
		this.tx_isotp_frame(payload);
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();

		byte[] data = new byte[8];
		data[0] = (byte) (0x1F);
		data[1] = (byte) (0xFF);
		System.arraycopy(payload, 0, data, 2, 6);
		Assert.assertThat(msg.data, is(data));

		this.simulate_rx_flowcontrol(0, 0, 0, null);

		int seqnum = 1;
		int n = 6;

		this.stack.process();
		while (true) {
			msg = this.get_tx_can_msg();
			TestCase.assertNotNull(msg);

			int newlen = (n + 7) <= 4095 ? 7 : 4095 - n;
			data = new byte[newlen + 1];
			data[0] = (byte) (0x20 | seqnum);
			System.arraycopy(payload, n, data, 1, newlen);
			Assert.assertThat(msg.data, is(data));

			n += 7;
			seqnum = (seqnum + 1) & 0xF;
			if (n > 4095) {
				break;
			}
		}
	}

	@Test
	public void test_transmit_data_length_12_bytes() throws Exception {
		this.stack.params.set("ll_data_length", 12);
		byte[] payload = this.make_payload(30);
		this.tx_isotp_frame(payload);
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();
		byte[] data = new byte[12];
		data[0] = (byte) (0x10);
		data[1] = (byte) 30;
		System.arraycopy(payload, 0, data, 2, 10);
		Assert.assertThat(msg.data, is(data));

		this.simulate_rx_flowcontrol(0, 0, 0, null);
		this.stack.process();
		msg = this.get_tx_can_msg();
		data = new byte[12];
		data[0] = (byte) (0x21);
		System.arraycopy(payload, 10, data, 1, 11);
		Assert.assertThat(msg.data, is(data));

		msg = this.get_tx_can_msg();
		data = new byte[10];
		data[0] = (byte) (0x22);
		System.arraycopy(payload, 21, data, 1, 9);
		Assert.assertThat(msg.data, is(data));
	}

	@Test
	public void test_transmit_data_length_5_bytes() throws Exception {
		this.stack.params.set("ll_data_length", 5);

		byte[] payload = this.make_payload(15);
		this.tx_isotp_frame(payload);
		this.stack.process();
		CanMessage msg = this.get_tx_can_msg();
		byte[] data = new byte[5];
		data[0] = (byte) (0x10);
		data[1] = (byte) 15;
		System.arraycopy(payload, 0, data, 2, 3);
		Assert.assertThat(msg.data, is(data));

		this.simulate_rx_flowcontrol(0, 0, 0, null);
		this.stack.process();

		msg = this.get_tx_can_msg();
		data = new byte[5];
		data[0] = (byte) (0x21);
		System.arraycopy(payload, 3, data, 1, 4);
		Assert.assertThat(msg.data, is(data));
		msg = this.get_tx_can_msg();
		data = new byte[5];
		data[0] = (byte) (0x22);
		System.arraycopy(payload, 7, data, 1, 4);
		Assert.assertThat(msg.data, is(data));
		msg = this.get_tx_can_msg();
		data = new byte[5];
		data[0] = (byte) (0x23);
		System.arraycopy(payload, 11, data, 1, 4);
		Assert.assertThat(msg.data, is(data));
	}

	private TransportLayer create_layer(Params params) throws Exception {
		Address address = new Address(AddressingMode.Normal_11bits, null, null, null, this.TXID, this.RXID);
		return new TransportLayer(this.stack_rxfn, this.stack_txfn, address, this.error_handler, params);
	}

	@Test
	public void test_params_bad_values() throws Exception {
		Params params = new Params();
		params.set("stmin", 1);
		params.set("blocksize", 8);
		params.set("squash_stmin_requirement", false);
		params.set("rx_flowcontrol_timeout", 1000l);
		params.set("rx_consecutive_frame_timeout", 1000l);

		this.create_layer(null); // Empty params. Use default value
		this.create_layer(params);

		try {
			params.set("stmin", -1);
			this.create_layer(params);
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		try {
			params.set("stmin", 0x100);
			this.create_layer(params);
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}
		params.set("stmin", 1);
		// not applicable for java
//		try {
//			params.set("stmin", "String");
//			this.create_layer(params);
//			fail("Expected an ValueError, but actual there isn't.");
//		} catch (ValueError e) {
//			TestCase.assertEquals(e.getClass(), ValueError.class);
//		}
		try {
			params.set("blocksize", -1);
			this.create_layer(params);
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}
		try {
			params.set("blocksize", 0x100);
			this.create_layer(params);
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}
		params.set("blocksize", 8);

		try {
			params.set("rx_flowcontrol_timeout", -1l);
			this.create_layer(params);
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		params.set("rx_flowcontrol_timeout", 1000l);

		try {
			params.set("rx_consecutive_frame_timeout", -1l);
			this.create_layer(params);
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		params.set("rx_consecutive_frame_timeout", 1000l);

	}
}
