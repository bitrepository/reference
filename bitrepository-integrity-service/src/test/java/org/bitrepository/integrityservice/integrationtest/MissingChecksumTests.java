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
import org.bitrepository.TestGroups;
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
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
import org.bitrepository.pillar.integration.PostgresFixedPortContainer;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.exception.WorkflowAbortedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class MissingChecksumTests {
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

    /// Is NOT unused
    ///
    /// Creates a postgres server on port 9876 which match the requirement from
    /// ```
    /// <integritydatabase>
    ///     <driverclass>org.postgresql.Driver</driverclass>
    ///     <databaseurl>jdbc:postgresql://localhost:9876/integrityDB</databaseurl>
    ///     <username>testcontainerUser</username>
    ///     <password>testcontainerPassword</password>
    /// </integritydatabase>
    /// ```
    /// from bitrepository-core/src/test/resources/settings/xml/bitrepository-devel/ReferenceSettings.xml
    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgresFixedPortContainer("postgres:18-alpine")
                                                     .withFixedExposedPort(9876, 5432, InternetProtocol.TCP)
                                                     .withDatabaseName("integrityDB")
                                                     .withUsername("testcontainerUser")
                                                     .withPassword("testcontainerPassword")
                                                     .withLabel("purpose","integrityDB");

    @BeforeAll
    public void beforeAll() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        SettingsUtils.initialize(settings);
        IntegrityDatabaseCreator integrityDatabaseCreator = new IntegrityDatabaseCreator();
        integrityDatabaseCreator.createIntegrityDatabase(settings, "sql/postgres/integrityDBCreation.sql", "sql/postgres/integrityDB7to8migration.sql");

    }

    @BeforeEach
    public void beforeEach() throws Exception {
        clearDatabase();
        IntegrityDatabaseCreator integrityDatabaseCreator = new IntegrityDatabaseCreator();
        integrityDatabaseCreator.createIntegrityDatabase(settings, "sql/postgres/integrityDBCreation.sql", "sql/postgres/integrityDB7to8migration.sql");

        settings.getRepositorySettings().getCollections().getCollection().getFirst().getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().getFirst().getPillarIDs().getPillarID().add(PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().getFirst().getPillarIDs().getPillarID().add(PILLAR_2);

        Duration time = DatatypeFactory.newInstance().newDuration(0);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(time);
        TEST_COLLECTION = settings.getRepositorySettings().getCollections().getCollection().getFirst().getID();
        SettingsUtils.initialize(settings);

        collector = Mockito.mock(IntegrityInformationCollector.class);
        alerter = Mockito.mock(IntegrityAlerter.class);
        model = new IntegrityDatabase(settings);

        reporter = Mockito.mock(IntegrityReporter.class);
        integrityContributors = Mockito.mock(IntegrityContributors.class);

        SettingsUtils.initialize(settings);
    }

    public void clearDatabase() {
        DBConnector connector = new DBConnector(settings.getReferenceSettings()
                                                        .getIntegrityServiceSettings()
                                                        .getIntegrityDatabase());
        DatabaseUtils.executeStatement(connector, "DELETE FROM fileinfo");
        DatabaseUtils.executeStatement(connector, "DELETE FROM collection_progress");
        DatabaseUtils.executeStatement(connector, "DELETE FROM pillarstats");
        DatabaseUtils.executeStatement(connector, "DELETE FROM collectionstats");
        DatabaseUtils.executeStatement(connector, "DELETE FROM stats");
        DatabaseUtils.executeStatement(connector, "DELETE FROM pillar");
        DatabaseUtils.executeStatement(connector, "DELETE FROM collections");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag("integritytest")
    void testMissingChecksumAndStep() throws Exception {
        addDescription("Test that files initially are set to checksum-state unknown, and to missing in the "
                + "missing checksum step.");
        addStep("Ingest file to database", "");
        populateDatabase(model, TEST_FILE_1);

        addStep("Run missing checksum step.", "The file should be marked as missing at all pillars.");
        Mockito.doAnswer(invocation -> TEST_COLLECTION).when(reporter).getCollectionID();

        StatisticsCollector cs = new StatisticsCollector(TEST_COLLECTION);
        HandleMissingChecksumsStep missingChecksumStep = new HandleMissingChecksumsStep(model, reporter, cs, Instant.EPOCH, true);
        missingChecksumStep.performStep();
        for (String pillar : SettingsUtils.getPillarIDsForCollection(TEST_COLLECTION)) {
            Assertions.assertEquals(1, (long) cs.getPillarCollectionStat(pillar).getMissingChecksums());
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag("integritytest")
    void stepCarriesForwardMissingChecksumsWhenItCannotDetectThem() throws Exception {
        addDescription("Test that a workflow which cannot authoritatively detect missing checksums (e.g. an "
                + "incremental check, which only touches a subset of the collection) does not overwrite a "
                + "previously reported missing-checksums count with a fresh, partial recomputation - instead "
                + "the previous count is carried forward. This is a regression test for BITMAG-1230.");
        Mockito.doAnswer(invocation -> TEST_COLLECTION).when(reporter).getCollectionID();

        addStep("Ingest a file with no checksum and run the step as a full sweep (e.g. a complete check).",
                "The file should be marked as missing at all pillars, and the count persisted as a statistics entry.");
        populateDatabase(model, TEST_FILE_1);
        StatisticsCollector completeCheckStats = new StatisticsCollector(TEST_COLLECTION);
        new HandleMissingChecksumsStep(model, reporter, completeCheckStats, Instant.EPOCH, true).performStep();
        for (String pillar : SettingsUtils.getPillarIDsForCollection(TEST_COLLECTION)) {
            Assertions.assertEquals(1, (long) completeCheckStats.getPillarCollectionStat(pillar).getMissingChecksums());
        }
        persistStatistics(completeCheckStats);

        addStep("Run the step again as a workflow that cannot authoritatively detect missing checksums " +
                "(e.g. an incremental check).",
                "The previously reported count of 1 should be carried forward, not reset to 0.");
        StatisticsCollector incrementalCheckStats = new StatisticsCollector(TEST_COLLECTION);
        new HandleMissingChecksumsStep(model, reporter, incrementalCheckStats, null, false).performStep();
        for (String pillar : SettingsUtils.getPillarIDsForCollection(TEST_COLLECTION)) {
            Assertions.assertEquals(1, (long) incrementalCheckStats.getPillarCollectionStat(pillar).getMissingChecksums());
        }
    }

    private void persistStatistics(StatisticsCollector sc) {
        Instant now = Instant.now();
        sc.getCollectionStat().setStatsTime(now);
        sc.getCollectionStat().setFileCount(0L);
        sc.getCollectionStat().setDataSize(0L);
        sc.getCollectionStat().setChecksumErrors(0L);
        sc.getCollectionStat().setLatestFileTime(now);
        model.createStatistics(TEST_COLLECTION, sc);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag("integritytest")
    void testMissingChecksumForFirstGetChecksums() throws WorkflowAbortedException {
        addDescription("Test that checksums are set to missing, when not found during GetChecksum.");
        addStep("Ingest file to database", "");
        Instant testStart = Instant.now();
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
        Assertions.assertEquals(TEST_FILE_1, missingChecksumsPillar2.getFirst());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag("integritytest")
    void testMissingChecksumDuringSecondIngest() throws WorkflowAbortedException {
        addDescription("Test that checksums are set to missing, when not found during GetChecksum, "
                + "even though they have been found before.");
        addStep("Ingest file to database", "");
        Instant testStart = Instant.now();
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

        Instant secondUpdate = Instant.now();
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
        XMLGregorianCalendar lastModificationTime = CalendarUtils.getXmlGregorianCalendar(Instant.now());
        for (String f : files) {
            FileIDsDataItem item = new FileIDsDataItem();
            item.setFileID(f);
            item.setFileSize(BigInteger.ONE);
            item.setLastModificationTime(lastModificationTime);
            items.getFileIDsDataItem().add(item);
        }
        data.setFileIDsDataItems(items);
        String collectionID = settings.getRepositorySettings().getCollections().getCollection().getFirst().getID();
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
            csData.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(Instant.now()));
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
