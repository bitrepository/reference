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

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileinfos.conversation.FileInfosCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileInfosDataItem;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResultingFileInfos;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SuppressWarnings("rawtypes")
public class UpdateFileInfosStepTestFileInfos extends WorkflowstepTest {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";

    @Test(groups = {"regressiontest"})
    public void testPositiveReply() throws WorkflowAbortedException {
        addDescription("Test the step for updating the fileinfos can handle COMPLETE operation event.");
        doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1))).thenReturn(new HashSet<>());

        UpdateFileInfosStep step = new IncrementalUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        verify(collector).getFileInfos(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class),
                anyString(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verifyNoMoreInteractions(alerter);
    }

    @Test(groups = {"regressiontest"})
    public void testAbortWorkflowWhenNegativeReply() {
        addDescription("Test the step for updating the file infos will abort the workflow in case "
                + "of FAILURE operation event and AbortOnFailedContributor = true .");
        doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new ContributorFailedEvent(TEST_PILLAR_1, TEST_COLLECTION, ResponseCode.FAILURE));
            eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Problem encountered", null));
            return null;
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1)))
                .thenReturn(new HashSet<>());
        when(integrityContributors.getFailedContributors()).thenReturn(new HashSet<>(List.of(TEST_PILLAR_1)));

        settings.getReferenceSettings().getIntegrityServiceSettings().setAbortOnFailedContributor(true);
        UpdateFileInfosStep step = new IncrementalUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        try {
            step.performStep();
            Assert.fail("The step should have thrown an WorkflowAbortedException");
        } catch (WorkflowAbortedException e) {
            // nothing to do here
        }
        verify(collector).getFileInfos(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(integrityContributors).failContributor(eq(TEST_PILLAR_1));
        verify(alerter).integrityFailed(anyString(), eq(TEST_COLLECTION));
    }

    @Test(groups = {"regressiontest"})
    public void testRetryCollectionWhenNegativeReply() throws WorkflowAbortedException {
        addDescription("Test the step for updating the file infos will retry on a FAILED event");

        final ResultingFileInfos resultingFileInfos = createResultingFileInfos(DEFAULT_CHECKSUM, TEST_FILE_1);
        doAnswer(new Answer() {
            boolean firstAnswer = true;

            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                if (firstAnswer) {
                    firstAnswer = false;
                    eventHandler.handleEvent(new ContributorFailedEvent(TEST_PILLAR_1, TEST_COLLECTION, ResponseCode.FAILURE));
                    eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Problem encountered", null));
                } else {
                    eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, List.of(TEST_PILLAR_1)));
                    eventHandler.handleEvent(new FileInfosCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                            resultingFileInfos, createChecksumSpecTYPE(), false));
                    eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                }
                return null;
            }
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1)))
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1)))
                .thenReturn(new HashSet<>());
        when(integrityContributors.getFailedContributors()).thenReturn(new HashSet<>());

        UpdateFileInfosStep step = new IncrementalUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        verify(collector, times(2)).getFileInfos(eq(TEST_COLLECTION), any(),
                any(ChecksumSpecTYPE.class), anyString(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(model).addFileInfos(resultingFileInfos.getFileInfosDataItem(), TEST_PILLAR_1, TEST_COLLECTION);
        verifyNoMoreInteractions(alerter);
        verify(integrityContributors).failContributor(eq(TEST_PILLAR_1));
        verify(integrityContributors).finishContributor(eq(TEST_PILLAR_1));

    }

    @Test(groups = {"regressiontest"})
    public void testContinueWorkflowNegativeReply() throws WorkflowAbortedException {
        addDescription("Test the step for updating the fileinfos will continue the workflow in case "
                + "of FAILURE operation event and AbortOnFailedContributor = false .");
        doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new ContributorFailedEvent(TEST_PILLAR_1, TEST_COLLECTION, ResponseCode.FAILURE));
            eventHandler.handleEvent(new OperationFailedEvent(TEST_COLLECTION, "Problem encountered", null));
            return null;
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1)))
                .thenReturn(new HashSet<>());
        when(integrityContributors.getFailedContributors()).thenReturn(new HashSet<>(List.of(TEST_PILLAR_1)));

        settings.getReferenceSettings().getIntegrityServiceSettings().setAbortOnFailedContributor(false);
        UpdateFileInfosStep step = new IncrementalUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();

        verify(integrityContributors).failContributor(eq(TEST_PILLAR_1));
        verify(collector).getFileInfos(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(alerter).integrityFailed(anyString(), eq(TEST_COLLECTION));
    }

    @Test(groups = {"regressiontest"})
    public void testIngestOfResults() throws WorkflowAbortedException {
        addDescription("Test the step for updating the fileinfos delivers the results to the integrity model.");
        final ResultingFileInfos resultingFileInfos = createResultingFileInfos(DEFAULT_CHECKSUM, TEST_FILE_1);
        doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, List.of(TEST_PILLAR_1)));
            eventHandler.handleEvent(new FileInfosCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                    resultingFileInfos, createChecksumSpecTYPE(), false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1))).thenReturn(new HashSet<>());
        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1))).thenReturn(new HashSet<>());

        UpdateFileInfosStep step = new IncrementalUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        verify(collector).getFileInfos(eq(TEST_COLLECTION), any(),
                any(ChecksumSpecTYPE.class), anyString(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(model).addFileInfos(resultingFileInfos.getFileInfosDataItem(), TEST_PILLAR_1, TEST_COLLECTION);
        verifyNoMoreInteractions(alerter);
    }

    @Test(groups = {"regressiontest"})
    public void testCallForChangingChecksumStates() throws WorkflowAbortedException {
        addDescription("Test the step for updating the fileinfos delivers the results to the integrity model.");
        final ResultingFileInfos resultingFileInfos = createResultingFileInfos(DEFAULT_CHECKSUM, TEST_FILE_1);
        doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, List.of(TEST_PILLAR_1)));
            eventHandler.handleEvent(new FileInfosCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                    resultingFileInfos, createChecksumSpecTYPE(), false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1))).thenReturn(new HashSet<>());

        UpdateFileInfosStep step = new FullUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        verify(collector).getFileInfos(eq(TEST_COLLECTION), any(),
                any(ChecksumSpecTYPE.class), anyString(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verify(model).resetFileInfoCollectionProgress(TEST_COLLECTION);
        verify(model).addFileInfos(resultingFileInfos.getFileInfosDataItem(), TEST_PILLAR_1, TEST_COLLECTION);
        verifyNoMoreInteractions(alerter);
    }

    @Test(groups = {"regressiontest"})
    public void testPartialResults() throws WorkflowAbortedException {
        addDescription("Test that the number of partial is used for generating more than one request.");
        final ResultingFileInfos resultingFileInfos = createResultingFileInfos(DEFAULT_CHECKSUM, TEST_FILE_1);

        addStep("Setup the collector mock to generate a isPartialResult=true event the first time and a " +
                        "isPartialResult=false the second time",
                "The collectors getFileInfos should be called 2 times. The same goes for the 'models' addFileInfos");
        Answer callbackAnswer = new Answer() {
            boolean firstPage = true;

            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, List.of(TEST_PILLAR_1)));
                eventHandler.handleEvent(new FileInfosCompletePillarEvent(TEST_PILLAR_1, TEST_COLLECTION,
                        resultingFileInfos, createChecksumSpecTYPE(), firstPage));
                firstPage = false;
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        };
        doAnswer(callbackAnswer).when(collector).getFileInfos(
                eq(TEST_COLLECTION), anyCollection(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1)))
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1)))
                .thenReturn(new HashSet<>());

        UpdateFileInfosStep step = new IncrementalUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);

        step.performStep();
        verify(model, times(2)).addFileInfos(resultingFileInfos.getFileInfosDataItem(), TEST_PILLAR_1, TEST_COLLECTION);
        verify(collector, times(2)).getFileInfos(eq(TEST_COLLECTION), any(),
                any(ChecksumSpecTYPE.class), anyString(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
    }

    @Test(groups = {"regressiontest"})
    public void testFullFileInfoCollection() throws WorkflowAbortedException {
        addDescription("Test that the full list of file infos is requested.");

        doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(model.getDateForNewestChecksumEntryForPillar(anyString(), anyString())).thenReturn(new Date(0));
        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1))).thenReturn(new HashSet<>());

        UpdateFileInfosStep step = new FullUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();

        ContributorQuery[] expectedContributorQueries =
                makeFullQueries(settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID()
                );

        verify(collector).getFileInfos(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class),
                anyString(), anyString(), eq(expectedContributorQueries), any(EventHandler.class));
        verifyNoMoreInteractions(alerter);
    }

    @Test(groups = {"regressiontest"})
    public void testIncrementalFileInfoCollection() throws WorkflowAbortedException {
        addDescription("Test that only the list of new fileinfos is requested.");

        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(List.of(TEST_PILLAR_1))).thenReturn(new HashSet<>());

        UpdateFileInfosStep step = new IncrementalUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();

        ContributorQuery[] expectedContributorQueries =
                makeQueries(settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID(), model);

        verify(collector).getFileInfos(eq(TEST_COLLECTION), any(), any(ChecksumSpecTYPE.class),
                anyString(), anyString(), eq(expectedContributorQueries), any(EventHandler.class));
        verifyNoMoreInteractions(alerter);
    }

    private ContributorQuery[] makeFullQueries(List<String> pillars) {
        List<ContributorQuery> res = new ArrayList<>();
        for (String pillar : pillars) {
            Date latestChecksumDate = new Date(0);
            res.add(new ContributorQuery(pillar, latestChecksumDate, null, SettingsUtils.DEFAULT_MAX_CLIENT_PAGE_SIZE));
        }

        return res.toArray(new ContributorQuery[pillars.size()]);
    }

    private ContributorQuery[] makeQueries(List<String> pillars, IntegrityModel store) {
        List<ContributorQuery> res = new ArrayList<>();
        for (String pillar : pillars) {
            Date latestChecksumDate = store.getDateForNewestChecksumEntryForPillar(pillar, TEST_COLLECTION);
            res.add(new ContributorQuery(pillar, latestChecksumDate, null, SettingsUtils.DEFAULT_MAX_CLIENT_PAGE_SIZE));
        }

        return res.toArray(new ContributorQuery[pillars.size()]);
    }


    private ResultingFileInfos createResultingFileInfos(String checksum, String... fileids) {
        ResultingFileInfos res = new ResultingFileInfos();
        res.getFileInfosDataItem().addAll(createFileInfoData(checksum, fileids));
        return res;
    }

    private List<FileInfosDataItem> createFileInfoData(String checksum, String... fileids) {
        List<FileInfosDataItem> res = new ArrayList<>();
        for (String fileID : fileids) {
            FileInfosDataItem item = new FileInfosDataItem();
            item.setLastModificationTime(CalendarUtils.getNow());
            item.setCalculationTimestamp(CalendarUtils.getNow());
            item.setChecksumValue(Base16Utils.encodeBase16(checksum));
            item.setFileID(fileID);
            res.add(item);
        }
        return res;
    }

    private ChecksumSpecTYPE createChecksumSpecTYPE() {
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumType(ChecksumType.MD5);
        return res;
    }
}