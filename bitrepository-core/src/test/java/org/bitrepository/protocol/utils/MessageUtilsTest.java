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
package org.bitrepository.protocol.utils;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MessageUtilsTest extends ExtendedTestCase {
    
    @Test(groups = { "regressiontest" })
    public void testPositiveIdentification() throws Exception {
        addDescription("Tests isPositiveIdentifyResponse method in the message utility class.");
        MessageResponse response = new MessageResponse();
        ResponseInfo ri = new ResponseInfo();
        response.setResponseInfo(ri);

        addStep("validate that it can see a positive identify response", "Should return true for positive identify.");
        response.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertTrue(MessageUtils.isPositiveIdentifyResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        Assert.assertFalse(MessageUtils.isPositiveIdentifyResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.OPERATION_COMPLETED);
        Assert.assertFalse(MessageUtils.isPositiveIdentifyResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.OPERATION_PROGRESS);
        Assert.assertFalse(MessageUtils.isPositiveIdentifyResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        Assert.assertFalse(MessageUtils.isPositiveIdentifyResponse(response));
    }

    @Test(groups = { "regressiontest" })
    public void testIdentificationResponse() throws Exception {
        addDescription("Tests isIdentifyResponse method in the message utility class.");
        MessageResponse response = new MessageResponse();
        ResponseInfo ri = new ResponseInfo();
        response.setResponseInfo(ri);
        
        addStep("validate that it can see a identify response", "Should only return true for identify responses.");
        response.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        Assert.assertTrue(MessageUtils.isIdentifyResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertTrue(MessageUtils.isIdentifyResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.FAILURE);
        Assert.assertFalse(MessageUtils.isIdentifyResponse(response));
        
    }
    
    @Test(groups = { "regressiontest" })
    public void testProgressResponse() throws Exception {
        addDescription("Tests isPositiveProgressResponse method in the message utility class.");
        MessageResponse response = new MessageResponse();
        ResponseInfo ri = new ResponseInfo();
        response.setResponseInfo(ri);
        
        addStep("validate progress response", "Should only return true for 'operation_progress', "
                + "'operation_accepted_progress' and 'identification_positive'.");
        response.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        Assert.assertFalse(MessageUtils.isPositiveProgressResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertTrue(MessageUtils.isPositiveProgressResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.OPERATION_COMPLETED);
        Assert.assertFalse(MessageUtils.isPositiveProgressResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.OPERATION_PROGRESS);
        Assert.assertTrue(MessageUtils.isPositiveProgressResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        Assert.assertTrue(MessageUtils.isPositiveProgressResponse(response));
        response.getResponseInfo().setResponseCode(ResponseCode.FAILURE);
        Assert.assertFalse(MessageUtils.isIdentifyResponse(response));
    }
    
}
