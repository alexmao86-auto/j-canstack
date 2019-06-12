package udsoncan.base;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import junit.framework.TestCase;

public class DefinitionsTest extends TestCase {
	Logger logger = LoggerFactory.getLogger("DefinitionsTest");

	public DefinitionsTest() {
		// TODO Auto-generated constructor stub
	}

	public DefinitionsTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Test
	public void test_data_identifier_name_from_id() throws Exception {
		for (int i = 0; i < 0x10000; i++) {
			String name = DataIdentifier.name_from_id(i);
			TestCase.assertTrue(name instanceof String);
		}
	}
	
	@Test
	public void test_routine_name_from_id() throws Exception {
		for (int i = 0; i < 0x10000; i++) {
			String name = Routine.name_from_id(i);
			TestCase.assertTrue(name instanceof String);
		}
	}

}
