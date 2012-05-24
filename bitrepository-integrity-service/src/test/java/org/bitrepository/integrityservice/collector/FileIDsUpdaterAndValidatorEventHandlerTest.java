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
package org.bitrepository.integrityservice.collector;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.checking.IntegrityReport;
import org.bitrepository.integrityservice.collector.eventhandler.FileIDsUpdaterAndValidatorEventHandler;
import org.bitrepository.integrityservice.mocks.MockIntegrityAlerter;
import org.bitrepository.integrityservice.mocks.MockChecker;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FileIDsUpdaterAndValidatorEventHandlerTest extends ExtendedTestCase {
    private static final String CONVERSATION_ID = "conversationId";

    @Test(groups = {"regressiontest"})
    public void testFileIDsUpdaterAndValidatorEvent() {
        addDescription("Tests the functionality of this event handler.");
        addStep("Setup variables", "No errors");
        MockIntegrityModel mockCache = new MockIntegrityModel(new TestIntegrityModel());
        MockChecker checker = new MockChecker();
        MockIntegrityAlerter alarmDispatcher = new MockIntegrityAlerter();
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        String pillarId = "TEST-PILLAR";
        String correlationId = "correlation-id";
        
        addStep("Instantiate the ChecksumUpdaterAndValidatorEventHandler", "Should be OK.");
        FileIDsUpdaterAndValidatorEventHandler eventHandler = new FileIDsUpdaterAndValidatorEventHandler(
                mockCache, checker, alarmDispatcher, fileIDs);
        
        addStep("Validate initial step", "Should not have called any mock");
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");
        
        addStep("Handle Progress", "Should not call anything.");
        ContributorEvent progressEvent = new ContributorEvent(OperationEventType.PROGRESS, "progess", pillarId,
                CONVERSATION_ID);
        progressEvent.setConversationID(correlationId);
        eventHandler.handleEvent(progressEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");

        addStep("Handle Failure", "Should only perform a call to the checker.");
        ContributorEvent failedEvent = new ContributorEvent(OperationEventType.FAILED, "failure", pillarId,
                CONVERSATION_ID);
        failedEvent.setConversationID(correlationId);
        eventHandler.handleEvent(failedEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 1, "Should give a call for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");
        
        addStep("Handle Complete", "Should only perform a call to the checker.");
        ContributorEvent completeEvent = new ContributorEvent(OperationEventType.COMPLETE, "complete", pillarId,
                CONVERSATION_ID);
        completeEvent.setConversationID(correlationId);
        eventHandler.handleEvent(completeEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 2, "Should give another call for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");

        addStep("Handle PillarComplete", "Should only perform a call to the checker.");
        ResultingFileIDs resultingFileIDs = new ResultingFileIDs();
        FileIDsData fileIDsData = new FileIDsData();
        fileIDsData.setFileIDsDataItems(new FileIDsDataItems());
        resultingFileIDs.setFileIDsData(fileIDsData);
        FileIDsCompletePillarEvent pillarCompleteEvent = new FileIDsCompletePillarEvent(resultingFileIDs, pillarId, 
                "pillar complete", CONVERSATION_ID);
        pillarCompleteEvent.setConversationID(correlationId);
        eventHandler.handleEvent(pillarCompleteEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 1, "Should give a call for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 2, "Should not give another call for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");
    }
    
    @Test(groups = {"regressiontest"})
    public void testFileIDsUpdaterAndValidatorEventFailure() {
        addDescription("Tests the functionality of this event handler.");
        addStep("Setup variables", "No errors");
        MockIntegrityModel mockCache = new MockIntegrityModel(new TestIntegrityModel());
        MockIntegrityAlerter alarmDispatcher = new MockIntegrityAlerter();
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        String pillarId = "TEST-PILLAR";
        String correlationId = "correlation-id";
        
        addStep("Create a checker, which returns a failed integrity report", "");
        MockChecker checker = new MockChecker() {
            @Override
            public IntegrityReport checkFileIDs(FileIDs fileIDs) {
                IntegrityReport res = super.checkFileIDs(fileIDs);
                res.addFileWithCheksumSpecIssues(fileIDs.getFileID());
                return res;
            }
        };

        addStep("Instantiate the ChecksumUpdaterAndValidatorEventHandler", "Should be OK.");
        FileIDsUpdaterAndValidatorEventHandler eventHandler = new FileIDsUpdaterAndValidatorEventHandler(
                mockCache, checker, alarmDispatcher, fileIDs);
        
        addStep("Validate initial step", "Should not have called any mock");
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");
        
        addStep("Handle Complete", "Should only perform a call to the checker.");
        ContributorEvent completeEvent = new ContributorEvent(OperationEventType.COMPLETE, "complete", pillarId,
                CONVERSATION_ID);
        completeEvent.setConversationID(correlationId);
        eventHandler.handleEvent(completeEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 1, "Should give a call for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 1, "Should send an alarm for failure.");
    }    
}
