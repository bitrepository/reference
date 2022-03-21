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
package org.bitrepository.client.exception;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.client.exceptions.NegativeResponseException;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class NegativeResponseExceptionTest extends ExtendedTestCase {

    @Test(groups = {"regressiontest"})
    public void testNegativeResponse() {
        addDescription("Test the instantiation of the exception");
        addStep("Setup", "");
        String errMsg = "TEST-EXCEPTION";
        ResponseCode responseCode = ResponseCode.FAILURE;

        addStep("Try to throw such an exception with the response code",
                "Should be able to be caught and validated");
        try {
            throw new NegativeResponseException(errMsg, responseCode);
        } catch (Exception e) {
            Assert.assertTrue(e instanceof NegativeResponseException);
            Assert.assertEquals(e.getMessage(), errMsg);
            Assert.assertEquals(((NegativeResponseException) e).getErrorCode(), responseCode);
            Assert.assertNull(e.getCause());
        }
    }
}
