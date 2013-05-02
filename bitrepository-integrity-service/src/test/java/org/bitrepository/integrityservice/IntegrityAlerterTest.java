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

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.integrityservice.alerter.IntegrityAlarmDispatcher;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.mocks.MockIntegrityReport;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IntegrityAlerterTest extends ExtendedTestCase {
    Settings settings;
    MessageBus messageBus;
    
    public static final String TEST_COLLECTION = "collection1";
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings("IntegrityAlerterUnderTest");
        SecurityManager securityManager = new DummySecurityManager();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
    }
    
    static int callsForError = 0;
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIntegrityFailed() {
        addDescription("Test the IntegrityFailed method for the IntegrityAlerter");
        callsForError = 0;
        
        addStep("Create the alerter, but remove the actual sending of the alarm", 
                "Should increase the counter 'callsForError'");
        IntegrityAlerter alerter = new IntegrityAlarmDispatcher(settings, messageBus, null) {
            @Override
            public void error(Alarm alarm) {
                callsForError++;
            }
        };
        
        addStep("Call the function for integrity failure.", "Should attempt to make a call for 'error'.");
        alerter.integrityFailed(new MockIntegrityReport(TEST_COLLECTION));
        Assert.assertEquals(callsForError, 1);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testOperationFailed() {
        addDescription("Test the OperationFailed method for the IntegrityAlerter");
        callsForError = 0;
        
        addStep("Create the alerter, but remove the actual sending of the alarm", 
                "Should increase the counter 'callsForError'");
        IntegrityAlerter alerter = new IntegrityAlarmDispatcher(settings, messageBus, null) {
            @Override
            public void error(Alarm alarm) {
                callsForError++;
            }
        };
        
        addStep("Call the function for integrity failure.", "Should attempt to make a call for 'error'.");
        alerter.operationFailed("Testing the ability to fail.", TEST_COLLECTION);
        Assert.assertEquals(callsForError, 1);
    }
}
