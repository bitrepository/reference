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
package org.bitrepository.integrityservice.workflow.step;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.exception.WorkflowAbortedException;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("rawtypes")
public class UpdateFileIDsStepTest extends WorkflowstepTest {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";

    @Test(groups = {"regressiontest"})
    public void testPositiveReply() throws WorkflowAbortedException {
        addDescription("Test the step for updating the file ids can handle COMPLETE operation event.");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[4];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1))).thenReturn(new HashSet<>());
        
        UpdateFileIDsStep step = new FullUpdateFileIDsStep(collector, model, alerter, settings, TEST_COLLECTION, 
                integrityContributors);
        step.performStep();
        verify(collector).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));
        verifyNoMoreInteractions(alerter);
    }
    
    @Test(groups = {"regressiontest"})
    public void testAbortWorkflowWhenNegativeReply() {
        addDescription("Test the step for updating the file ids will throw an WorkflowAbortedException"
                + "when AbortOnFailedContributor is set to true and a FAILED event is received.");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[4];
                eventHandler.handleEvent(new ContributorFailedEvent(TEST_PILLAR_1, TEST_COLLECTION, ResponseCode.FAILURE));
                eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Operation failed", null));
                return null;
            }
        }).when(collector).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));
        
        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>());
        when(integrityContributors.getFailedContributors()).thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)));
                
        settings.getReferenceSettings().getIntegrityServiceSettings().setAbortOnFailedContributor(true);
        UpdateFileIDsStep step = new FullUpdateFileIDsStep(collector, model, alerter, settings, TEST_COLLECTION, 
                integrityContributors);
        
        try {
            step.performStep();
            Assert.fail("The step should have thrown an WorkflowAbortedException");
        } catch (WorkflowAbortedException e) {
            // nothing to do here
        }
        
        verify(collector).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));
        verify(integrityContributors).failContributor(eq(TEST_PILLAR_1));
        verify(alerter).integrityFailed(anyString(), anyString());
    }

    @Test(groups = {"regressiontest"})
    public void testRetryCollectionWhenNegativeReply() throws WorkflowAbortedException {
        addDescription("Test the step for updating the file ids will retry on a FAILED event");
        final ResultingFileIDs resultingFileIDs = createResultingFileIDs(TEST_FILE_1);
        Answer callbackAnswer = new Answer() {
            boolean firstAnswer = true;
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[4];
                if(firstAnswer) {
                    firstAnswer = false;
                    eventHandler.handleEvent(new ContributorFailedEvent(TEST_PILLAR_1, TEST_COLLECTION, ResponseCode.FAILURE));
                    eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Operation failed", null));
                } else {
                    eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                    eventHandler.handleEvent(new FileIDsCompletePillarEvent(
                            TEST_PILLAR_1, TEST_COLLECTION, resultingFileIDs, false));
                    eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                }
                return null;
            }
        };
        doAnswer(callbackAnswer).when(collector).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));
        
        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>());
        when(integrityContributors.getFailedContributors()).thenReturn(new HashSet<>());
                
        settings.getReferenceSettings().getIntegrityServiceSettings().setAbortOnFailedContributor(true);
        UpdateFileIDsStep step = new FullUpdateFileIDsStep(collector, model, alerter, settings, TEST_COLLECTION, 
                integrityContributors);
        
        step.performStep();
        
        verify(collector, times(2)).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));
        verify(integrityContributors).failContributor(eq(TEST_PILLAR_1));
        verify(integrityContributors).finishContributor(eq(TEST_PILLAR_1));
    }
    
    @Test(groups = {"regressiontest"})
    public void testContinueWorkflowWhenNegativeReply() throws WorkflowAbortedException {
        addDescription("Test the step for updating the file ids will continue when getting an FAILED operation event"
                + " when AbortOnFailedContributor is set to false");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[4];
                eventHandler.handleEvent(new ContributorFailedEvent(TEST_PILLAR_1, TEST_COLLECTION, ResponseCode.FAILURE));
                eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Operation failed", null));
                return null;
            }
        }).when(collector).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));
        
        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1))).thenReturn(new HashSet<>());
        when(integrityContributors.getFailedContributors()).thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)));
        
        settings.getReferenceSettings().getIntegrityServiceSettings().setAbortOnFailedContributor(false);
        UpdateFileIDsStep step = new FullUpdateFileIDsStep(collector, model, alerter, settings, TEST_COLLECTION, 
                integrityContributors);
        
        step.performStep();
        verify(integrityContributors).failContributor(eq(TEST_PILLAR_1));
        verify(alerter).integrityFailed(anyString(), anyString());
        verify(collector).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));
    }

    

    @Test(groups = {"regressiontest"})
    public void testIngestOfResults() throws WorkflowAbortedException {
        addDescription("Test the step for updating the file ids can ingest the data correctly into the store.");
        final ResultingFileIDs resultingFileIDs = createResultingFileIDs(TEST_FILE_1);
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[4];
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                eventHandler.handleEvent(new FileIDsCompletePillarEvent(
                        TEST_PILLAR_1, TEST_COLLECTION, resultingFileIDs, false));
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));
        
        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1))).thenReturn(new HashSet<>());

        UpdateFileIDsStep step = new FullUpdateFileIDsStep(collector, model, alerter, settings, TEST_COLLECTION, 
                integrityContributors);
        step.performStep();
        verify(model).addFileIDs(resultingFileIDs.getFileIDsData(), TEST_PILLAR_1, TEST_COLLECTION);
        verify(integrityContributors).finishContributor(eq(TEST_PILLAR_1));
    }


    @Test(groups = {"regressiontest"})
    public void testPartialResults() throws WorkflowAbortedException {
        addDescription("Test that the number of partial is used for generating more than one request.");
        final ResultingFileIDs resultingFileIDs = createResultingFileIDs(TEST_FILE_1);

        addStep("Setup the collector mock to generate a isPartialResult=true event the first time and a " +
                "isPartialResult=false the second time",
                "The collectors getFileIDs should be called 2 time. The same goes for the 'models' addFileIDs");
        Answer callbackAnswer = new Answer() {
            boolean firstPage = true;
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[4];
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                eventHandler.handleEvent(
                        new FileIDsCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION, resultingFileIDs, firstPage));
                firstPage = false;
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        };
        doAnswer(callbackAnswer).when(collector).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(),
                any(ContributorQuery[].class),
                any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>());

        UpdateFileIDsStep step = new FullUpdateFileIDsStep(collector, model, alerter, settings, TEST_COLLECTION, 
                integrityContributors);
        
        step.performStep();
        verify(model, times(2)).addFileIDs(resultingFileIDs.getFileIDsData(), TEST_PILLAR_1, TEST_COLLECTION);
        verify(integrityContributors).succeedContributor(eq(TEST_PILLAR_1));
        verify(integrityContributors).finishContributor(eq(TEST_PILLAR_1));
        verify(collector, times(2)).getFileIDs(
                eq(TEST_COLLECTION), any(), anyString(), any(ContributorQuery[].class),
                any(EventHandler.class));
    }

    private ResultingFileIDs createResultingFileIDs(String ... fileIDs) {
        ResultingFileIDs res = new ResultingFileIDs();
        res.setFileIDsData(getFileIDsData(fileIDs));
        return res;
    }

    private FileIDsData getFileIDsData(String... fileIDs) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();

        for(String fileID : fileIDs) {
            FileIDsDataItem dataItem = new FileIDsDataItem();
            dataItem.setFileID(fileID);
            dataItem.setFileSize(BigInteger.valueOf(items.getFileIDsDataItem().size() + 1));
            dataItem.setLastModificationTime(CalendarUtils.getNow());
            items.getFileIDsDataItem().add(dataItem);
        }

        res.setFileIDsDataItems(items);
        return res;
    }
}
