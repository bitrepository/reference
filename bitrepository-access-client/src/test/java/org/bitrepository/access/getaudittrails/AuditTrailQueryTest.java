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
