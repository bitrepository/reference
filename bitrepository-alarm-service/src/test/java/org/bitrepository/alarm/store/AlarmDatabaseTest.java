/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2059 The Royal Danish Library and The State Archives, Denmark
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
package org.bitrepository.alarm.store;

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.BindMode;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

/**
 * Sees if alarms are correctly stored in the database.
 */
@Testcontainers
class AlarmDatabaseTest {
    /**
     * The settings for the tests. Should be instantiated in the setup.
     */
    Settings settings;
    String fileID = "TEST-FILE-ID-" + Instant.now().toEpochMilli();
    String component1 = "ACTOR-1";
    String component2 = "ACTOR-2";
    String collection1 = "collection1";
    String collection2 = "collection2";
    String DATABASE_NAME = "alarmservicedb";

    @Container
    PostgreSQLContainer postgreSQLContainer =
            new PostgreSQLContainer("postgres:18-alpine")
                    .withDatabaseName(DATABASE_NAME)
                    .withClasspathResourceMapping("sql/postgres/alarmServiceDBCreation.sql",
                                                  "/docker-entrypoint-initdb.d/init.sql",
                                                  BindMode.READ_ONLY);

    @BeforeEach
    void setup() {
        settings = TestSettingsProvider.reloadSettings("AlarmDatabaseUnderTest");
        var alarmServiceDatabaseSettings = settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase();
        alarmServiceDatabaseSettings.setDatabaseURL(postgreSQLContainer.getJdbcUrl());
        alarmServiceDatabaseSettings.setUsername(postgreSQLContainer.getUsername());
        alarmServiceDatabaseSettings.setPassword(postgreSQLContainer.getPassword());
        alarmServiceDatabaseSettings.setDriverClass(postgreSQLContainer.getDriverClassName());
    }

    @AfterEach
    void cleanupDatabase() {
        // TODO
        DBConnector connector = new DBConnector(settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + AlarmDatabaseConstants.ALARM_TABLE);
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + AlarmDatabaseConstants.COMPONENT_TABLE);
    }

    @AfterAll
    static void shutdown() {
        addStep("Cleanup after test.", "Should remove directory with test material.");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag(TestGroups.DATABASETEST)
    void AlarmDatabaseExtractionTest() {
        addDescription("Testing the connection to the alarm service database especially with regards to "
                + "extracting the data from it.");
        addStep("Setup the variables and constants.", "Should be ok.");
        Instant restrictionDate = Instant.ofEpochMilli(123456789); // Sometime between epoch and now!

        addStep("Adds the variables to the settings and instantiates the database cache",
                "Should be connected.");
        AlarmDAOFactory alarmDAOFactory = new AlarmDAOFactory();
        AlarmServiceDAO database = alarmDAOFactory.getAlarmServiceDAOInstance(
                settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());

        addStep("Populate the database with two alarms.", "Should be inserted.");
        for (Alarm alarm : makeAlarms()) {
            database.addAlarm(alarm);
        }

        addStep("Try to extract all the data from the database.", "Should deliver both alarms.");
        List<Alarm> extractedAlarms =
                database.extractAlarms(null, null, (Instant) null, (Instant) null, null,
                        null, null, false);
        Assertions.assertEquals(2, extractedAlarms.size());

        addStep("Try to extract the alarms for component 1.", "Should deliver one alarm.");
        extractedAlarms =
                database.extractAlarms(component1, null, (Instant) null, (Instant) null, null,
                        null, null, false);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component1, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.COMPONENT_FAILURE, extractedAlarms.get(0).getAlarmCode());
        Assertions.assertNull(extractedAlarms.get(0).getFileID());

        addStep("Try to extract the alarms for component 2.", "Should deliver one alarm.");
        extractedAlarms =
                database.extractAlarms(component2, null, (Instant) null, (Instant) null, null,
                        null, null, false);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component2, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.CHECKSUM_ALARM, extractedAlarms.get(0).getAlarmCode());
        Assertions.assertEquals(fileID, extractedAlarms.get(0).getFileID());

        addStep("Try to extract the alarms for the alarm code 'COMPONENT_FAILURE'.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, AlarmCode.COMPONENT_FAILURE, (Instant) null,
                (Instant) null, null, null, null, false);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component1, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.COMPONENT_FAILURE, extractedAlarms.get(0).getAlarmCode());
        Assertions.assertNull(extractedAlarms.get(0).getFileID());

        addStep("Try to extract the alarms for the alarm code 'CHECKSUM_ALARM'.",
                "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, AlarmCode.CHECKSUM_ALARM, (Instant) null,
                (Instant) null, null, null, null, false);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component2, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.CHECKSUM_ALARM, extractedAlarms.get(0).getAlarmCode());
        Assertions.assertEquals(fileID, extractedAlarms.get(0).getFileID());

        addStep("Try to extract the new alarm.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, restrictionDate,
                (Instant) null, null, null, null, false);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component2, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.CHECKSUM_ALARM, extractedAlarms.get(0).getAlarmCode());
        Assertions.assertEquals(fileID, extractedAlarms.get(0).getFileID());

        addStep("Try to extract the old alarm.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, (Instant) null, restrictionDate,
                null, null, null, false);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component1, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.COMPONENT_FAILURE, extractedAlarms.get(0).getAlarmCode());
        Assertions.assertNull(extractedAlarms.get(0).getFileID());

        addStep("Try to extract the alarms for the file id.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, (Instant) null, (Instant) null, fileID,
                null, null, false);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component2, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.CHECKSUM_ALARM, extractedAlarms.get(0).getAlarmCode());
        Assertions.assertEquals(fileID, extractedAlarms.get(0).getFileID());

        addStep("Try to extract the alarms for the collection id.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, (Instant) null, (Instant) null,
                null, collection1, null, false);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component1, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(collection1, extractedAlarms.get(0).getCollectionID());
        Assertions.assertEquals(AlarmCode.COMPONENT_FAILURE, extractedAlarms.get(0).getAlarmCode());

        addStep("Try to extract the oldest alarm from the database.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, (Instant) null, (Instant) null,
                null, null, 1, true);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component1, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.COMPONENT_FAILURE, extractedAlarms.get(0).getAlarmCode());
        Assertions.assertNull(extractedAlarms.get(0).getFileID());

        addStep("Try to extract the newest alarm from the database.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, (Instant) null, (Instant) null,
                null, null, 1, false);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(component2, extractedAlarms.get(0).getAlarmRaiser());
        Assertions.assertEquals(AlarmCode.CHECKSUM_ALARM, extractedAlarms.get(0).getAlarmCode());
        Assertions.assertEquals(fileID, extractedAlarms.get(0).getFileID());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag(TestGroups.DATABASETEST)
    void AlarmDatabaseLargeIngestionTest() {
        addDescription("Testing the ingestion of a large texts into the database");
        addStep("Setup and create alarm", "");
        AlarmDAOFactory alarmDAOFactory = new AlarmDAOFactory();
        AlarmServiceDAO database = alarmDAOFactory.getAlarmServiceDAOInstance(
                settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());

        Alarm alarm = new Alarm();
        alarm.setAlarmCode(AlarmCode.CHECKSUM_ALARM);
        alarm.setAlarmRaiser("TEST");
        alarm.setFileID(fileID);
        alarm.setOrigDateTime(CalendarUtils.getEpoch());

        StringBuilder text = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            text.append(settings.getRepositorySettings().toString());
            text.append("\n");
            text.append(settings.getReferenceSettings().toString());
            text.append("\n");
        }
        alarm.setAlarmText(text.toString());

        addStep("Insert the data into the database", "Should be extractable again.");
        database.addAlarm(alarm);

        List<Alarm> extractedAlarms = database.extractAlarms(null, null, (Instant) null,
                (Instant) null, null, null, null, true);
        Assertions.assertEquals(1, extractedAlarms.size());
        Assertions.assertEquals(alarm, extractedAlarms.get(0));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag(TestGroups.DATABASETEST)
    void alarmDatabaseCorrectTimestampTest() {
        addDescription("Testing the correct ingest and extraction of alarm dates");
        AlarmDAOFactory alarmDAOFactory = new AlarmDAOFactory();
        AlarmServiceDAO database = alarmDAOFactory.getAlarmServiceDAOInstance(
                settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());

        addStep("Prepare, check and ingest alarms", "");

        OffsetDateTime summertimeODT = OffsetDateTime.parse("2015-10-25T02:59:54.000+02:00");
        Instant summertimeTS = summertimeODT.toInstant();
        Instant summertimeUnix = Instant.ofEpochMilli(1445734794000L);
        Assertions.assertEquals(summertimeUnix, summertimeTS);

        OffsetDateTime wintertimeZDT = OffsetDateTime.parse("2015-10-25T02:59:54.000+01:00");
        Instant wintertimeTS = wintertimeZDT.toInstant();
        Instant wintertimeUnix = Instant.ofEpochMilli(1445738394000L);
        Assertions.assertEquals(wintertimeUnix, wintertimeTS);

        Alarm summertimeAlarm = new Alarm();
        summertimeAlarm.setAlarmCode(AlarmCode.CHECKSUM_ALARM);
        summertimeAlarm.setAlarmRaiser("TEST");
        summertimeAlarm.setFileID("summertime");
        summertimeAlarm.setAlarmText("Date summertime test alarm");
        summertimeAlarm.setOrigDateTime(CalendarUtils.getXmlGregorianCalendar(summertimeTS));

        Alarm wintertimeAlarm = new Alarm();
        wintertimeAlarm.setAlarmCode(AlarmCode.CHECKSUM_ALARM);
        wintertimeAlarm.setAlarmRaiser("TEST");
        wintertimeAlarm.setFileID("wintertime");
        wintertimeAlarm.setAlarmText("Date wintertime test alarm");
        wintertimeAlarm.setOrigDateTime(CalendarUtils.getXmlGregorianCalendar(wintertimeTS));

        database.addAlarm(summertimeAlarm);
        database.addAlarm(wintertimeAlarm);

        addStep("Extract and check alarms", "");
        List<Alarm> summertimeAlarms = database.extractAlarms(null, null, (Instant) null,
                (Instant) null, "summertime", null, null, true);
        Assertions.assertEquals(1, summertimeAlarms.size());
        Assertions.assertEquals(summertimeUnix, CalendarUtils.convertFromXMLGregorianCalendarToInstant(summertimeAlarms.get(0).getOrigDateTime()));

        List<Alarm> wintertimeAlarms = database.extractAlarms(null, null, (Instant) null,
                (Instant) null, "wintertime", null, null, true);
        Assertions.assertEquals(1, wintertimeAlarms.size());
        Assertions.assertEquals(wintertimeUnix,
                CalendarUtils.convertFromXMLGregorianCalendarToInstant(wintertimeAlarms.get(0).getOrigDateTime()));

    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag(TestGroups.DATABASETEST)
    void concurrentAlarmsFromNewComponentAreAllStoredTest() throws InterruptedException {
        addDescription("Testing that concurrent alarms from a not-before-seen component are all stored. "
                + "Regression test for a race condition where two concurrent inserts of the same new component "
                + "into the component table could cause one of them to fail with a unique constraint violation, "
                + "losing an alarm.");

        AlarmDAOFactory alarmDAOFactory = new AlarmDAOFactory();
        AlarmServiceDAO database = alarmDAOFactory.getAlarmServiceDAOInstance(
                settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());

        String raceComponent = "RACE-COMPONENT-" + Instant.now().toEpochMilli();
        int concurrentAlarms = 20;

        addStep("Fire " + concurrentAlarms + " alarms from the same not-before-seen component at the same time.",
                "None of the insertions should fail.");
        ExecutorService executor = Executors.newFixedThreadPool(concurrentAlarms);
        CountDownLatch readyLatch = new CountDownLatch(concurrentAlarms);
        CountDownLatch startLatch = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < concurrentAlarms; i++) {
            int index = i;
            futures.add(executor.submit(() -> {
                Alarm alarm = new Alarm();
                alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
                alarm.setAlarmRaiser(raceComponent);
                alarm.setAlarmText("Concurrent alarm #" + index);
                alarm.setOrigDateTime(CalendarUtils.getNow());
                readyLatch.countDown();
                startLatch.await();
                database.addAlarm(alarm);
                return null;
            }));
        }
        readyLatch.await();
        startLatch.countDown();

        addStep("Wait for all insertions to complete.", "None should throw an exception.");
        List<Exception> failures = new ArrayList<>();
        for (Future<?> future : futures) {
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                failures.add(e);
            }
        }
        executor.shutdown();
        Assertions.assertTrue(failures.isEmpty(), "Concurrent alarm ingestion threw: " + failures);

        addStep("Verify every alarm was actually stored, and the component was only inserted once.",
                "Should find all alarms and exactly one component row.");
        List<Alarm> storedAlarms = database.extractAlarms(raceComponent, null, (Instant) null,
                (Instant) null, null, null, null, false);
        Assertions.assertEquals(concurrentAlarms, storedAlarms.size());

        DBConnector connector = new DBConnector(settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());
        Long componentCount = DatabaseUtils.selectLongValue(connector,
                "SELECT COUNT(*) FROM " + AlarmDatabaseConstants.COMPONENT_TABLE + " WHERE "
                        + AlarmDatabaseConstants.COMPONENT_ID + " = ?", raceComponent);
        Assertions.assertEquals(1L, componentCount);
    }

    private List<Alarm> makeAlarms() {
        List<Alarm> res = new ArrayList<>();

        Alarm alarm1 = new Alarm();
        alarm1.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
        alarm1.setAlarmRaiser(component1);
        alarm1.setAlarmText("The first alarm: Component failure at epoch.");
        alarm1.setFileID(null);
        alarm1.setOrigDateTime(CalendarUtils.getEpoch());
        alarm1.setCollectionID(collection1);
        res.add(alarm1);

        Alarm alarm2 = new Alarm();
        alarm2.setAlarmCode(AlarmCode.CHECKSUM_ALARM);
        alarm2.setAlarmRaiser(component2);
        alarm2.setAlarmText("The second alarm: Current checksum alarm.");
        alarm2.setFileID(fileID);
        alarm2.setOrigDateTime(CalendarUtils.getNow());
        alarm2.setCollectionID(collection2);
        res.add(alarm2);

        return res;
    }
}
