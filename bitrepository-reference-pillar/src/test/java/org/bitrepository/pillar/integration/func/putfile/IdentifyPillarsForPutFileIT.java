/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.integration.func.putfile;

import junit.framework.Assert;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IdentifyPillarsForPutFileIT extends PillarFunctionTest {
    protected PutFileMessageFactory msgFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new PutFileMessageFactory(componentSettings, componentSettings.getComponentID(), null);
    }

    @Test( groups = {"pillar-integration-test"})
    public void normalIdentification() {
        addDescription("Verifies the normal behaviour for putFile identification");
        addStep("Sending a putFile identification.",
            "The pillar under test should make a response with the correct elements.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                TestFileHelper.DEFAULT_FILE_ID, 0L);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getCollectionID(), identifyRequest.getCollectionID());
        Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        Assert.assertNull(receivedIdentifyResponse.getChecksumDataForExistingFile());
        Assert.assertNull(receivedIdentifyResponse.getPillarChecksumSpec());
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertEquals(receivedIdentifyResponse.getTo(), identifyRequest.getReplyTo());
    }
}
