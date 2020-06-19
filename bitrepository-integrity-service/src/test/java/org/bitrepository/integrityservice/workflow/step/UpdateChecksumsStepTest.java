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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.service.exception.WorkflowAbortedException;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

@SuppressWarnings("rawtypes")
public class UpdateChecksumsStepTest extends WorkflowstepTest {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";

    @Test(groups = {"regressiontest"})
    public void testPositiveReply() throws WorkflowAbortedException {
        addDescription("Test the step for updating the checksums can handle COMPLETE operation event.");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1))).thenReturn(new HashSet<>());
        
        UpdateChecksumsStep step = new IncrementalUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), 
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class),
                any(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verifyNoMoreInteractions(alerter);
    }

    @Test(groups = {"regressiontest"})
    public void testAbortWorkflowWhenNegativeReply() {
        addDescription("Test the step for updating the checksums will abort the workflow in case "
                + "of FAILURE operation event and AbortOnFailedContributor = true .");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new ContributorFailedEvent(TEST_PILLAR_1, TEST_COLLECTION, ResponseCode.FAILURE));
                eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Problem encountered", null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>());
        when(integrityContributors.getFailedContributors()).thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)));
        
        settings.getReferenceSettings().getIntegrityServiceSettings().setAbortOnFailedContributor(true);
        UpdateChecksumsStep step = new IncrementalUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), 
                settings, TEST_COLLECTION, integrityContributors);
        try {
            step.performStep();
            Assert.fail("The step should have thrown an WorkflowAbortedException");
        } catch (WorkflowAbortedException e) {
            // nothing to do here
        }
        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(integrityContributors).failContributor(eq(TEST_PILLAR_1));
        verify(alerter).integrityFailed(anyString(), eq(TEST_COLLECTION));
    }
    
    @Test(groups = {"regressiontest"})
    public void testRetryCollectionWhenNegativeReply() throws WorkflowAbortedException {
        addDescription("Test the step for updating the file ids will retry on a FAILED event");
        
        final ResultingChecksums resultingChecksums = createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1);
        doAnswer(new Answer() {
            boolean firstAnswer = true;
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                if(firstAnswer) {
                    firstAnswer = false;
                    eventHandler.handleEvent(new ContributorFailedEvent(TEST_PILLAR_1, TEST_COLLECTION, ResponseCode.FAILURE));
                    eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Problem encountered", null));
                } else {
                    eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                    eventHandler.handleEvent(new ChecksumsCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                            resultingChecksums, createChecksumSpecTYPE(), false));
                    eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                }
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>());
        when(integrityContributors.getFailedContributors()).thenReturn(new HashSet<>());

        UpdateChecksumsStep step = new IncrementalUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), 
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        verify(collector, times(2)).getChecksums(eq(TEST_COLLECTION), any(),
                any(ChecksumSpecTYPE.class), any(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(model).addChecksums(resultingChecksums.getChecksumDataItems(), TEST_PILLAR_1, TEST_COLLECTION);
        verifyNoMoreInteractions(alerter);
        verify(integrityContributors).failContributor(eq(TEST_PILLAR_1));
        verify(integrityContributors).finishContributor(eq(TEST_PILLAR_1));
        
    }
    
    @Test(groups = {"regressiontest"})
    public void testContinueWorkflowNegativeReply() throws WorkflowAbortedException {
        addDescription("Test the step for updating the checksums will continue the workflow in case "
                + "of FAILURE operation event and AbortOnFailedContributor = false .");
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new ContributorFailedEvent(TEST_PILLAR_1, TEST_COLLECTION, ResponseCode.FAILURE));
                eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Problem encountered", null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>());
        when(integrityContributors.getFailedContributors()).thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)));
        
        settings.getReferenceSettings().getIntegrityServiceSettings().setAbortOnFailedContributor(false);
        UpdateChecksumsStep step = new IncrementalUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), 
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        
        verify(integrityContributors).failContributor(eq(TEST_PILLAR_1));
        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(alerter).integrityFailed(anyString(), eq(TEST_COLLECTION));
    }
    
    @Test(groups = {"regressiontest"})
    public void testIngestOfResults() throws WorkflowAbortedException {
        addDescription("Test the step for updating the checksums delivers the results to the integrity model.");
        final ResultingChecksums resultingChecksums = createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1);
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                eventHandler.handleEvent(new ChecksumsCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                        resultingChecksums, createChecksumSpecTYPE(), false));
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1))).thenReturn(new HashSet<>());
        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1))).thenReturn(new HashSet<>());
        
        UpdateChecksumsStep step = new IncrementalUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), 
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        verify(collector).getChecksums(eq(TEST_COLLECTION), any(),
                any(ChecksumSpecTYPE.class), any(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(model).addChecksums(resultingChecksums.getChecksumDataItems(), TEST_PILLAR_1, TEST_COLLECTION);
        verifyNoMoreInteractions(alerter);
    }

    @Test(groups = {"regressiontest"})
    public void testCallForChangingChecksumStates() throws WorkflowAbortedException {
        addDescription("Test the step for updating the checksums delivers the results to the integrity model.");
        final ResultingChecksums resultingChecksums = createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1);
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                eventHandler.handleEvent(new ChecksumsCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                        resultingChecksums, createChecksumSpecTYPE(), false));
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1))).thenReturn(new HashSet<>());
        
        UpdateChecksumsStep step = new FullUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), 
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        verify(collector).getChecksums(eq(TEST_COLLECTION), any(),
                any(ChecksumSpecTYPE.class), any(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(model).resetChecksumCollectionProgress(TEST_COLLECTION);
        verify(model).addChecksums(resultingChecksums.getChecksumDataItems(), TEST_PILLAR_1, TEST_COLLECTION);
        verifyNoMoreInteractions(alerter);
    }
    
    @Test(groups = {"regressiontest"})
    public void testPartialResults() throws WorkflowAbortedException {
        addDescription("Test that the number of partial is used for generating more than one request.");
        final ResultingChecksums resultingChecksums = createResultingChecksums(DEFAULT_CHECKSUM, TEST_FILE_1);

        addStep("Setup the collector mock to generate a isPartialResult=true event the first time and a " +
                "isPartialResult=false the second time",
                "The collectors getFileIDs should be called 2 time. The same goes for the 'models' addFileIDs");
        doAnswer(new Answer() {
            boolean firstPage = true;
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(TEST_PILLAR_1)));
                eventHandler.handleEvent(new ChecksumsCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                        resultingChecksums, createChecksumSpecTYPE(), firstPage));
                firstPage = false;
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1)))
            .thenReturn(new HashSet<>());
        
        UpdateChecksumsStep step = new IncrementalUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), 
                settings, TEST_COLLECTION, integrityContributors);

        step.performStep();
        verify(model, times(2)).addChecksums(resultingChecksums.getChecksumDataItems(), TEST_PILLAR_1, TEST_COLLECTION);
        verify(collector, times(2)).getChecksums(eq(TEST_COLLECTION), any(),
                any(ChecksumSpecTYPE.class), any(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
    }
    
    @Test(groups = {"regressiontest"})
    public void testFullChecksumCollection() throws WorkflowAbortedException {
        addDescription("Test that the full list of checksums is requested.");
        
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(model.getDateForNewestChecksumEntryForPillar(anyString(), anyString())).thenReturn(new Date(0));
        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1))).thenReturn(new HashSet<>());
        
        UpdateChecksumsStep step = new FullUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), 
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        
        ContributorQuery[] expectedContributorQueries = 
                makeFullQueries(settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID(), model); 
        
        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class),
        		any(), anyString(), eq(expectedContributorQueries), any(EventHandler.class));
        verifyNoMoreInteractions(alerter);
    }
    
    @Test(groups = {"regressiontest"})
    public void testIncrementalChecksumCollection() throws WorkflowAbortedException {
        addDescription("Test that only the list of new checksums is requested.");
        
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getChecksums(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), any(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        
        when(integrityContributors.getActiveContributors())
            .thenReturn(new HashSet<>(Arrays.asList(TEST_PILLAR_1))).thenReturn(new HashSet<>());
        
        UpdateChecksumsStep step = new IncrementalUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(), 
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        
        ContributorQuery[] expectedContributorQueries = 
                makeQueries(settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID(), model); 
        
        verify(collector).getChecksums(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class),
        		any(), anyString(), eq(expectedContributorQueries), any(EventHandler.class));
        verifyNoMoreInteractions(alerter);
    }
    
    private ContributorQuery[] makeFullQueries(List<String> pillars, IntegrityModel store) {
        List<ContributorQuery> res = new ArrayList<ContributorQuery>();
        for(String pillar : pillars) {
            Date latestChecksumDate = new Date(0);
            res.add(new ContributorQuery(pillar, latestChecksumDate, null, SettingsUtils.DEFAULT_MAX_CLIENT_PAGE_SIZE));
        }
        
        return res.toArray(new ContributorQuery[pillars.size()]);
    }
    
    private ContributorQuery[] makeQueries(List<String> pillars, IntegrityModel store) {
        List<ContributorQuery> res = new ArrayList<ContributorQuery>();
        for(String pillar : pillars) {
        	Date latestChecksumDate = store.getDateForNewestChecksumEntryForPillar(pillar, TEST_COLLECTION);
            res.add(new ContributorQuery(pillar, latestChecksumDate, null, SettingsUtils.DEFAULT_MAX_CLIENT_PAGE_SIZE));
        }
        
        return res.toArray(new ContributorQuery[pillars.size()]);
    }
    

    private ResultingChecksums createResultingChecksums(String checksum, String ... fileids) {
        ResultingChecksums res = new ResultingChecksums();
        res.getChecksumDataItems().addAll(createChecksumData(checksum, fileids));
        return res;
    }
    
    private List<ChecksumDataForChecksumSpecTYPE> createChecksumData(String checksum, String ... fileids) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileID : fileids) {
            ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
            csData.setCalculationTimestamp(CalendarUtils.getNow());
            csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
            csData.setFileID(fileID);
            res.add(csData);
        }
        return res;
    }

    private ChecksumSpecTYPE createChecksumSpecTYPE() {
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumType(ChecksumType.MD5);
        return res;
    }
}
