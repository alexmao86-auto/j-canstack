package udsoncan;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;
import udsoncan.services.base.BaseService;

public class ResponseTest extends TestCase {
	Logger logger = LoggerFactory.getLogger("ResponseTest");

	class DummyServiceNormal extends BaseService {

		@Override
		public int subfunction_id() {
			return 0x44;
		}

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

	class DummyServiceNoResponseData extends BaseService {

		public DummyServiceNoResponseData() {
			super();
			this._sid = 0x13;
			this._no_response_data = true;
		}

	}

	class RandomClass {

	}

	public ResponseTest() {
		// TODO Auto-generated constructor stub
	}

	public ResponseTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Test
	public void test_create_from_instance_ok() throws Exception {
		Response response = new Response(new DummyServiceNormal(), 0x22);
		TestCase.assertTrue(response.valid);
		TestCase.assertEquals(response.service.request_id(), 0x13);
		TestCase.assertEquals(response.code, 0x22);
	}

	// no need test weak type feature in Java
//	@Test
//	public void test_create_from_class_ok() throws Exception {
//		Response response = new Response(DummyServiceNormal.class, 0x22);
//		TestCase.assertTrue(response.valid);
//		TestCase.assertEquals(response.service.request_id(), 0x13);
//		TestCase.assertEquals(response.code, 0x22);
//	}

	@Test
	public void test_make_payload_basic_positive() throws Exception {
		Response response = new Response(new DummyServiceNormal(), 0x0, new byte[] { 0x01 });
		TestCase.assertTrue(response.valid);
		TestCase.assertTrue(response.positive);
		Assert.assertArrayEquals(new byte[] { 0x53, 0x01 }, response.get_payload());
	}

	@Test
	public void test_make_payload_basic_negative() throws Exception {
		Response response = new Response(new DummyServiceNormal(), 0x10);
		TestCase.assertTrue(response.valid);
		TestCase.assertFalse(response.positive);
		Assert.assertArrayEquals(new byte[] { 0x7F, 0x13, 0x10 }, response.get_payload());
	}

	@Test
	public void test_make_payload_custom_data_negative() throws Exception {
		Response response = new Response(new DummyServiceNormal(), 0x10);
		TestCase.assertTrue(response.valid);
		TestCase.assertFalse(response.positive);
		response.data = new byte[] { 0x12, 0x34, 0x56, 0x78 };
		Assert.assertArrayEquals(new byte[] { 0x7F, 0x13, 0x10, 0x12, 0x34, 0x56, 0x78 }, response.get_payload());
	}

	@Test
	public void test_from_payload_basic_positive() throws Exception {
		byte[] payload = { 0x7E, 0x00 }; // 0x7e = TesterPresent
		Response response = Response.from_payload(payload);
		TestCase.assertTrue(response.valid);
		TestCase.assertTrue(response.positive);
		TestCase.assertEquals(response.service.response_id(), 0x7E);
		TestCase.assertEquals(response.code, 0);
	}

	@Test
	public void test_from_payload_basic_negative() throws Exception {
		byte[] payload = { 0x7F, 0x3E, 0x10 }; // 0x3e = TesterPresent, 0x10 = General Reject
		Response response = Response.from_payload(payload);
		TestCase.assertTrue(response.valid);
		TestCase.assertFalse(response.positive);
		TestCase.assertEquals(response.service.response_id(), 0x7E);
		TestCase.assertEquals(response.code, 0x10);
	}

	@Test
	public void test_from_payload_custom_data_positive() throws Exception {
		byte[] payload = { 0x7E, 0x01, 0x12, 0x34, 0x56, 0x78 }; // 0x3E = TesterPresent
		Response response = Response.from_payload(payload);
		TestCase.assertTrue(response.valid);
		TestCase.assertTrue(response.positive);
		TestCase.assertEquals(response.service.response_id(), 0x7E);
		Assert.assertArrayEquals(response.data, new byte[] { 0x01, 0x12, 0x34, 0x56, 0x78 });
	}

	@Test
	public void test_from_payload_custom_data_negative() throws Exception {
		byte[] payload = { 0x7F, 0x3E, 0x10, 0x12, 0x34, 0x56, 0x78 }; // 0x3E = TesterPresent, 0x10 = General Reject
		Response response = Response.from_payload(payload);
		TestCase.assertTrue(response.valid);
		TestCase.assertFalse(response.positive);
		TestCase.assertEquals(response.service.response_id(), 0x7E);
		TestCase.assertEquals(response.code, 0x10);
		Assert.assertArrayEquals(response.data, new byte[] { 0x12, 0x34, 0x56, 0x78 });
	}

	@Test
	public void test_from_empty_payload() throws Exception {
		byte[] payload = {};
		Response response = Response.from_payload(payload);
		TestCase.assertFalse(response.valid);
		TestCase.assertNull(response.service);
		Assert.assertArrayEquals(response.data, new byte[] {});
	}

	@Test
	public void test_from_bad_payload() throws Exception {
		byte[] payload = { (byte) 0xFF, (byte) 0xFF };
		Response response = Response.from_payload(payload);
		TestCase.assertFalse(response.valid);
		TestCase.assertNull(response.service);
		Assert.assertArrayEquals(response.data, new byte[] {});
	}

	@Test
	public void test_from_input_param() {

		try {
			Response response = new Response(new DummyServiceNormal(), -1);
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}

		try {
			Response response = new Response(new DummyServiceNormal(), 0x100);
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}

		try {
			Response response = new Response(new DummyServiceNormal(), 0x10, new byte[] { 11 });
		} catch (Exception e) {
			logger.warn(e.getMessage());
		}
	}
}
