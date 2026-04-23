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
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


/**
 * Test that IllegalOperationException behaves as expected.
 */

class IllegalOperationExceptionTest {
    private final String TEST_COLLECTION_ID = "test-collection-id";

    @Test
    @Tag("regressiontest")
    void testIdentifyContributor() throws Exception {
        addDescription("Test the instantiation of the exception");
        addStep("Setup", "");
        String errMsg = "TEST-ERROR";
        String FileID = "FILE-ID";
        ResponseCode errCode = ResponseCode.FAILURE;
        String causeMsg = "CAUSE-EXCEPTION";

        addStep("Try to throw such an exception", "Should be able to be caught and validated");
        try {
            throw new IllegalOperationException(errCode, errMsg, FileID);
        } catch (Exception e) {
            assertInstanceOf(IllegalOperationException.class, e);
            assertEquals(errMsg, e.getMessage());
            assertEquals(errCode, ((IllegalOperationException) e).getResponseInfo().getResponseCode());
            assertEquals(errMsg, ((IllegalOperationException) e).getResponseInfo().getResponseText());
            assertNull(e.getCause());
            assertEquals(FileID, ((IllegalOperationException) e).getFileId());
        }

        addStep("Throw the exception with an embedded exception",
                "The embedded exception should be the same.");
        try {
            throw new IllegalOperationException(errCode, errMsg, FileID, new IllegalArgumentException(causeMsg));
        } catch (Exception e) {
            assertInstanceOf(IllegalOperationException.class, e);
            assertInstanceOf(RequestHandlerException.class, e);
            assertEquals(errMsg, e.getMessage());
            assertEquals(errCode, ((IllegalOperationException) e).getResponseInfo().getResponseCode());
            assertEquals(errMsg, ((IllegalOperationException) e).getResponseInfo().getResponseText());
            assertNotNull(e.getCause());
            assertInstanceOf(IllegalArgumentException.class, e.getCause());
            assertEquals(causeMsg, e.getCause().getMessage());
            assertNotNull(e.toString());
            assertEquals(FileID, ((IllegalOperationException) e).getFileId());
        }
    }
}
