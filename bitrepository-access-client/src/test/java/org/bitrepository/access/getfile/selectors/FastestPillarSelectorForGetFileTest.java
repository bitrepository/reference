/*
 * #%L
 * Bitrepository Access
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getfile.selectors;

import java.math.BigInteger;
import java.util.Arrays;

import org.bitrepository.access.getfile.TestGetFileMessageFactory;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class FastestPillarSelectorForGetFileTest extends ExtendedTestCase {
    private static final PillarStub slowPillar = 
        new PillarStub("slowPillar", "slowPillarTopic", new BigInteger("1"), 
                TimeMeasureUnit.HOURS);
    private static final PillarStub fastPillar = 
        new PillarStub("fastPillar", "fastPillarTopic", new BigInteger("2"), 
                TimeMeasureUnit.MILLISECONDS);
    private static final PillarStub mediumPillar = 
        new PillarStub("mediumPillar", "mediumPillarTopic", new BigInteger("1000"), 
                TimeMeasureUnit.MILLISECONDS);
    private static final String[] PILLAR_IDS = 
        new String[] { slowPillar.pillarID, fastPillar.pillarID, mediumPillar.pillarID};
    private FastestPillarSelectorForGetFile selector;
    
    @BeforeMethod (alwaysRun=true)
    public void setup() {
        selector = new FastestPillarSelectorForGetFile(Arrays.asList(PILLAR_IDS));
    }

    @Test (groups = { "regressiontest" })
    public void processNiceResponsesTest() throws Exception {
        Assert.assertFalse(selector.isFinished(), "Selector thinks it is finish before processing responses");

        selector.processResponse(slowPillar.createResponse());
        Assert.assertFalse(selector.isFinished(), "Selector thinks it is finished after 1 response");

        selector.processResponse(fastPillar.createResponse());
        Assert.assertFalse(selector.isFinished(), "Selector thinks it is finished after 2 response");

        selector.processResponse(mediumPillar.createResponse());
        Assert.assertTrue(selector.isFinished(), "Selector didn't change to finished after 3 response");
        Assert.assertEquals(selector.selectedPillar.getID(), fastPillar.pillarID, "Not pillarID from fastest pillar");
        Assert.assertEquals(selector.selectedPillar.getDestination(), 
                fastPillar.pillarTopic, "Not pillarID from fastest pillar");
    }

    @Test (groups = { "regressiontest" })
    public void processBadResponsesTest() throws Exception {
        Assert.assertFalse(selector.isFinished(), "Selector thinks it is finish before processing responses");
        Assert.assertFalse(selector.hasSelectedComponent());

        selector.processResponse(slowPillar.createResponse());
        try { 
            selector.processResponse(slowPillar.createResponse());
            Assert.fail("Should have throw exception after receiving dublicated response");
        } catch (UnexpectedResponseException uee) {}
        Assert.assertFalse(selector.isFinished(), 
        "Selector thinks it is finished after initially receiving 2 equal response");

        IdentifyPillarsForGetFileResponse response2 = fastPillar.createResponse();
        response2.setPillarID("Invalid pillar");
        response2.setFrom("Invalid pillar");
        try { 
            selector.processResponse(response2);
            Assert.fail("Should have throw exception after receiving response from invalid pillar");
        } catch (UnexpectedResponseException uee) {}
        Assert.assertFalse(selector.isFinished(), 
                "Selector thinks it is finished after initially receiving response from invalid pillar");
                selector.processResponse(fastPillar.createResponse());

        selector.processResponse(mediumPillar.createResponse());
        Assert.assertTrue(selector.isFinished(), "Selector didn't change to finished after 3 response");
        Assert.assertEquals(selector.selectedPillar.getID(), fastPillar.pillarID, "Not pillarID from fastest pillar");
        Assert.assertEquals(selector.selectedPillar.getDestination(), 
                fastPillar.pillarTopic, "Not pillarID from fastest pillar");
    }

    @Test (groups = { "regressiontest" })
    public void processOtherResponsesTest() throws Exception {
        addDescription("Test the handling of a wrong response");
        IdentifyPillarsForGetFileIDsResponse fileidsResponse = new IdentifyPillarsForGetFileIDsResponse();
        fileidsResponse.setFrom(PILLAR_IDS[0]);
        fileidsResponse.setPillarID(PILLAR_IDS[0]);
        fileidsResponse.setResponseInfo(createNegativeIdentificationResponseInfo());
        try {
            selector.processResponse(fileidsResponse);
            Assert.fail("Should throw an exception here.");
        } catch (UnexpectedResponseException e) {
            // Expected.
        }
    }

    private static class PillarStub {
        private final String pillarID;
        private final String pillarTopic;
        private final BigInteger timeToDeliverValue;
        private final TimeMeasureUnit timeToDeliverUnit;

        private static final TestGetFileMessageFactory messageFactory = 
            new TestGetFileMessageFactory("FastestPillarSelectorForGetFileTest");

        public PillarStub(String pillarID, String pillarTopic,
                BigInteger timeToDeliverValue, 
                TimeMeasureUnit timeToDeliverUnit) {
            this.pillarID = pillarID;
            this.pillarTopic = pillarTopic;
            this.timeToDeliverValue = timeToDeliverValue;
            this.timeToDeliverUnit = timeToDeliverUnit;
        }

        private IdentifyPillarsForGetFileResponse createResponse() {
            IdentifyPillarsForGetFileResponse response = messageFactory.createIdentifyPillarsForGetFileResponse(
                    messageFactory.createIdentifyPillarsForGetFileRequest(), 
                    pillarID, pillarTopic);
            TimeMeasureTYPE timeToDeliver = new TimeMeasureTYPE();
            timeToDeliver.setTimeMeasureValue(timeToDeliverValue);
            timeToDeliver.setTimeMeasureUnit(timeToDeliverUnit);
            response.setTimeToDeliver(timeToDeliver);
            return response;
        }
    }
    
    private ResponseInfo createNegativeIdentificationResponseInfo() {
        ResponseInfo resInfo = new ResponseInfo();
        resInfo.setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        resInfo.setResponseText("Failure");
        return resInfo;
    }
}
