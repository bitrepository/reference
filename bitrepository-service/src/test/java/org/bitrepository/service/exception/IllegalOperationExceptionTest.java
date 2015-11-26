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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class IllegalOperationExceptionTest extends ExtendedTestCase {
    private final String TEST_COLLECTION_ID = "test-collection-id";
    
    @Test(groups = { "regressiontest" })
    public void testIdentifyContributor() throws Exception {
        addDescription("Test the instantiation of the exception");
        addStep("Setup", "");
        String errMsg = "TEST-ERROR";
        String FileID = "FILE-ID";
        ResponseCode errCode = ResponseCode.FAILURE;
        String causeMsg = "CAUSE-EXCEPTION";
        
        addStep("Try to throw such an exception", "Should be able to be caught and validated");
        try {
            throw new IllegalOperationException(errCode, errMsg, TEST_COLLECTION_ID, FileID);
        } catch(Exception e) {
            assertTrue(e instanceof IllegalOperationException);
            assertEquals(e.getMessage(), errMsg);
            assertEquals(((IllegalOperationException) e).getResponseInfo().getResponseCode(), errCode);
            assertEquals(((IllegalOperationException) e).getResponseInfo().getResponseText(), errMsg);
            assertEquals(((IllegalOperationException) e).getCollectionID(), TEST_COLLECTION_ID);
            assertNull(e.getCause());
            assertEquals(((IllegalOperationException) e).getFileId(), FileID);
        }
        
        addStep("Throw the exception with an embedded exception", "The embedded exception should be the same.");
        try {
            throw new IllegalOperationException(errCode, errMsg, TEST_COLLECTION_ID, FileID, new IllegalArgumentException(causeMsg));
        } catch(Exception e) {
            assertTrue(e instanceof IllegalOperationException);
            assertTrue(e instanceof RequestHandlerException);
            assertEquals(e.getMessage(), errMsg);
            assertEquals(((IllegalOperationException) e).getResponseInfo().getResponseCode(), errCode);
            assertEquals(((IllegalOperationException) e).getResponseInfo().getResponseText(), errMsg);
            assertEquals(((IllegalOperationException) e).getCollectionID(), TEST_COLLECTION_ID);
            assertNotNull(e.getCause());
            assertTrue(e.getCause() instanceof IllegalArgumentException);
            assertEquals(e.getCause().getMessage(), causeMsg);
            assertNotNull(((RequestHandlerException) e).toString());
            assertEquals(((IllegalOperationException) e).getFileId(), FileID);
        }
    }
}
