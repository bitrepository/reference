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

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.checking.IntegrityReport;
import org.bitrepository.integrityservice.collector.eventhandler.ChecksumsUpdaterAndValidatorEventHandler;
import org.bitrepository.integrityservice.mocks.MockAlarmDispatcher;
import org.bitrepository.integrityservice.mocks.MockChecker;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChecksumUpdaterAndValidatorEventHandlerTest extends ExtendedTestCase {
    
    @Test(groups = {"regressiontest"})
    public void testChecksumUpdaterAndValidatorEvent() {
        addDescription("Tests the functionality of this event handler.");
        addStep("Setup variables", "No errors");
        MockIntegrityModel mockCache = new MockIntegrityModel(new TestIntegrityModel());
        MockChecker checker = new MockChecker();
        MockAlarmDispatcher alarmDispatcher = new MockAlarmDispatcher();
        FileIDs fileIDs = new FileIDs();
        fileIDs.setAllFileIDs("true");
        String pillarId = "TEST-PILLAR";
        String correlationId = "correlation-id";
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE(); 
        
        addStep("Instantiate the ChecksumUpdaterAndValidatorEventHandler", "Should be OK.");
        ChecksumsUpdaterAndValidatorEventHandler eventHandler = new ChecksumsUpdaterAndValidatorEventHandler(
                mockCache, checker, alarmDispatcher, fileIDs);
        
        addStep("Validate initial step", "Should not have called any mock");
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");
        
        addStep("Handle Progress", "Should not call anything.");
        ContributorEvent progressEvent = new ContributorEvent(OperationEventType.PROGRESS, "progess", pillarId);
        progressEvent.setConversationID(correlationId);
        eventHandler.handleEvent(progressEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");

        addStep("Handle Failure", "Should only perform a call to the checker.");
        ContributorEvent failedEvent = new ContributorEvent(OperationEventType.FAILED, "failure", pillarId);
        failedEvent.setConversationID(correlationId);
        eventHandler.handleEvent(failedEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 1, "Should give a call for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");
        
        addStep("Handle Complete", "Should only perform a call to the checker.");
        ContributorEvent completeEvent = new ContributorEvent(OperationEventType.COMPLETE, "complete", pillarId);
        completeEvent.setConversationID(correlationId);
        eventHandler.handleEvent(completeEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 2, "Should give another call for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");

        addStep("Handle PillarComplete", "Should only perform a call to the checker.");
        ResultingChecksums resChecksums = new ResultingChecksums();
        ChecksumsCompletePillarEvent pillarCompleteEvent = new ChecksumsCompletePillarEvent(resChecksums, csType, pillarId, "pillar complete");
        pillarCompleteEvent.setConversationID(correlationId);
        eventHandler.handleEvent(pillarCompleteEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 1, "Should be one call for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 2, "Should not give another call for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");
    }
    
    @Test(groups = {"regressiontest"})
    public void testChecksumUpdaterAndValidatorEventFailure() {
        addDescription("Tests the functionality of this event handler.");
        addStep("Setup variables", "No errors");
        MockIntegrityModel mockCache = new MockIntegrityModel(new TestIntegrityModel());
        MockAlarmDispatcher alarmDispatcher = new MockAlarmDispatcher();
        FileIDs fileIDs = new FileIDs();
        String fileId = "FILE-ID";
        fileIDs.setFileID(fileId);
        String pillarId = "TEST-PILLAR";
        String correlationId = "correlation-id";

        addStep("Create a checker, which returns a failed integrity report", "");
        MockChecker checker = new MockChecker() {
            @Override
            public IntegrityReport checkChecksum(FileIDs fileIDs) {
                IntegrityReport res = super.checkChecksum(fileIDs);
                res.addFileWithCheksumSpecIssues(fileIDs.getFileID());
                return res;
            }
        };

        addStep("Instantiate the ChecksumUpdaterAndValidatorEventHandler", "Should be OK.");
        ChecksumsUpdaterAndValidatorEventHandler eventHandler = new ChecksumsUpdaterAndValidatorEventHandler(
                mockCache, checker, alarmDispatcher, fileIDs);
        
        addStep("Validate initial step", "Should not have called any mock");
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 0, "Should be no calls for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 0, "Should not have integrity failures.");
        
        addStep("Handle Complete", "Should only perform a call to the checker.");
        ContributorEvent completeEvent = new ContributorEvent(OperationEventType.COMPLETE, "complete", pillarId);
        completeEvent.setConversationID(correlationId);
        eventHandler.handleEvent(completeEvent);
        Assert.assertEquals(mockCache.getCallsForAddChecksums(), 0, "Should be no calls for add checksums");
        Assert.assertEquals(mockCache.getCallsForAddFileIDs(), 0, "Should be no calls for add FileIDs");
        Assert.assertEquals(checker.getCallsForCheckChecksums(), 1, "Should give one call for CheckChecksums");
        Assert.assertEquals(checker.getCallsForCheckFileIDs(), 0, "Should be no calls for CheckFileIDs");
        Assert.assertEquals(alarmDispatcher.getCallsForIntegrityFailed(), 1, "Should send an alarm for failure.");

    }    
}
