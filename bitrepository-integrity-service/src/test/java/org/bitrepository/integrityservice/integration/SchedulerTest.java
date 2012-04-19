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
package org.bitrepository.integrityservice.integration;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.TimerWorkflowScheduler;
import org.bitrepository.integrityservice.workflow.scheduler.CollectAllChecksumsWorkflow;
import org.bitrepository.integrityservice.workflow.scheduler.CollectAllFileIDsWorkflow;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.client.eventhandler.EventHandler;
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
    
    private final Long INTERVAL = 1000L;
    private final Long INTERVAL_DELAY = 50L;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
        securityManager = new DummySecurityManager();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
    }
    
    @Test(groups = {"integrationtest"})
    public void simpleSchedulerGetFileIDsTester() throws Exception {
        addDescription("Tests that the scheduler is able make calls to the collector at given intervals.");
        addStep("Setup the variables and such.", "Should not be able to fail here.");
        String taskName = "AllFileIDs-Task";
        settings.getReferenceSettings().getIntegrityServiceSettings().setSchedulerInterval(INTERVAL);
        TimerWorkflowScheduler scheduler = new TimerWorkflowScheduler(settings);
        TestCollector collector = new TestCollector();
        
        addStep("Validate that no calls for either checksums or fileIDs have been made.", 
                "Both counts should be zero");
        Assert.assertEquals(collector.getChecksumsCount(), 0, "No checksum calls at begining.");
        Assert.assertEquals(collector.getFileIDsCount(), 0, "No fileids calls at begining.");
        
        addStep("Start the trigger for the FileIDs", "Should start sending requests to the InformationCollector");
        scheduler.putWorkflow(new CollectAllFileIDsWorkflow(INTERVAL, taskName, settings, collector, null));
        
        addStep("Wait 4 * the interval (plus 50 millis for instantiation), stop the trigger and validate the results.", 
                "Should be exactly three calls for FileIDs and none for Checksums");
        synchronized(this) {
            wait(4*INTERVAL + INTERVAL_DELAY);
        }
        scheduler.removeWorkflow(taskName);
        int getFileIDsCalls = collector.getFileIDsCount();
        Assert.assertEquals(getFileIDsCalls, 3, "The expected amount of calls for the workflow.");
        Assert.assertEquals(collector.getChecksumsCount(), 0, "Still no calls for GetChecksums");
        
        addStep("Wait another 2 seconds and validate that the trigger has been cancled.", 
                "The Collector should have received no more requests.");
        synchronized(this) {
            wait(2*INTERVAL);
        }
        scheduler.removeWorkflow(taskName);
        Assert.assertEquals(collector.getChecksumsCount(), 0, "Still no calls for GetChecksums");
        Assert.assertEquals(collector.getFileIDsCount(), getFileIDsCalls, 
                "The number of calls for GetFileIDs should not vary.");
    }
    
    @Test(groups = {"integrationtest"})
    public void simpleSchedulerGetChecksumsTester() throws Exception {
        addDescription("Tests that the scheduler is able make calls to the collector at given intervals.");
        addStep("Setup the variables and such.", "Should not be able to fail here.");
        String taskName = "AllChecksums-Task";
        settings.getReferenceSettings().getIntegrityServiceSettings().setSchedulerInterval(INTERVAL);
        TimerWorkflowScheduler scheduler = new TimerWorkflowScheduler(settings);
        TestCollector collector = new TestCollector();
        
        addStep("Validate that no calls for either checksums or fileIDs have been made.", 
                "Both counts should be zero");
        Assert.assertEquals(collector.getChecksumsCount(), 0, "No checksum calls at begining.");
        Assert.assertEquals(collector.getFileIDsCount(), 0, "No fileids calls at begining.");

        addStep("Start the trigger for the FileIDs", "Should start sending requests to the InformationCollector");
        scheduler.putWorkflow(new CollectAllChecksumsWorkflow(INTERVAL, taskName, new ChecksumSpecTYPE(), settings, 
                collector, null));
        
        addStep("Wait 4 * the interval (plus 50 millis for instantiation), stop the trigger and validate the results.", 
                "Should be exactly three calls for getChecksums and none for FileIDs");
        synchronized(this) {
            wait(4*INTERVAL + INTERVAL_DELAY);
        }
        scheduler.removeWorkflow(taskName);
        int getChecksumsCalls = collector.getChecksumsCount();
        Assert.assertEquals(collector.getFileIDsCount(), 0, "Still no calls for GetFileIDs");
        Assert.assertEquals(getChecksumsCalls, 3, "The expected amount of calls for the workflow.");
        
        addStep("Wait another 2*interval and validate that the trigger has been cancled.", 
                "The Collector should have received no more requests.");
        synchronized(this) {
            wait(2*INTERVAL);
        }
        scheduler.removeWorkflow(taskName);
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
