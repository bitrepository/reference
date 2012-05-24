package org.bitrepository.common.utils;

import org.bitrepository.common.utils.TimeUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimeUtilsTester extends ExtendedTestCase {
    @Test(groups = {"regressiontest"})
    public void timeTester() throws Exception {
        addDescription("Tests the TimeUtils. Pi days = 271433605 milliseconds");
        addStep("Test that milliseconds can be converted into human readable seconds", 
                "Pi days % minutes");
        long millis = 271433605;
        String millisInSec = TimeUtils.millisecondsToSeconds(millis % 60000);
        String expectedSec = "53seconds";
        Assert.assertTrue(millisInSec.startsWith(expectedSec));
        
        addStep("Test that milliseconds can be converted into human readable minutes.", 
                "Pi days % hours");
        String millisInMin = TimeUtils.millisecondsToMinutes(millis % 3600000);
        String expectedMin = "23minutes";
        Assert.assertTrue(millisInMin.startsWith(expectedMin));
        
        addStep("Test that milliseconds can be converted into human readable hours.", 
                "Pi days % days");
        String millisInHour = TimeUtils.millisecondsToHours(millis % (3600000*24));
        String expectedHours = "3hours";
        Assert.assertTrue(millisInHour.startsWith(expectedHours));
        
        addStep("Test that milliseconds can be converted into human readable minutes.", 
                "Pi days");
        String millisInDay = TimeUtils.millisecondsToDays(millis);
        String expectedDays = "3days";
        Assert.assertTrue(millisInDay.startsWith(expectedDays));
        
        addStep("Test the human readable output.", "");
        String expectedMillis = (millis % 1000L) + "ms"; 
        String human = TimeUtils.millisecondsToHuman(millis);
        Assert.assertTrue(human.contains(expectedMillis), human);
        Assert.assertTrue(human.contains(expectedSec), human);
        Assert.assertTrue(human.contains(expectedMin), human);
        Assert.assertTrue(human.contains(expectedHours), human);
        Assert.assertTrue(human.contains(expectedDays), human);
    }
}
