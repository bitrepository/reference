/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.integrationtest;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getfileinfos.conversation.FileInfosCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileInfosDataItem;
import org.bitrepository.bitrepositoryelements.ResultingFileInfos;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityDatabase;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.PillarCollectionMetric;
import org.bitrepository.integrityservice.cache.database.IntegrityDatabaseCreator;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;
import org.bitrepository.integrityservice.workflow.step.FullUpdateFileInfosStep;
import org.bitrepository.integrityservice.workflow.step.HandleMissingChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.UpdateFileInfosStep;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.service.exception.WorkflowAbortedException;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@SuppressWarnings("rawtypes")
public class MissingChecksumTestsFileInfos extends ExtendedTestCase {

    private static final String PILLAR_1 = "pillar1";
    private static final String PILLAR_2 = "pillar2";

    private static final String DEFAULT_CHECKSUM = "0123456789";
    private static final String TEST_FILE_1 = "test-file-1";
    private static final String TEST_FILE_2 = "test-file-2";
    private String TEST_COLLECTION;

    protected Settings settings;
    protected IntegrityInformationCollector collector;
    protected IntegrityAlerter alerter;
    protected IntegrityModel model;
    protected IntegrityContributors integrityContributors;

    IntegrityReporter reporter;

    @BeforeMethod(alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");

        DerbyDatabaseDestroyer.deleteDatabase(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());

        IntegrityDatabaseCreator integrityDatabaseCreator = new IntegrityDatabaseCreator();
        integrityDatabaseCreator.createIntegrityDatabase(settings, null);

        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_2);

        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0);
        TEST_COLLECTION = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        SettingsUtils.initialize(settings);

        collector = mock(IntegrityInformationCollector.class);
        alerter = mock(IntegrityAlerter.class);
        model = new IntegrityDatabase(settings);

        reporter = mock(IntegrityReporter.class);
        integrityContributors = mock(IntegrityContributors.class);

        SettingsUtils.initialize(settings);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testMissingChecksumAndStep() throws Exception {
        addDescription("Test that files initially are set to checksum-state unknown, and to missing in the "
                + "missing checksum step.");
        addStep("Ingest file to database", "");
        populateDatabase(model, TEST_FILE_1);

        addStep("Run missing checksum step.", "The file should be marked as missing at all pillars.");
        doAnswer(new Answer() {
            public String answer(InvocationOnMock invocation) {
                return TEST_COLLECTION;
            }
        }).when(reporter).getCollectionID();

        StatisticsCollector cs = new StatisticsCollector(TEST_COLLECTION);
        HandleMissingChecksumsStep missingChecksumStep = new HandleMissingChecksumsStep(model, reporter, cs, new Date(0));
        missingChecksumStep.performStep();
        for (String pillar : SettingsUtils.getPillarIDsForCollection(TEST_COLLECTION)) {
            assertTrue(cs.getPillarCollectionStat(pillar).getMissingChecksums() == 1);
        }
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testMissingChecksumForFirstGetChecksums() throws WorkflowAbortedException {
        addDescription("Test that checksums are set to missing, when not found during GetChecksum.");
        addStep("Ingest file to database", "");
        Date testStart = new Date();
        populateDatabase(model, TEST_FILE_1);

        addStep("Add checksum results for only one pillar.", "");
        final ResultingFileInfos resultingFileInfos = createResultingFileInfos(DEFAULT_CHECKSUM, TEST_FILE_1);
        doAnswer(new Answer() {
            public Void answer(InvocationOnMock invocation) {
                EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
                eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(PILLAR_1, PILLAR_2)));
                eventHandler.handleEvent(new FileInfosCompletePillarEvent(PILLAR_1, TEST_COLLECTION,
                        resultingFileInfos, createChecksumSpecTYPE(), false));
                eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
                return null;
            }
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), Matchers.<Collection<String>>any(), any(ChecksumSpecTYPE.class), anyString(), anyString(),
                any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(Arrays.asList(PILLAR_1, PILLAR_2))).thenReturn(new HashSet<>());

        UpdateFileInfosStep step = new FullUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        verify(collector).getFileInfos(eq(TEST_COLLECTION), Matchers.<Collection<String>>any(),
                any(ChecksumSpecTYPE.class), anyString(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verifyNoMoreInteractions(alerter);

        addStep("Check whether checksum is missing", "Should be missing at pillar two only.");
        Map<String, PillarCollectionMetric> metrics = model.getPillarCollectionMetrics(TEST_COLLECTION);
        assertEquals(metrics.get(PILLAR_1).getPillarFileCount(), 1);
        assertEquals(metrics.get(PILLAR_2).getPillarFileCount(), 1);

        List<String> missingChecksumsPillar1
                = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, PILLAR_1, testStart));
        assertEquals(missingChecksumsPillar1.size(), 0);

        List<String> missingChecksumsPillar2
                = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, PILLAR_2, testStart));
        assertEquals(missingChecksumsPillar2.size(), 1);
        assertEquals(missingChecksumsPillar2.get(0), TEST_FILE_1);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testMissingChecksumDuringSecondIngest() throws WorkflowAbortedException {
        addDescription("Test that checksums are set to missing, when not found during GetChecksum, "
                + "even though they have been found before.");
        addStep("Ingest file to database", "");
        Date testStart = new Date();
        populateDatabase(model, TEST_FILE_1);

        addStep("Add checksum results for both pillar.", "");
        final ResultingFileInfos resultingFileInfos = createResultingFileInfos(DEFAULT_CHECKSUM, TEST_FILE_1);
        doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(PILLAR_1, PILLAR_2)));
            eventHandler.handleEvent(new FileInfosCompletePillarEvent(PILLAR_1, TEST_COLLECTION,
                    resultingFileInfos, createChecksumSpecTYPE(), false));
            eventHandler.handleEvent(new FileInfosCompletePillarEvent(PILLAR_2, TEST_COLLECTION,
                    resultingFileInfos, createChecksumSpecTYPE(), false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), Matchers.any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(Arrays.asList(PILLAR_1, PILLAR_2))).thenReturn(new HashSet<>());

        UpdateFileInfosStep step1 = new FullUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step1.performStep();
        verify(collector).getFileInfos(eq(TEST_COLLECTION), Matchers.any(),
                any(ChecksumSpecTYPE.class), anyString(), anyString(), any(ContributorQuery[].class), any(EventHandler.class));
        verifyNoMoreInteractions(alerter);

        addStep("Check whether checksum is missing", "Should be missing at pillar two only.");
        Map<String, PillarCollectionMetric> metrics = model.getPillarCollectionMetrics(TEST_COLLECTION);
        assertEquals(metrics.get(PILLAR_1).getPillarFileCount(), 1);
        assertEquals(metrics.get(PILLAR_2).getPillarFileCount(), 1);

        for (String pillar : Arrays.asList(PILLAR_1, PILLAR_2)) {
            List<String> missingChecksums
                    = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, pillar, testStart));
            assertEquals(missingChecksums.size(), 0);
        }

        addStep("Add checksum results for only the second pillar.", "");
        doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(PILLAR_1, PILLAR_2)));
            eventHandler.handleEvent(new FileInfosCompletePillarEvent(PILLAR_2, TEST_COLLECTION,
                    resultingFileInfos, createChecksumSpecTYPE(), false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getFileInfos(
                eq(TEST_COLLECTION), Matchers.any(), any(ChecksumSpecTYPE.class), anyString(),
                anyString(), any(ContributorQuery[].class), any(EventHandler.class));

        when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(Arrays.asList(PILLAR_1, PILLAR_2))).thenReturn(new HashSet<>());

        Date secondUpdate = new Date();
        UpdateFileInfosStep step2 = new FullUpdateFileInfosStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step2.performStep();
        verifyNoMoreInteractions(alerter);

        addStep("Check whether checksum is missing", "Should be missing at pillar one, and not on pillar two.");
        metrics = model.getPillarCollectionMetrics(TEST_COLLECTION);
        assertEquals(metrics.get(PILLAR_1).getPillarFileCount(), 1);
        assertEquals(metrics.get(PILLAR_2).getPillarFileCount(), 1);

        List<String> missingChecksumsPillar1
                = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, PILLAR_1, secondUpdate));
        assertEquals(missingChecksumsPillar1.size(), 1);
        List<String> missingChecksumsPillar2
                = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, PILLAR_2, secondUpdate));
        assertEquals(missingChecksumsPillar2.size(), 0);
    }

    protected void populateDatabase(IntegrityModel model, String... files) {
        ResultingFileInfos rfi = createResultingFileInfos(null, files);
        String collectionID = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        model.addFileInfos(rfi.getFileInfosDataItem(), PILLAR_1, collectionID);
        model.addFileInfos(rfi.getFileInfosDataItem(), PILLAR_2, collectionID);
    }

    private ChecksumSpecTYPE createChecksumSpecTYPE() {
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumType(ChecksumType.MD5);
        return res;
    }

    /**
     * This is not the way to handle the iterators, as the lists might grow really long.
     * It's here to make the tests simple, and can be done as there's only small amounts of test data in the tests.
     */
    private List<String> getIssuesFromIterator(IntegrityIssueIterator it) {
        List<String> issues = new ArrayList<>();
        String issue;
        while ((issue = it.getNextIntegrityIssue()) != null) {
            issues.add(issue);
        }

        return issues;
    }

    private ResultingFileInfos createResultingFileInfos(String checksum, String... fileids) {
        ResultingFileInfos res = new ResultingFileInfos();
        res.getFileInfosDataItem().addAll(createFileInfoData(checksum, fileids));
        return res;
    }

    private List<FileInfosDataItem> createFileInfoData(String checksum, String... fileids) {
        List<FileInfosDataItem> res = new ArrayList<FileInfosDataItem>();
        for (String fileID : fileids) {
            FileInfosDataItem item = new FileInfosDataItem();
            item.setLastModificationTime(CalendarUtils.getNow());
            item.setCalculationTimestamp(CalendarUtils.getNow());
            item.setChecksumValue(Optional.ofNullable(checksum).map(Base16Utils::encodeBase16).orElse(null));
            item.setFileSize(BigInteger.valueOf(0L));
            item.setFileID(fileID);
            res.add(item);
        }
        return res;
    }
}