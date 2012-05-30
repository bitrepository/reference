package org.bitrepository.client.exception;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.client.exceptions.NegativeResponseException;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NegativeResponseExceptionTest extends ExtendedTestCase {
    
    @Test(groups = { "regressiontest" })
    public void testNegativeResponse() throws Exception {
        addDescription("Test the instantiation of the exception");
        addStep("Setup", "");
        String errMsg = "TEST-EXCEPTION";
        ResponseCode responseCode = ResponseCode.FAILURE;
        
        addStep("Try to throw such an exception with the response code", 
                "Should be able to be caught and validated");
        try {
            throw new NegativeResponseException(errMsg, responseCode);
        } catch(Exception e) {
            Assert.assertTrue(e instanceof NegativeResponseException);
            Assert.assertEquals(e.getMessage(), errMsg);
            Assert.assertEquals(((NegativeResponseException) e).getErrorcode(), responseCode);
            Assert.assertNull(e.getCause());
        }
    }
}
