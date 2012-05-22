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
        response.setResponseInfo(createNegativeIdentificationResponseInfo());
        
        try {
            selector.processResponse(response);
            Assert.fail("Should throw an " + UnexpectedResponseException.class.getName());
        } catch (UnexpectedResponseException e) {
            // expected
        }
        
        
        Assert.assertEquals(selector.getOutstandingPillars(), Arrays.asList(PILLAR_ID_1));
        Assert.assertFalse(selector.isFinished());
        
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
