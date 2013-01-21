/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice;

import java.util.Arrays;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.bitrepository.integrityservice.mocks.MockChecker;
import org.bitrepository.integrityservice.mocks.MockCollector;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.bitrepository.integrityservice.mocks.MockScheduler;
import org.bitrepository.integrityservice.mocks.MockWorkflow;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.workflow.Workflow;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IntegrityServiceTest extends ExtendedTestCase {
    protected Settings settings;
    MessageBus messageBus;
    SecurityManager securityManager;

    public static final String TEST_PILLAR_1 = "test-pillar-1";
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        securityManager = new DummySecurityManager();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void integrityServiceTest() throws Exception {
        addDescription("Testing the basic functionality of the integrity service.");
        MockIntegrityModel model = new MockIntegrityModel(new TestIntegrityModel(Arrays.asList(TEST_PILLAR_1)));
        MockScheduler scheduler = new MockScheduler();
        MockChecker checker = new MockChecker();
        MockIntegrityAlerter alerter = new MockIntegrityAlerter();
        MockCollector collector = new MockCollector();
        MockAuditManager auditManager = new MockAuditManager(); 
        SimpleIntegrityService integrityService = new SimpleIntegrityService(model, scheduler, checker, alerter, 
                collector, auditManager, settings, messageBus);
        
        addStep("Test the initial state", "");
        Assert.assertEquals(integrityService.getAllWorkflows().size(), 1, "Should initially have one workflow");
        Assert.assertEquals(integrityService.getNumberOfChecksumErrors(TEST_PILLAR_1), 0);
        Assert.assertEquals(integrityService.getNumberOfFiles(TEST_PILLAR_1), 0);
        Assert.assertEquals(integrityService.getNumberOfMissingFiles(TEST_PILLAR_1), 0);
        Assert.assertEquals(integrityService.getScheduledWorkflows().size(), 0);
        
        addStep("Try to schedule a new workflow.", "Should added both to the scheduled and the list of workflows.");
        Workflow workflow = new MockWorkflow();
        integrityService.scheduleWorkflow(workflow, 0);
        Assert.assertEquals(integrityService.getAllWorkflows().size(), 2);
        Assert.assertEquals(integrityService.getScheduledWorkflows().size(), 1);
        
        addStep("Try to add it again.", "Should not change anything.");
        integrityService.scheduleWorkflow(workflow, 0);
        Assert.assertEquals(integrityService.getAllWorkflows().size(), 2);
        Assert.assertEquals(integrityService.getScheduledWorkflows().size(), 1);
        
        addStep("Test 'start'.", "Should not do anything.");
        integrityService.start();
        Assert.assertEquals(integrityService.getAllWorkflows().size(), 2);
        Assert.assertEquals(integrityService.getNumberOfChecksumErrors(TEST_PILLAR_1), 0);
        Assert.assertEquals(integrityService.getNumberOfFiles(TEST_PILLAR_1), 0);
        Assert.assertEquals(integrityService.getNumberOfMissingFiles(TEST_PILLAR_1), 0);
        Assert.assertEquals(integrityService.getScheduledWorkflows().size(), 1);
    }
}
