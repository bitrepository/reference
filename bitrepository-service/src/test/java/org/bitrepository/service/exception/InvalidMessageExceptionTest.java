/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.service.exception;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test that InvalidMessageException works as expected.
 */
public class InvalidMessageExceptionTest extends ExtendedTestCase {
    private final String TEST_COLLECTION_ID = "test-collection-id";
    
    @Test(groups = { "regressiontest" })
    public void testIdentifyContributor() throws Exception {
        addDescription("Test the instantiation of the exception");
        addStep("Setup", "");
        String errMsg = "TEST-ERROR";
        ResponseCode errCode = ResponseCode.FAILURE;
        String causeMsg = "CAUSE-EXCEPTION";
        
        addStep("Try to throw such an exception", "Should be able to be caught and validated");
        try {
            throw new InvalidMessageException(errCode, errMsg);
        } catch(Exception e) {
            Assert.assertTrue(e instanceof InvalidMessageException);
            Assert.assertEquals(e.getMessage(), errMsg);
            Assert.assertEquals(((InvalidMessageException) e).getResponseInfo().getResponseCode(), errCode);
            Assert.assertEquals(((InvalidMessageException) e).getResponseInfo().getResponseText(), errMsg);
            Assert.assertNull(e.getCause());
        }
        
        addStep("Throw the exception with an embedded exception", "The embedded exception should be the same.");
        try {
            throw new InvalidMessageException(errCode, errMsg, new IllegalArgumentException(causeMsg));
        } catch(Exception e) {
            Assert.assertTrue(e instanceof InvalidMessageException);
            Assert.assertTrue(e instanceof RequestHandlerException);
            Assert.assertEquals(e.getMessage(), errMsg);
            Assert.assertEquals(((InvalidMessageException) e).getResponseInfo().getResponseCode(), errCode);
            Assert.assertEquals(((InvalidMessageException) e).getResponseInfo().getResponseText(), errMsg);
            Assert.assertNotNull(e.getCause());
            Assert.assertTrue(e.getCause() instanceof IllegalArgumentException);
            Assert.assertEquals(e.getCause().getMessage(), causeMsg);
            Assert.assertNotNull(((RequestHandlerException) e).toString());
        }
        
        addStep("Throw the exception with a ResponseInfo", "Should be caught and validated");
        try {
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(errCode);
            ri.setResponseText(errMsg);
            throw new InvalidMessageException(ri);
        } catch(Exception e) {
            Assert.assertTrue(e instanceof InvalidMessageException);
            Assert.assertEquals(e.getMessage(), errMsg);
            Assert.assertEquals(((InvalidMessageException) e).getResponseInfo().getResponseCode(), errCode);
            Assert.assertEquals(((InvalidMessageException) e).getResponseInfo().getResponseText(), errMsg);
            Assert.assertNull(e.getCause());
        }
    }
}
