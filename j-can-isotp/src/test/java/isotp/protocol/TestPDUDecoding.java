package isotp.protocol;

import static org.hamcrest.CoreMatchers.is;

import org.junit.Assert;
import org.junit.Test;

import isotp.errors.ValueError;
import junit.framework.TestCase;

public class TestPDUDecoding extends TestCase {

	public TestPDUDecoding() {
		// TODO Auto-generated constructor stub
	}

	public TestPDUDecoding(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	private PDU make_pdu(byte[] data, int start_of_data, int datalen) throws Exception {
		return new PDU(new CanMessage(-1, null, data, null), start_of_data, datalen);
	}

	private PDU make_pdu(byte[] data) throws Exception {
		return this.make_pdu(data, 0, 8);
	}

	private PDU make_pdu(byte[] data, int datalen) throws Exception {
		return this.make_pdu(data, 0, datalen);
	}

	@Test
	public void test_decode_single_frame() throws Exception {
		PDU frame;

		try {
			this.make_pdu(new byte[] {});
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		try {
			this.make_pdu(new byte[] { 0 });
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		for (int i = 8; i < 0xF; i++) {
			try {
				this.make_pdu(new byte[] { (byte) i });
				fail("Expected an ValueError, but actual there isn't.");
			} catch (ValueError e) {
				TestCase.assertEquals(e.getClass(), ValueError.class);
			}
		}

		try {
			this.make_pdu(new byte[] { 0x01 });
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		frame = this.make_pdu(new byte[] { 0x01, (byte) 0xAA });
		TestCase.assertEquals(frame.type, PDU.Type.SINGLE_FRAME);
		Assert.assertThat(frame.data, is(new byte[] { (byte) 0xAA }));
		TestCase.assertEquals(frame.length, frame.data.length);

		try {
			this.make_pdu(new byte[] { 0x02, 0x11 });
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		frame = this.make_pdu(new byte[] { 0x02, 0x11, 0x22 });
		TestCase.assertEquals(frame.type, PDU.Type.SINGLE_FRAME);
		Assert.assertThat(frame.data, is(new byte[] { 0x11, 0x22 }));
		TestCase.assertEquals(frame.length, frame.data.length);

		frame = this.make_pdu(new byte[] { 0x02, 0x11, 0x22, 0x33, 0x44 });
		TestCase.assertEquals(frame.type, PDU.Type.SINGLE_FRAME);
		Assert.assertThat(frame.data, is(new byte[] { 0x11, 0x22 }));
		TestCase.assertEquals(frame.length, frame.data.length);

		frame = this.make_pdu(new byte[] { 0x07, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77 });
		TestCase.assertEquals(frame.type, PDU.Type.SINGLE_FRAME);
		Assert.assertThat(frame.data, is(new byte[] { 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77 }));
		TestCase.assertEquals(frame.length, frame.data.length);

		try {
			this.make_pdu(new byte[] { 0x08, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 });
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		try {
			this.make_pdu(new byte[] { 0x05, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88 }, 5);
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		try {
			this.make_pdu(new byte[] { 0x01, 0x07, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77 }, 1, 8);
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

		try {
			this.make_pdu(new byte[] { 0x01, 0x06, 0x22, 0x33, 0x44, 0x55, 0x66 }, 1, 7);
			fail("Expected an ValueError, but actual there isn't.");
		} catch (ValueError e) {
			TestCase.assertEquals(e.getClass(), ValueError.class);
		}

	}

}
