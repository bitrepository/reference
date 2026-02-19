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

import org.apache.commons.codec.DecoderException;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.*;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
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
import org.bitrepository.integrityservice.workflow.step.FullUpdateChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.HandleMissingChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.UpdateChecksumsStep;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.service.exception.WorkflowAbortedException;
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.util.*;


public class MissingChecksumTests extends ExtendedTestCase {
    private static final String PILLAR_1 = "pillar1";
    private static final String PILLAR_2 = "pillar2";

    private static final String DEFAULT_CHECKSUM = "0123456789";
    private static final String TEST_FILE_1 = "test-file-1";
    private String TEST_COLLECTION;

    protected Settings settings;
    protected IntegrityInformationCollector collector;
    protected IntegrityAlerter alerter;
    protected IntegrityModel model;
    protected IntegrityContributors integrityContributors;

    IntegrityReporter reporter;

    @BeforeEach
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");

        DerbyDatabaseDestroyer.deleteDatabase(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());

        IntegrityDatabaseCreator integrityDatabaseCreator = new IntegrityDatabaseCreator();
        integrityDatabaseCreator.createIntegrityDatabase(settings, null);

        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_2);

        Duration time = DatatypeFactory.newInstance().newDuration(0);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(time);
        TEST_COLLECTION = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        SettingsUtils.initialize(settings);

        collector = Mockito.mock(IntegrityInformationCollector.class);
        alerter = Mockito.mock(IntegrityAlerter.class);
        model = new IntegrityDatabase(settings);

        reporter = Mockito.mock(IntegrityReporter.class);
        integrityContributors = Mockito.mock(IntegrityContributors.class);

        SettingsUtils.initialize(settings);
    }

    @Test
    @Tag("regressiontest")
    @Tag("integritytest")
    public void testMissingChecksumAndStep() throws Exception {
        addDescription("Test that files initially are set to checksum-state unknown, and to missing in the "
                + "missing checksum step.");
        addStep("Ingest file to database", "");
        populateDatabase(model, TEST_FILE_1);

        addStep("Run missing checksum step.", "The file should be marked as missing at all pillars.");
        Mockito.doAnswer(invocation -> TEST_COLLECTION).when(reporter).getCollectionID();

        StatisticsCollector cs = new StatisticsCollector(TEST_COLLECTION);
        HandleMissingChecksumsStep missingChecksumStep = new HandleMissingChecksumsStep(model, reporter, cs, new Date(0));
        missingChecksumStep.performStep();
        for (String pillar : SettingsUtils.getPillarIDsForCollection(TEST_COLLECTION)) {
            Assertions.assertEquals(1, (long) cs.getPillarCollectionStat(pillar).getMissingChecksums());
        }
    }

    @Test
    @Tag("regressiontest")
    @Tag("integritytest")
    public void testMissingChecksumForFirstGetChecksums() throws WorkflowAbortedException {
        addDescription("Test that checksums are set to missing, when not found during GetChecksum.");
        addStep("Ingest file to database", "");
        Date testStart = new Date();
        populateDatabase(model, TEST_FILE_1);

        addStep("Add checksum results for only one pillar.", "");
        final ResultingChecksums resultingChecksums = createResultingChecksums(TEST_FILE_1);
        Mockito.doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(PILLAR_1, PILLAR_2)));
            eventHandler.handleEvent(new ChecksumsCompletePillarEvent(PILLAR_1, TEST_COLLECTION,
                    resultingChecksums, createChecksumSpecTYPE(), false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getChecksums(
                ArgumentMatchers.eq(TEST_COLLECTION), ArgumentMatchers.any(),
                ArgumentMatchers.any(ChecksumSpecTYPE.class), ArgumentMatchers.any(), ArgumentMatchers.anyString(),
                ArgumentMatchers.any(ContributorQuery[].class), ArgumentMatchers.any(EventHandler.class));

        Mockito.when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(Arrays.asList(PILLAR_1, PILLAR_2))).thenReturn(new HashSet<>());

        UpdateChecksumsStep step = new FullUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step.performStep();
        Mockito.verify(collector).getChecksums(ArgumentMatchers.eq(TEST_COLLECTION), ArgumentMatchers.any(),
                ArgumentMatchers.any(ChecksumSpecTYPE.class), ArgumentMatchers.any(), ArgumentMatchers.anyString(),
                ArgumentMatchers.any(ContributorQuery[].class), ArgumentMatchers.any(EventHandler.class));
        Mockito.verifyNoMoreInteractions(alerter);

        addStep("Check whether checksum is missing", "Should be missing at pillar two only.");
        Map<String, PillarCollectionMetric> metrics = model.getPillarCollectionMetrics(TEST_COLLECTION);
        Assertions.assertEquals(1, metrics.get(PILLAR_1).getPillarFileCount());
        Assertions.assertEquals(1, metrics.get(PILLAR_2).getPillarFileCount());

        List<String> missingChecksumsPillar1
                = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, PILLAR_1, testStart));
        Assertions.assertEquals(0, missingChecksumsPillar1.size());

        List<String> missingChecksumsPillar2
                = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, PILLAR_2, testStart));
        Assertions.assertEquals(1, missingChecksumsPillar2.size());
        Assertions.assertEquals(TEST_FILE_1, missingChecksumsPillar2.get(0));
    }

    @Test
    @Tag("regressiontest")
    @Tag("integritytest")
    public void testMissingChecksumDuringSecondIngest() throws WorkflowAbortedException {
        addDescription("Test that checksums are set to missing, when not found during GetChecksum, "
                + "even though they have been found before.");
        addStep("Ingest file to database", "");
        Date testStart = new Date();
        populateDatabase(model, TEST_FILE_1);

        addStep("Add checksum results for both pillar.", "");
        final ResultingChecksums resultingChecksums = createResultingChecksums(TEST_FILE_1);
        Mockito.doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(PILLAR_1, PILLAR_2)));
            eventHandler.handleEvent(new ChecksumsCompletePillarEvent(PILLAR_1, TEST_COLLECTION,
                    resultingChecksums, createChecksumSpecTYPE(), false));
            eventHandler.handleEvent(new ChecksumsCompletePillarEvent(PILLAR_2, TEST_COLLECTION,
                    resultingChecksums, createChecksumSpecTYPE(), false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getChecksums(
                ArgumentMatchers.eq(TEST_COLLECTION), ArgumentMatchers.any(),
                ArgumentMatchers.any(ChecksumSpecTYPE.class), ArgumentMatchers.any(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(ContributorQuery[].class),
                ArgumentMatchers.any(EventHandler.class));

        Mockito.when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(Arrays.asList(PILLAR_1, PILLAR_2))).thenReturn(new HashSet<>());

        UpdateChecksumsStep step1 = new FullUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step1.performStep();
        Mockito.verify(collector).getChecksums(ArgumentMatchers.eq(TEST_COLLECTION), ArgumentMatchers.any(),
                ArgumentMatchers.any(ChecksumSpecTYPE.class), ArgumentMatchers.any(), ArgumentMatchers.anyString(),
                ArgumentMatchers.any(ContributorQuery[].class), ArgumentMatchers.any(EventHandler.class));
        Mockito.verifyNoMoreInteractions(alerter);

        addStep("Check whether checksum is missing", "Should be missing at pillar two only.");
        Map<String, PillarCollectionMetric> metrics = model.getPillarCollectionMetrics(TEST_COLLECTION);
        Assertions.assertEquals(1, metrics.get(PILLAR_1).getPillarFileCount());
        Assertions.assertEquals(1, metrics.get(PILLAR_2).getPillarFileCount());

        for (String pillar : Arrays.asList(PILLAR_1, PILLAR_2)) {
            List<String> missingChecksums
                    = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, pillar, testStart));
            Assertions.assertEquals(0, missingChecksums.size());
        }

        addStep("Add checksum results for only the second pillar.", "");
        Mockito.doAnswer(invocation -> {
            EventHandler eventHandler = (EventHandler) invocation.getArguments()[6];
            eventHandler.handleEvent(new IdentificationCompleteEvent(TEST_COLLECTION, Arrays.asList(PILLAR_1, PILLAR_2)));
            eventHandler.handleEvent(new ChecksumsCompletePillarEvent(PILLAR_2, TEST_COLLECTION,
                    resultingChecksums, createChecksumSpecTYPE(), false));
            eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
            return null;
        }).when(collector).getChecksums(
                ArgumentMatchers.eq(TEST_COLLECTION), ArgumentMatchers.any(), ArgumentMatchers.any(ChecksumSpecTYPE.class),
                ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.any(ContributorQuery[].class),
                ArgumentMatchers.any(EventHandler.class));

        Mockito.when(integrityContributors.getActiveContributors())
                .thenReturn(new HashSet<>(Arrays.asList(PILLAR_1, PILLAR_2))).thenReturn(new HashSet<>());

        Date secondUpdate = new Date();
        UpdateChecksumsStep step2 = new FullUpdateChecksumsStep(collector, model, alerter, createChecksumSpecTYPE(),
                settings, TEST_COLLECTION, integrityContributors);
        step2.performStep();
        Mockito.verifyNoMoreInteractions(alerter);

        addStep("Check whether checksum is missing",
                "Should be missing at pillar one, and not on pillar two.");
        metrics = model.getPillarCollectionMetrics(TEST_COLLECTION);
        Assertions.assertEquals(1, metrics.get(PILLAR_1).getPillarFileCount());
        Assertions.assertEquals(1, metrics.get(PILLAR_2).getPillarFileCount());

        List<String> missingChecksumsPillar1
                = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, PILLAR_1, secondUpdate));
        Assertions.assertEquals(1, missingChecksumsPillar1.size());
        List<String> missingChecksumsPillar2
                = getIssuesFromIterator(model.findFilesWithMissingChecksum(TEST_COLLECTION, PILLAR_2, secondUpdate));
        Assertions.assertEquals(0, missingChecksumsPillar2.size());
    }

    protected void populateDatabase(IntegrityModel model, String... files) {
        FileIDsData data = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        XMLGregorianCalendar lastModificationTime = CalendarUtils.getNow();
        for (String f : files) {
            FileIDsDataItem item = new FileIDsDataItem();
            item.setFileID(f);
            item.setFileSize(BigInteger.ONE);
            item.setLastModificationTime(lastModificationTime);
            items.getFileIDsDataItem().add(item);
        }
        data.setFileIDsDataItems(items);
        String collectionID = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        model.addFileIDs(data, PILLAR_1, collectionID);
        model.addFileIDs(data, PILLAR_2, collectionID);
    }

    private ResultingChecksums createResultingChecksums(String... fileIDs) {
        ResultingChecksums res = new ResultingChecksums();
        res.getChecksumDataItems().addAll(createChecksumData(fileIDs));
        return res;
    }

    private List<ChecksumDataForChecksumSpecTYPE> createChecksumData(String... fileIDs) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<>();
        for (String fileID : fileIDs) {
            ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
            csData.setCalculationTimestamp(CalendarUtils.getNow());
            try {
                csData.setChecksumValue(Base16Utils.encodeBase16(MissingChecksumTests.DEFAULT_CHECKSUM));
            } catch (DecoderException e) {
                System.err.println(e.getMessage());
            }
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
}
