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

import junit.framework.Assert;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.Test;

public class AuditTrailQueryTest extends ExtendedTestCase {

    String componentId = "componentId";
    
    @Test(groups = {"regressiontest"})
    public void testNoSequenceNumbers() throws Exception {
        addDescription("Test that a AuditTrailQuery can be created without any sequence numbers.");
        AuditTrailQuery query = new AuditTrailQuery(componentId);
        Assert.assertEquals(query.getComponentID(), componentId);
        Assert.assertNull(query.getMaxSequenceNumber());
        Assert.assertNull(query.getMinSequenceNumber());
    }

    @Test(groups = {"regressiontest"})
    public void testOnlyMinSequenceNumber() throws Exception {
        addDescription("Test the creation of a AuditTrailQuery with only the minSequenceNumber");
        Integer minSeq = 1;
        AuditTrailQuery query = new AuditTrailQuery(componentId, minSeq);
        Assert.assertEquals(query.getComponentID(), componentId);
        Assert.assertEquals(query.getMinSequenceNumber(), minSeq);
        Assert.assertNull(query.getMaxSequenceNumber());
    }

    @Test(groups = {"regressiontest"})
    public void testBothSequenceNumberSuccess() throws Exception {
        addDescription("Test the creation of a AuditTrailQuery with both SequenceNumber, where max is larger than min.");
        Integer minSeq = 1;
        Integer maxSeq = 2;
        AuditTrailQuery query = new AuditTrailQuery(componentId, minSeq, maxSeq);
        Assert.assertEquals(query.getComponentID(), componentId);
        Assert.assertEquals(query.getMinSequenceNumber(), minSeq);
        Assert.assertEquals(query.getMaxSequenceNumber(), maxSeq);
    }
    
    @Test(groups = {"regressiontest"})
    public void testBothSequenceNumberFailure() throws Exception {
        addDescription("Test the creation of a AuditTrailQuery with both SequenceNumber, where max is smalle than min.");
        Integer minSeq = 2;
        Integer maxSeq = 1;
        try {
            new AuditTrailQuery(componentId, minSeq, maxSeq);
            Assert.fail("Should throw an exception here.");
        } catch(IllegalArgumentException e) {
            // expected.
        }
    }
}
