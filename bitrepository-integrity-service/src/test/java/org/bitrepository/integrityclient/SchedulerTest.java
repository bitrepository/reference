/*
 * #%L
 * Bitrepository Integrity Client
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
package org.bitrepository.integrityclient;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.integrityclient.collector.IntegrityInformationCollector;
import org.bitrepository.integrityclient.workflow.TimerWorkflowScheduler;
import org.bitrepository.integrityclient.workflow.scheduler.CollectAllChecksumsWorkflow;
import org.bitrepository.integrityclient.workflow.scheduler.CollectAllFileIDsWorkflow;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class SchedulerTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    SecurityManager securityManager;
    MessageBus messageBus;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
        securityManager = new DummySecurityManager();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
        
        
    }
    
    @Test(groups = {"regressiontest"})
    public void simpleSchedulerGetFileIDsTester() throws Exception {
        addDescription("Tests that the scheduler is able make calls to the collector at given intervals.");
        addStep("Setup the variables and such.", "Should not be able to fail here.");
        long interval = 1000;
        String taskName = "AllFileIDs-Task";
        settings.getReferenceSettings().getIntegrityServiceSettings().setSchedulerInterval(interval);
        TimerWorkflowScheduler scheduler = new TimerWorkflowScheduler(settings);
        TestCollector collector = new TestCollector();
        
        addStep("Validate that no calls for either checksums or fileIDs have been made.", 
                "Both counts should be zero");
        Assert.assertEquals(collector.getChecksumsCount(), 0, "No checksum calls at begining.");
        Assert.assertEquals(collector.getFileIDsCount(), 0, "No fileids calls at begining.");
        
        addStep("Start the trigger for the FileIDs", "Should start sending requests to the InformationCollector");
        scheduler.putTrigger(taskName, new CollectAllFileIDsWorkflow(interval, settings, collector, null));
        
        addStep("Wait 4 * the interval, stop the trigger and validate the results.", 
                "Should be three or four counts for FileIDs and none for Checksums");
        synchronized(this) {
            wait(4*interval);
        }
        scheduler.removeTrigger(taskName);
        int getFileIDsCalls = collector.getFileIDsCount();
        Assert.assertTrue(getFileIDsCalls >= 3, "At least 3 calls for GetFileIDs");
        Assert.assertTrue(getFileIDsCalls <= 4, "At most 4 calls for GetFileIDs");
        Assert.assertEquals(collector.getChecksumsCount(), 0, "Still no calls for GetChecksums");
        
        addStep("Wait another 2 seconds and validate that the trigger has been cancled.", 
                "The Collector should have received no more requests.");
        synchronized(this) {
            wait(2*interval);
        }
        scheduler.removeTrigger(taskName);
        Assert.assertEquals(collector.getChecksumsCount(), 0, "Still no calls for GetChecksums");
        Assert.assertEquals(collector.getFileIDsCount(), getFileIDsCalls, 
                "The number of calls for GetFileIDs should not vary.");
    }
    
    @Test(groups = {"regressiontest"})
    public void simpleSchedulerGetChecksumsTester() throws Exception {
        addDescription("Tests that the scheduler is able make calls to the collector at given intervals.");
        addStep("Setup the variables and such.", "Should not be able to fail here.");
        long interval = 1000;
        String taskName = "AllChecksums-Task";
        settings.getReferenceSettings().getIntegrityServiceSettings().setSchedulerInterval(interval);
        TimerWorkflowScheduler scheduler = new TimerWorkflowScheduler(settings);
        TestCollector collector = new TestCollector();
        
        addStep("Validate that no calls for either checksums or fileIDs have been made.", 
                "Both counts should be zero");
        Assert.assertEquals(collector.getChecksumsCount(), 0, "No checksum calls at begining.");
        Assert.assertEquals(collector.getFileIDsCount(), 0, "No fileids calls at begining.");

        addStep("Start the trigger for the FileIDs", "Should start sending requests to the InformationCollector");
        scheduler.putTrigger(taskName, new CollectAllChecksumsWorkflow(interval, new ChecksumSpecTYPE(), settings, 
                collector, null));
        
        addStep("Wait 4 * the interval, stop the trigger and validate the results.", 
                "Should be three or four counts for Checksums and none for FileIDs");
        synchronized(this) {
            wait(interval*4);
        }
        scheduler.removeTrigger(taskName);
        int getChecksumsCalls = collector.getChecksumsCount();
        Assert.assertEquals(collector.getFileIDsCount(), 0, "Still no calls for GetFileIDs");
        Assert.assertTrue(getChecksumsCalls >= 3, "At least 3 calls for GetChecksums, but was: " + getChecksumsCalls);
        Assert.assertTrue(getChecksumsCalls <= 4, "At most 4 calls for GetChecksums, but was: " + getChecksumsCalls);
        
        addStep("Wait another 2*interval and validate that the trigger has been cancled.", 
                "The Collector should have received no more requests.");
        synchronized(this) {
            wait(2*interval);
        }
        scheduler.removeTrigger(taskName);
        Assert.assertEquals(collector.getFileIDsCount(), 0, "Still no calls for GetFileIDs");
        Assert.assertEquals(collector.getChecksumsCount(), getChecksumsCalls, 
                "The number of calls for GetFileIDs should not vary.");
    }
    
    /**
     * Container for keeping track of the number of calls of two different methods.
     */
    private class TestCollector implements IntegrityInformationCollector {
        /** The number of calls for the GetFileIDs method. */
        private int getFileIDs;
        /** The number of calls for the GetChecksums method. */
        private int getChecksums;
        
        public TestCollector() {
            getFileIDs = 0;
            getChecksums = 0;
        }
        
        @Override
        public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, String auditTrailInformation, 
                EventHandler eventHandler) {
            getFileIDs++;
        }
        
        /**
         * @return The count for the calls for the GetFileIDs method.
         */
        public int getFileIDsCount() {
            return getFileIDs;
        }

        @Override
        public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumType,
                String auditTrailInformation, EventHandler eventHandler) {
            getChecksums++;
        }
        
        /**
         * @return The count for the calls for the GetChecksums method.
         */
        public int getChecksumsCount() {
            return getChecksums;
        }
    }
}
