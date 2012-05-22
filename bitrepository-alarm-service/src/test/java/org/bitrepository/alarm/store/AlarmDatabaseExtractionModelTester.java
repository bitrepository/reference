package org.bitrepository.alarm.store;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AlarmDatabaseExtractionModelTester extends ExtendedTestCase {
    @Test(groups = {"regressiontest"})
    public void alarmExceptionTest() throws Exception {
        addDescription("Test the AlarmDatabaseExtractionModel class");
        addStep("Define constants etc.", "Should be OK");
        boolean ascending = true;
        
        addStep("Create an empty model", "Should be populated with nulls.");
        AlarmDatabaseExtractionModel model = new AlarmDatabaseExtractionModel(null, null, null, null, null, null, ascending);
        
        Assert.assertNull(model.getAlarmCode());
        Assert.assertNull(model.getComponentId());
        Assert.assertNull(model.getEndDate());
        Assert.assertNull(model.getFileID());
        Assert.assertNull(model.getStartDate());
        Assert.assertEquals(model.getAscending(), ascending);
        Assert.assertEquals(model.getMaxCount().intValue(), Integer.MAX_VALUE);
        
        addStep("Test the AlarmCode", "Should be able to put a new one in and extract it again.");
        AlarmCode defaultAlarmCode = AlarmCode.COMPONENT_FAILURE;
        model.setAlarmCode(defaultAlarmCode);
        Assert.assertEquals(model.getAlarmCode(), defaultAlarmCode);
        
        addStep("Test the ascending", "Should be able to put a new one in and extract it again.");
        boolean defaultAscending = false;
        model.setAscending(defaultAscending);
        Assert.assertEquals(model.getAscending(), defaultAscending);

        addStep("Test the ComponentID", "Should be able to put a new one in and extract it again.");
        String defaultComponentID = "DefaultComponentID";
        model.setComponentId(defaultComponentID);
        Assert.assertEquals(model.getComponentId(), defaultComponentID);

        addStep("Test the EndDate", "Should be able to put a new one in and extract it again.");
        Date defaultEndDate = new Date(987654321);
        model.setEndDate(defaultEndDate);
        Assert.assertEquals(model.getEndDate(), defaultEndDate);

        addStep("Test the FileID", "Should be able to put a new one in and extract it again.");
        String defaultFileID = "DefaultFileID";
        model.setFileID(defaultFileID);
        Assert.assertEquals(model.getFileID(), defaultFileID);

        addStep("Test the MaxCount", "Should be able to put a new one in and extract it again.");
        Integer defaultMaxCount = new Integer(192837456);
        model.setMaxCount(defaultMaxCount);
        Assert.assertEquals(model.getMaxCount(), defaultMaxCount);
        
        addStep("Test the StartDate", "Should be able to put a new one in and extract it again.");
        Date defaultStartDate = new Date(123456789);
        model.setStartDate(defaultStartDate);
        Assert.assertEquals(model.getStartDate(), defaultStartDate);
    }
}
