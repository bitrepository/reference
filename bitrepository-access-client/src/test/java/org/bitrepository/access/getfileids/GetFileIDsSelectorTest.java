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
package org.bitrepository.access.getfileids;

import java.util.Arrays;

import org.bitrepository.access.getfileids.selector.PillarSelectorForGetFileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.utils.ResponseInfoUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GetFileIDsSelectorTest extends ExtendedTestCase {
    
    String PILLAR_ID_1 = "pillar1";
    
    @Test(groups={"regressiontest"})
    public void allPillarSelectorTester() throws Exception {
        addDescription("Tests the " + PillarSelectorForGetFileIDs.class.getName());
        addStep("Setup", "");
        PillarSelectorForGetFileIDs selector = new PillarSelectorForGetFileIDs(Arrays.asList(PILLAR_ID_1));
        
        addStep("Test the basic funtionallity", "Should return lists.");
        Assert.assertEquals(selector.getOutstandingPillars(), Arrays.asList(PILLAR_ID_1));
        Assert.assertEquals(selector.getSelectedPillars(), Arrays.asList(new String[0]));
        Assert.assertFalse(selector.isFinished());
        
        addStep("Test against a negative response.", "Should throw an exception.");
        IdentifyPillarsForGetFileIDsResponse response = new IdentifyPillarsForGetFileIDsResponse();
        response.setFrom(PILLAR_ID_1);
        response.setPillarID(PILLAR_ID_1);
        response.setResponseInfo(createNegativeIdentificationResponseInfo());
        
        try {
            selector.processResponse(response);
            Assert.fail("Should throw an " + UnexpectedResponseException.class.getName());
        } catch (UnexpectedResponseException e) {
            // expected
        }
        
        addStep("Good case. New selector and a positive response.", "Should finish");
        selector = new PillarSelectorForGetFileIDs(Arrays.asList(PILLAR_ID_1));
        IdentifyPillarsForGetFileIDsResponse goodResponse = new IdentifyPillarsForGetFileIDsResponse();
        goodResponse.setFrom(PILLAR_ID_1);
        goodResponse.setPillarID(PILLAR_ID_1);
        goodResponse.setResponseInfo(ResponseInfoUtils.getPositiveIdentification());
        selector.processResponse(goodResponse);
        
        Assert.assertTrue(selector.isFinished());
    }

    private ResponseInfo createNegativeIdentificationResponseInfo() {
        ResponseInfo resInfo = new ResponseInfo();
        resInfo.setResponseCode(ResponseCode.IDENTIFICATION_NEGATIVE);
        resInfo.setResponseText("Failure");
        return resInfo;
    }
}
