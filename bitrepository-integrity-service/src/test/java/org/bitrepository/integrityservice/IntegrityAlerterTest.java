package org.bitrepository.integrityservice;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.integrityservice.alerter.IntegrityAlarmDispatcher;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.checking.IntegrityReport;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.contributor.ContributorContext;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class IntegrityAlerterTest extends ExtendedTestCase {
    Settings settings;
    MessageBus messageBus;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
        SecurityManager securityManager = new DummySecurityManager();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
    }
    
    static int callsForError = 0;
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testIntegrityAlerter() {
        addDescription("Test the IntegrityAlerter");
        addStep("Setup", "Should not fail.");
        ContributorContext context = new ContributorContext(messageBus, settings);
        callsForError = 0;
        
        addStep("Create the alerter, but remove the actual sending of the alarm", 
                "Should increase the counter 'callsForError'");
        IntegrityAlerter alerter = new IntegrityAlarmDispatcher(context) {
            @Override
            public void error(Alarm alarm) {
                callsForError++;
            }
        };
        
        addStep("Call the function for integrity failure.", "Should attempt to make a call for 'error'.");
        alerter.integrityFailed(new IntegrityReport());
        Assert.assertEquals(callsForError, 1);
    }
}
