package isotp.protocol;

import org.junit.Test;

import isotp.protocol.TransportLayer.Timer;
import junit.framework.TestCase;

public class TPTimerTest extends TestCase {

	public TPTimerTest() {
		// TODO Auto-generated constructor stub
	}

	public TPTimerTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Test
	public void test_timer() throws Exception {
		long timeout = 200l;
		TransportLayer tp = new TransportLayer();
		Timer t = tp.new Timer(timeout);

		TestCase.assertFalse(t.is_timed_out());
		TestCase.assertEquals(t.elapsed(), 0);
		t.start();
		TestCase.assertFalse(t.is_timed_out());
		Thread.sleep(timeout + 10);
		TestCase.assertTrue(t.is_timed_out());
		TestCase.assertTrue(t.elapsed() > timeout);
		t.stop();
		TestCase.assertFalse(t.is_timed_out());
		TestCase.assertEquals(t.elapsed(), 0);
		t.start();
		TestCase.assertFalse(t.is_timed_out());
	}

}
