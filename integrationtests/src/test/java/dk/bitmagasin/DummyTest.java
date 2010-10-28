package dk.bitmagasin;

import org.testng.Assert;
import org.testng.annotations.Test;
import static org.testng.AssertJUnit.*;

public class DummyTest {
	
	@Test(groups = { "functest", "checkintest" })
	public void trivialTest() {
		Assert.assertEquals(5,5);
		assertEquals("5 not equal to 5", 5, 5);
	}
}
