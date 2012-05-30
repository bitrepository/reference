package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ResponseInfoUtilsTest extends ExtendedTestCase {
    @Test(groups = {"regressiontest"})
    public void responseInfoTester() throws Exception {
        addDescription("Test the response info.");
        addStep("Validate the positive identification response", "Should be 'IDENTIFICATION_POSITIVE'");
        ResponseInfo ri = ResponseInfoUtils.getPositiveIdentification();
        Assert.assertEquals(ri.getResponseCode(), ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Validate the Progress response", "Should be 'OPERATION_ACCEPTED_PROGRESS'");
        ri = ResponseInfoUtils.getInitialProgressResponse();
        Assert.assertEquals(ri.getResponseCode(), ResponseCode.OPERATION_ACCEPTED_PROGRESS);
    }
    
}
