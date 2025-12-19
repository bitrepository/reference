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
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


/**
 * jaccept steps to validate that the exception thrown is the exception thrown.
 */
public class IdentifyContributorExceptionTest extends ExtendedTestCase {
    private final String TEST_COLLECTION_ID = "test-collection-id";

    @Test
    @Tag( "regressiontest")
    public void testIdentifyContributor() throws Exception {
        addDescription("Test the instantiation of the exception");
        addStep("Setup", "");
        String errMsg = "TEST-ERROR";
        ResponseCode errCode = ResponseCode.FAILURE;
        String causeMsg = "CAUSE-EXCEPTION";
        
        addStep("Try to throw such an exception", "Should be able to be caught and validated");
        try {
            throw new IdentifyContributorException(errCode, errMsg);
        } catch(Exception e) {
            Assertions.assertTrue(e instanceof IdentifyContributorException);
            Assertions.assertEquals(e.getMessage(), errMsg);
            Assertions.assertEquals(((IdentifyContributorException) e).getResponseInfo().getResponseCode(), errCode);
            Assertions.assertEquals(((IdentifyContributorException) e).getResponseInfo().getResponseText(), errMsg);
            Assertions.assertNull(e.getCause());
        }
        
        addStep("Throw the exception with an embedded exception", "The embedded exception should be the same.");
        try {
            throw new IdentifyContributorException(errCode, errMsg, new IllegalArgumentException(causeMsg));
        } catch(Exception e) {
            Assertions.assertTrue(e instanceof IdentifyContributorException);
            Assertions.assertEquals(e.getMessage(), errMsg);
            Assertions.assertEquals(((IdentifyContributorException) e).getResponseInfo().getResponseCode(), errCode);
            Assertions.assertEquals(((IdentifyContributorException) e).getResponseInfo().getResponseText(), errMsg);
            Assertions.assertNotNull(e.getCause());
            Assertions.assertTrue(e.getCause() instanceof IllegalArgumentException);
            Assertions.assertEquals(e.getCause().getMessage(), causeMsg);
        }
    }    
}
