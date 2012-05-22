package org.bitrepository.alarm;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlarmExceptionTest extends ExtendedTestCase {
    
    @Test(groups = {"regressiontest"})
    public void alarmExceptionTest() throws Exception {
        addDescription("Tests that AlarmExceptions can be thrown.");
        String alarmError = "The message of the alarm exception";
        try {
            throw new AlarmException(alarmError);
        } catch (AlarmException e) {
            Assert.assertEquals(e.getMessage(), alarmError);
            Assert.assertNull(e.getCause());
        }
        
        String otherError = "This is the message of the included exception";
        try {
            throw new AlarmException(alarmError, new Exception(otherError));
        } catch (AlarmException e) {
            Assert.assertEquals(e.getMessage(), alarmError);
            Assert.assertNotNull(e.getCause());
            Assert.assertEquals(e.getCause().getMessage(), otherError);
        }
    }

}
