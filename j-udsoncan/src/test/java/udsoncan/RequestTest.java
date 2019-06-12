package udsoncan;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;
import udsoncan.services.base.BaseService;

public class RequestTest extends TestCase {

	class DummyServiceNormal extends BaseService {
		public DummyServiceNormal() {
			super();
			this._sid = 0x13;
		}
	}

	class DummyServiceNoSubunction extends BaseService {
		public DummyServiceNoSubunction() {
			super();
			this._sid = 0x13;
			this._use_subfunction = false;
		}
	}

	public RequestTest() {
		// TODO Auto-generated constructor stub
	}

	public RequestTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Test
	public void test_create_from_instance_ok() throws Exception {
		Request req = new Request(new DummyServiceNormal());
		TestCase.assertEquals(req.service.request_id(), 0x13);
	}

	@Test
	public void test_create_from_class_ok() throws Exception {
		Request req = new Request(new DummyServiceNormal(), 0x44);
		TestCase.assertEquals(req.service.request_id(), 0x13);
	}

	@Test
	public void test_make_payload_basic() throws Exception {
		Request req = new Request(new DummyServiceNormal(), 0x44);

		Assert.assertArrayEquals(new byte[] { 0x13, 0x44 }, req.get_payload());
	}

	@Test
	public void test_make_payload_custom_data() throws Exception {
		Request req = new Request(new DummyServiceNormal(), 0x44);
		req.data = new byte[] { 0x12, 0x34, 0x56, 0x78 };
		Assert.assertArrayEquals(new byte[] { 0x13, 0x44, 0x12, 0x34, 0x56, 0x78 }, req.get_payload());
	}

	@Test
	public void test_make_payload_custom_data_no_subfunction() throws Exception {
		Request req = new Request(new DummyServiceNoSubunction(), 0x44);
		req.data = new byte[] { 0x12, 0x34, 0x56, 0x78 };
		Assert.assertArrayEquals(new byte[] { 0x13, 0x12, 0x34, 0x56, 0x78 }, req.get_payload());
	}

	@Test
	public void test_suppress_positive_response() throws Exception {
		Request req = new Request(new DummyServiceNormal(), 0x44, true);
		Assert.assertArrayEquals(new byte[] { 0x13, (byte) 0xC4 }, req.get_payload());
	}

	@Test
	public void test_suppress_positive_response_override() throws Exception {
		Request req = new Request(new DummyServiceNormal(), 0x44, false);
		Assert.assertArrayEquals(new byte[] { 0x13, (byte) 0xC4 }, req.get_payload(true));

		req = new Request(new DummyServiceNormal(), 0x44, true);
		Assert.assertArrayEquals(new byte[] { 0x13, (byte) 0x44 }, req.get_payload(false));
	}

	@Test
	public void test_from_payload_basic() throws Exception {
		byte[] payload = { 0x3E, 0x01 }; // 0x3e = TesterPresent
		Request req = Request.from_payload(payload);
		TestCase.assertEquals(req.service.request_id(), 0x3E);
		TestCase.assertEquals(req.subfunction.intValue(), 0x01);
		TestCase.assertFalse(req.suppress_positive_response);
	}

	@Test
	public void test_from_payload_suppress_positive_response() throws Exception {
		byte[] payload = { 0x3E, (byte) 0x81 }; // 0x3e = TesterPresent
		Request req = Request.from_payload(payload);
		TestCase.assertEquals(req.service.request_id(), 0x3E);
		TestCase.assertEquals(req.subfunction.intValue(), 0x01);
		TestCase.assertTrue(req.suppress_positive_response);
	}

	@Test
	public void test_from_payload_custom_data() throws Exception {
		byte[] payload = { 0x3E, (byte) 0x01, 0x12, 0x34, 0x56, 0x78 }; // 0x3e = TesterPresent
		Request req = Request.from_payload(payload);
		TestCase.assertEquals(req.service.request_id(), 0x3E);
		TestCase.assertEquals(req.subfunction.intValue(), 0x01);
		Assert.assertArrayEquals(req.data, new byte[] { 0x12, 0x34, 0x56, 0x78 });
	}

	@Test
	public void test_from_empty_payload() throws Exception {
		byte[] payload = {};
		Request req = Request.from_payload(payload);
		TestCase.assertNull(req.service);
		TestCase.assertNull(req.subfunction);
		TestCase.assertNull(req.data);
	}

	@Test
	public void test_from_bad_payload() throws Exception {
		byte[] payload = { (byte) 0xFF, (byte) 0xFF };
		Request req = Request.from_payload(payload);
		TestCase.assertNull(req.service);
		TestCase.assertNull(req.subfunction);
		TestCase.assertNull(req.data);
	}


}
