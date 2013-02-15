/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.referencepillar;

import java.io.File;

import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.common.PillarAlarmDispatcher;
import org.bitrepository.pillar.referencepillar.archive.CollectionArchiveManager;
import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
import org.bitrepository.service.audit.MockAuditManager;
import org.bitrepository.service.contributor.ResponseDispatcher;
import org.bitrepository.settings.referencesettings.AlarmLevel;
import org.testng.annotations.AfterMethod;

public class ReferencePillarMediatorTester extends ReferencePillarTest {
    
    CollectionArchiveManager archives;
    ReferencePillarMediator mediator;
    MockAuditManager audits;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        File dir = new File(settingsForCUT.getReferenceSettings().getPillarSettings().getCollectionDirs().get(0).getFileDir().get(0));
        settingsForCUT.getReferenceSettings().getPillarSettings().setAlarmLevel(AlarmLevel.WARNING);
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        addStep("Initialize the pillar.", "Should not be a problem.");
        archives = new CollectionArchiveManager(settingsForCUT);
        audits = new MockAuditManager();
        MessageHandlerContext context = new MessageHandlerContext(
            settingsForCUT,
            new ResponseDispatcher(settingsForCUT, messageBus),
            new PillarAlarmDispatcher(settingsForCUT, messageBus),
            audits);
        mediator = new ReferencePillarMediator(messageBus, context, archives, csManager);
        mediator.start();
    }
    
    @AfterMethod (alwaysRun=true) 
    public void closeArchive() {
        File dir = new File(settingsForCUT.getReferenceSettings().getPillarSettings().getCollectionDirs().get(0).getFileDir().get(0));
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        if(mediator != null) {
            mediator.close();
        }
    }
    
//    @Test( groups = {"regressiontest", "pillartest"})
//    public void handleMessagesWithNoHandlers() throws Exception {
//        addDescription("Test the mediators handling of messages, where it does not have any handler.");
//        addStep("Clean up the mediator.", "The mediator should no longer contain any message handler.");
//        mediator.close();
//        
//        testMessage(new Message());
//        testMessage(new DeleteFileRequest());
//        testMessage(new GetAuditTrailsRequest());
//        testMessage(new GetChecksumsRequest());
//        testMessage(new GetFileIDsRequest());
//        testMessage(new GetFileRequest());
//        testMessage(new GetStatusRequest());
//        testMessage(new IdentifyContributorsForGetStatusRequest());
//        testMessage(new IdentifyPillarsForDeleteFileRequest());
//        testMessage(new IdentifyPillarsForGetChecksumsRequest());
//        testMessage(new IdentifyPillarsForGetFileIDsRequest());
//        testMessage(new IdentifyPillarsForGetFileRequest());
//        testMessage(new IdentifyPillarsForPutFileRequest());
//        testMessage(new IdentifyPillarsForReplaceFileRequest());
//        testMessage(new PutFileRequest());
//        testMessage(new ReplaceFileRequest());
//    }
//    
//    private void testMessage(Message message) {
//        addStep("Testing '" + message + "'.", "Should send an alarm for failed operation.");
//        alarmDispatcher.resetCallsForSendAlarm();
//        audits.resetCallsForAuditEvent();
//        mediator.onMessage(message);
//        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 1);
//        Assert.assertEquals(audits.getCallsForAuditEvent(), 0);
//    }
//    
//    @Test( groups = {"regressiontest", "pillartest"})
//    public void handleMessagesWithNoAlarms() throws Exception {
//        addDescription("Tests that if the alarm level is too high, then the alarms are not send for the Message.");
//        addStep("Setup variables, e.g. changing the alarm level.", "Should be ok.");
//        settings.getRepositorySettings().getPillarSettings().setAlarmLevel(AlarmLevel.EMERGENCY);
//        
//        alarmDispatcher.resetCallsForSendAlarm();
//        audits.resetCallsForAuditEvent();
//        mediator.onMessage(new Message());
//        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0);
//        Assert.assertEquals(audits.getCallsForAuditEvent(), 0);
//    }
}
