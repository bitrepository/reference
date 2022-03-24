/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails.store;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AuditDatabaseTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    String fileID = "TEST-FILE-ID-" + new Date().getTime();
    String fileID2 = "ANOTHER-FILE-ID" + new Date().getTime();
    String pillarID = "MY-TEST-PILLAR";
    String actor1 = "ACTOR-1";
    String actor2 = "ACTOR-2";
    String collectionID;
    static final String fingerprint1 = "abab";
    static final String operationID1 = "1234";
    static final String fingerprint2 = "baba";
    static final String operationID2 = "4321";

    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("AuditDatabaseUnderTest");
        DerbyDatabaseDestroyer.deleteDatabase(
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase());

        AuditTrailDatabaseCreator auditTrailDatabaseCreator = new AuditTrailDatabaseCreator();
        auditTrailDatabaseCreator.createAuditTrailDatabase(settings, null);
        
        collectionID = settings.getCollections().get(0).getID();
    }
    
    @Test(groups = {"regressiontest", "databasetest"})
    public void AuditDatabaseExtractionTest() throws Exception {
        addDescription("Testing the connection to the audit trail service database especially with regards to "
                + "extracting the data from it.");
        addStep("Setup the variables and constants.", "Should be ok.");
        Date restrictionDate = new Date(123456789); // Sometime between epoch and now!
        
        addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
        DatabaseManager dm = new AuditTrailDatabaseManager(
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase());
        AuditTrailServiceDAO database = new AuditTrailServiceDAO(dm);

        addStep("Validate that the database is empty and then populate it.", "Should be possible.");
        Assert.assertEquals(database.largestSequenceNumber(pillarID, collectionID), 0);
        database.addAuditTrails(createEvents(), collectionID, pillarID);
        Assert.assertEquals(database.largestSequenceNumber(pillarID, collectionID), 10);
        
        addStep("Extract the audit trails", "");
        List<AuditTrailEvent> res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, 
                null, null, null, null, null, null, null));
        Assert.assertEquals(res.size(), 2, res.toString());
        
        addStep("Test the extraction of FileID", "Should be able to extract the audit of each file individually.");
        res = getEventsFromIterator(database.getAuditTrailsByIterator(fileID, null, null, null, null, null, null,
                null, null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileID);
        
        res = getEventsFromIterator(database.getAuditTrailsByIterator(fileID2, null, null, null, null, null, null,
                null, null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileID2);
        
        addStep("Test the extraction of CollectionID", "Only results when the defined collection is used");
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, collectionID, null, null, null, null, null,
                null, null, null, null));
        Assert.assertEquals(res.size(), 2, res.toString());
        
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, 
                "NOT-THE-CORRECT-COLLECTION-ID" + System.currentTimeMillis(), null, null, null, null, null, 
                null, null, null, null));
        Assert.assertEquals(res.size(), 0, res.toString());
        
        addStep("Perform extraction based on the component id.", "");
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, pillarID, null, null, null, null,
                null, null, null, null));
        Assert.assertEquals(res.size(), 2, res.toString());
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, "NO COMPONENT", null, null, null, null, 
                null, null, null, null));
        Assert.assertEquals(res.size(), 0, res.toString());
        
        addStep("Perform extraction based on the sequence number restriction", 
                "Should be possible to have both lower and upper sequence number restrictions.");
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, 5L, null, null, null, null, 
                null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileID2);
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, 5L, null, null, null, 
                null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileID);
        
        addStep("Perform extraction based on actor id restriction.", 
                "Should be possible to restrict on the id of the actor.");
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, null, actor1, null, 
                null, null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getActorOnFile(), actor1);
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, null, actor2, null, 
                null, null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getActorOnFile(), actor2);
        
        addStep("Perform extraction based on operation restriction.", 
                "Should be possible to restrict on the FileAction operation.");
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, null, null, 
                FileAction.INCONSISTENCY, null, null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getActionOnFile(), FileAction.INCONSISTENCY);
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, null, null, 
                FileAction.FAILURE, null, null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getActionOnFile(), FileAction.FAILURE);
        
        addStep("Perform extraction based on date restriction.", 
                "Should be possible to restrict on the date of the audit.");
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, null, null, null, 
                restrictionDate, null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileID2);
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, null, null, null, 
                null, restrictionDate, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileID);

        addStep("Perform extraction based on fingerprint restriction.", 
                "Should be possible to restrict on the fingerprint of the audit.");
        res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, null, null, null, 
                null, null, fingerprint1, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileID);
        Assert.assertEquals(res.get(0).getCertificateID(), fingerprint1);
        
        addStep("Perform extraction based on operationID restriction.", 
                "Should be possible to restrict on the operationID of the audit.");
                res = getEventsFromIterator(database.getAuditTrailsByIterator(null, null, null, null, null, null, null, 
                null, null, null, operationID2));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(res.get(0).getFileID(), fileID2);
        Assert.assertEquals(res.get(0).getOperationID(), operationID2);
        
        database.close();
    }

    @Test(groups = {"regressiontest", "databasetest"})
    public void AuditDatabasePreservationTest() throws Exception {
        addDescription("Tests the functions related to the preservation of the database.");
        addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
        DatabaseManager dm = new AuditTrailDatabaseManager(
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase());
        AuditTrailServiceDAO database = new AuditTrailServiceDAO(dm);

        Assert.assertEquals(database.largestSequenceNumber(pillarID, collectionID), 0);
        database.addAuditTrails(createEvents(), collectionID, pillarID);
        Assert.assertEquals(database.largestSequenceNumber(pillarID, collectionID), 10);

        addStep("Validate the preservation sequence number", 
                "Should be zero, since it has not been updated yet.");
        long pindex = database.getPreservationSequenceNumber(pillarID, collectionID);
        Assert.assertEquals(pindex, 0);

        addStep("Validate the insertion of the preservation sequence number",
                "Should be the same value extracted afterwards.");
        long givenPreservationIndex = 123456789;
        database.setPreservationSequenceNumber(pillarID, collectionID, givenPreservationIndex);
        Assert.assertEquals(database.getPreservationSequenceNumber(pillarID, collectionID), givenPreservationIndex);

        database.close();
    }
    
    @Test(groups = {"regressiontest", "databasetest"})
    public void auditDatabaseCorrectTimestampTest() throws ParseException  {
        addDescription("Testing the correct ingest and extraction of audittrail dates");
        DatabaseManager dm = new AuditTrailDatabaseManager(
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase());
        AuditTrailServiceDAO database = new AuditTrailServiceDAO(dm);
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);
        Date summertimeTS = sdf.parse("2015-10-25T02:59:54.000+02:00");
        Date summertimeUnix = new Date(1445734794000L);
        Assert.assertEquals(summertimeTS, summertimeUnix);
        
        Date wintertimeTS = sdf.parse("2015-10-25T02:59:54.000+01:00");
        Date wintertimeUnix = new Date(1445738394000L);
        Assert.assertEquals(wintertimeTS, wintertimeUnix);
        
        AuditTrailEvents events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getXmlGregorianCalendar(summertimeTS), 
                FileAction.CHECKSUM_CALCULATED, "actor", "auditInfo", "summertime", "info", pillarID, new BigInteger("1"), 
                operationID1, fingerprint1));
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getXmlGregorianCalendar(wintertimeTS), 
                FileAction.CHECKSUM_CALCULATED, "actor", "auditInfo", "wintertime", "info", pillarID, new BigInteger("1"), 
                operationID1, fingerprint1));
        database.addAuditTrails(events, collectionID, pillarID);
        
        List<AuditTrailEvent> res = getEventsFromIterator(database.getAuditTrailsByIterator("summertime", null, null, null, 
                null, null, null, null, null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(
                CalendarUtils.convertFromXMLGregorianCalendar(res.get(0).getActionDateTime()), summertimeUnix);
        
        res = getEventsFromIterator(database.getAuditTrailsByIterator("wintertime", null, null, null, null, null, null, 
                null, null, null, null));
        Assert.assertEquals(res.size(), 1, res.toString());
        Assert.assertEquals(
                CalendarUtils.convertFromXMLGregorianCalendar(res.get(0).getActionDateTime()), wintertimeUnix);

    }
    
    @Test(groups = {"regressiontest", "databasetest"})
    public void AuditDatabaseIngestTest() throws Exception {
        addDescription("Testing ingest of audittrails into the database");
        addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
        DatabaseManager dm = new AuditTrailDatabaseManager(
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase());
        AuditTrailServiceDAO database = new AuditTrailServiceDAO(dm);
        AuditTrailEvents events;
        String veryLongString = "";
        for(int i = 0; i < 255; i++) {
            veryLongString += i;
        }
        
        addStep("Test ingesting with all data", "No failure");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", "auditInfo", "fileID", "info", pillarID, new BigInteger("1"), operationID1, fingerprint1));
        database.addAuditTrails(events, collectionID, pillarID);
        
        
        addStep("Test ingesting with no timestamp", "No failure");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(null, FileAction.CHECKSUM_CALCULATED, 
                "actor", "auditInfo", "fileID", "info", pillarID, new BigInteger("2"), operationID1, fingerprint1));
        try {
            database.addAuditTrails(events, collectionID, pillarID);
            Assert.fail("Should throw an exception.");
        } catch  (IllegalArgumentException e) {
            // expected
        }
        
        addStep("Test ingesting with no file action", "No failure");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), null, 
                "actor", "auditInfo", "fileID", "info", pillarID, new BigInteger("3"), operationID1, fingerprint1));
        try {
            database.addAuditTrails(events, collectionID, pillarID);
            Assert.fail("Should throw an exception.");
        } catch (IllegalStateException e) {
            //expected
        }
               
        addStep("Test ingesting with no actor", "Throws exception");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                null, "auditInfo", "fileID", "info", pillarID, new BigInteger("4"), operationID1, fingerprint1));
        try {
            database.addAuditTrails(events, collectionID, pillarID);
            Assert.fail("Should throw an exception.");
        } catch (IllegalStateException e) {
            // expected
        }

        addStep("Test ingesting with no audit info", "No failure");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", null, "fileID", "info", pillarID, new BigInteger("5"), operationID1, fingerprint1));
        database.addAuditTrails(events, collectionID, pillarID);

        addStep("Test ingesting with no file id", "Throws exception");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", "auditInfo", null, "info", pillarID, new BigInteger("6"), operationID1, fingerprint1));
        try {
            database.addAuditTrails(events, collectionID, pillarID);
            Assert.fail("Should throw an exception.");
        } catch (IllegalStateException e) {
            // expected
        } 

        addStep("Test ingesting with no info", "No failure");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", "auditInfo", "fileID", null, pillarID, new BigInteger("7"), operationID1, fingerprint1));
        database.addAuditTrails(events, collectionID, pillarID);

        addStep("Test ingesting with no component id", "Throws exception");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", "auditInfo", "fileID", "info", null, new BigInteger("8"), operationID1, fingerprint1));
        try {
            database.addAuditTrails(events, collectionID, pillarID);
            Assert.fail("Should throw an exception.");
        } catch (IllegalStateException e) {
            // expected
        }
        
        
        // broken (null-pointer)
        addStep("Test ingesting with no sequence number", "Throws exception");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", "auditInfo", "fileID", "info", pillarID, null, operationID1, fingerprint1));
        try {
            database.addAuditTrails(events, collectionID, pillarID);
            Assert.fail("Should throw an exception.");
        } catch (IllegalStateException e) {
            // expected
        }
        
        addStep("Test ingest with very long auditInfo (255+)", "Not failing any more");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", veryLongString, "fileID", "info", pillarID, new BigInteger("9"), operationID1, fingerprint1));
        database.addAuditTrails(events, collectionID, pillarID);
        
        addStep("Test ingest with very long info (255+)", "Not failing any more");
        events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", "auditInfo", "fileID", veryLongString, pillarID, new BigInteger("10"), operationID1, fingerprint1));
        database.addAuditTrails(events, collectionID, pillarID);
    }
    
    
    @Test(groups = {"regressiontest", "databasetest"})
    public void AuditDatabaseGoodIngestTest() throws Exception {
        addDescription("Testing good case ingest of audittrails into the database");
        addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
        DatabaseManager dm = new AuditTrailDatabaseManager(
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase());
        AuditTrailServiceDAO database = new AuditTrailServiceDAO(dm);
        
        addStep("Build test data", "No failure");
        AuditTrailEvents events = new AuditTrailEvents();
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.PUT_FILE, 
                "actor", "auditInfo", "fileID", "info", pillarID, new BigInteger("1"), operationID1, fingerprint1));
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", "auditInfo", "fileID", "info", pillarID, new BigInteger("2"), operationID1, null));
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.REPLACE_FILE, 
                "actor", "auditInfo", "fileID", "info", pillarID, new BigInteger("3"), null, fingerprint1));
        events.getAuditTrailEvent().add(createSingleEvent(CalendarUtils.getNow(), FileAction.CHECKSUM_CALCULATED, 
                "actor", "auditInfo", "fileID", "info", pillarID, new BigInteger("4"), null, null));
        
        database.addAuditTrails(events, collectionID, pillarID);
        
        
        
    }
    
    private AuditTrailEvents createEvents() {
        AuditTrailEvents events = new AuditTrailEvents();
        
        AuditTrailEvent event1 = createSingleEvent(CalendarUtils.getEpoch(), FileAction.INCONSISTENCY, actor1, 
                "I AM AUDIT", fileID, null, pillarID, new BigInteger("1"), operationID1, fingerprint1);
        events.getAuditTrailEvent().add(event1);
        
        AuditTrailEvent event2 = createSingleEvent(CalendarUtils.getNow(), FileAction.FAILURE, actor2, null, fileID2,
                "WHAT AM I DOING?", pillarID, new BigInteger("10"), operationID2, fingerprint2);
        events.getAuditTrailEvent().add(event2);

        return events;
    }
    
    private AuditTrailEvent createSingleEvent(XMLGregorianCalendar datetime, FileAction action, String actor, 
            String auditInfo, String fileID, String info, String component, BigInteger seqNumber, String operationID,
            String fingerprint) {
        AuditTrailEvent res = new AuditTrailEvent();
        res.setActionDateTime(datetime);
        res.setActionOnFile(action);
        res.setActorOnFile(actor);
        res.setAuditTrailInformation(auditInfo);
        res.setFileID(fileID);
        res.setInfo(info);
        res.setReportingComponent(component);
        res.setSequenceNumber(seqNumber);
        res.setOperationID(operationID);
        res.setCertificateID(fingerprint);
        return res;
    }
    
    private List<AuditTrailEvent> getEventsFromIterator(AuditEventIterator it) {
        List<AuditTrailEvent> events = new ArrayList<>();
        AuditTrailEvent event;
        while((event = it.getNextAuditTrailEvent()) != null) {
            events.add(event);
        }
        
        return events;
    }
}
