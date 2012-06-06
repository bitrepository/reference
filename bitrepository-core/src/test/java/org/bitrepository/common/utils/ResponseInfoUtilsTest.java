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
