/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getaudittrails;

import java.util.Arrays;

import org.bitrepository.access.getaudittrails.client.AuditTrailContributorSelector;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.utils.ResponseInfoUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GetAuditSelectorTest extends ExtendedTestCase {

    String PILLAR_ID_1 = "pillar1";

    @Test(groups={"regressiontest"})
    public void allPillarSelectorTester() throws Exception {
        addDescription("Tests the " + AuditTrailContributorSelector.class.getName());
        addStep("Setup", "");
        AuditTrailContributorSelector selector = new AuditTrailContributorSelector(Arrays.asList(PILLAR_ID_1));

        addStep("Test the basic funtionallity", "Should return lists.");
        Assert.assertEquals(selector.getOutstandingComponents(), Arrays.asList(PILLAR_ID_1));
        Assert.assertEquals(selector.getSelectedComponents(), Arrays.asList(new String[0]));
        Assert.assertFalse(selector.isFinished());

        addStep("Test against positive response.", "Should finish");
        selector = new AuditTrailContributorSelector(Arrays.asList(PILLAR_ID_1));
        IdentifyContributorsForGetAuditTrailsResponse goodResponse = new IdentifyContributorsForGetAuditTrailsResponse();
        goodResponse.setFrom(PILLAR_ID_1);
        goodResponse.setCollectionID(PILLAR_ID_1);
        goodResponse.setResponseInfo(ResponseInfoUtils.getPositiveIdentification());
        selector.processResponse(goodResponse);

        Assert.assertTrue(selector.isFinished());

        addStep("Test against a different response.", "Should throw exception.");
        IdentifyPillarsForGetFileIDsResponse fileidsResponse = new IdentifyPillarsForGetFileIDsResponse();
        fileidsResponse.setFrom(PILLAR_ID_1);
        fileidsResponse.setPillarID(PILLAR_ID_1);
        fileidsResponse.setResponseInfo(createNegativeIdentificationResponseInfo());
        try {
            selector.processResponse(fileidsResponse);
            Assert.fail("Should throw an exception here.");
        } catch (UnexpectedResponseException e) {
            // Expected.
        }
    }

    private ResponseInfo createNegativeIdentificationResponseInfo() {
        ResponseInfo resInfo = new ResponseInfo();
        resInfo.setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        resInfo.setResponseText("Failure");
        return resInfo;
    }
}
