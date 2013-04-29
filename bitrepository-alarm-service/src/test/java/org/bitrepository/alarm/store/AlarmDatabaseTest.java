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
package org.bitrepository.alarm.store;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.bitrepository.alarm.store.AlarmDatabaseConstants.ALARM_TABLE;
import static org.bitrepository.alarm.store.AlarmDatabaseConstants.COMPONENT_TABLE;

public class AlarmDatabaseTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    String fileId = "TEST-FILE-ID-" + new Date().getTime();
    String component1 = "ACTOR-1";
    String component2 = "ACTOR-2";
    String collection1 = "collection1";
    String collection2 = "collection2";
    String DATABASE_NAME = "alarmservicedb";
    String DATABASE_DIRECTORY = "test-data";
    String DATABASE_URL = "jdbc:derby:" + DATABASE_DIRECTORY + "/" + DATABASE_NAME;
    File dbDir = null;

    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("AlarmDatabaseUnderTest");

        DerbyDatabaseDestroyer.deleteDatabase(
                settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());

        AlarmDatabaseCreator integrityDatabaseCreator = new AlarmDatabaseCreator();
        integrityDatabaseCreator.createAlarmDatabase(settings, null);
    }
    
    @AfterMethod (alwaysRun = true)
    public void cleanupDatabase() throws Exception {
        // TODO
        DBConnector connector = new DBConnector(settings.getReferenceSettings().getAlarmServiceSettings().getAlarmServiceDatabase());
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + ALARM_TABLE, new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + COMPONENT_TABLE, new Object[0]);
    }

    @AfterClass (alwaysRun = true)
    public void shutdown() throws Exception {
        addStep("Cleanup after test.", "Should remove directory with test material.");
        if(dbDir != null) {
            FileUtils.delete(dbDir);
        }
    }

    @Test(groups = {"regressiontest", "databasetest"})
    public void AlarmDatabaseExtractionTest() throws Exception {
        addDescription("Testing the connection to the alarm service database especially with regards to "
                + "extracting the data from it.");
        addStep("Setup the variables and constants.", "Should be ok.");
        Date restrictionDate = new Date(123456789); // Sometime between epoch and now!
        
        addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
        AlarmServiceDAO database = new AlarmServiceDAO(settings);
        
        addStep("Populate the database with two alarms.", "Should be inserted.");
        for(Alarm alarm : makeAlarms()) {
            database.addAlarm(alarm);
        }
        
        addStep("Try to extract all the data from the database.", "Should deliver both alarms.");
        List<Alarm> extractedAlarms = database.extractAlarms(null, null, null, null, null, null, null, false);
        Assert.assertEquals(extractedAlarms.size(), 2);
        
        addStep("Try to extract the alarms for component 1.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(component1, null, null, null, null, null, null, false);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.COMPONENT_FAILURE);
        Assert.assertNull(extractedAlarms.get(0).getFileID());
        
        addStep("Try to extract the alarms for component 2.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(component2, null, null, null, null, null, null, false);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component2);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.CHECKSUM_ALARM);
        Assert.assertEquals(extractedAlarms.get(0).getFileID(), fileId);
        
        addStep("Try to extract the alarms for the alarm code 'COMPONENT_FAILURE'.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, AlarmCode.COMPONENT_FAILURE, null, null, null, null, null, false);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.COMPONENT_FAILURE);
        Assert.assertNull(extractedAlarms.get(0).getFileID());
        
        addStep("Try to extract the alarms for the alarm code 'CHECKSUM_ALARM'.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, AlarmCode.CHECKSUM_ALARM, null, null, null, null, null, false);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component2);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.CHECKSUM_ALARM);
        Assert.assertEquals(extractedAlarms.get(0).getFileID(), fileId);
        
        addStep("Try to extract the new alarm.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, restrictionDate, null, null, null, null, false);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component2);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.CHECKSUM_ALARM);
        Assert.assertEquals(extractedAlarms.get(0).getFileID(), fileId);
        
        addStep("Try to extract the old alarm.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, null, restrictionDate, null, null, null, false);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.COMPONENT_FAILURE);
        Assert.assertNull(extractedAlarms.get(0).getFileID());

        addStep("Try to extract the alarms for the file id.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, null, null, fileId, null, null, false);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component2);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.CHECKSUM_ALARM);
        Assert.assertEquals(extractedAlarms.get(0).getFileID(), fileId);
        
        addStep("Try to extract the alarms for the collection id.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, null, null, null, collection1, null, false);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component1);
        Assert.assertEquals(extractedAlarms.get(0).getCollectionID(), collection1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.COMPONENT_FAILURE);
                
        addStep("Try to extract the oldest alarm from the database.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, null, null, null, null, 1, true);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.COMPONENT_FAILURE);
        Assert.assertNull(extractedAlarms.get(0).getFileID());
        
        addStep("Try to extract the newest alarm from the database.", "Should deliver one alarm.");
        extractedAlarms = database.extractAlarms(null, null, null, null, null, null, 1, false);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmRaiser(), component2);
        Assert.assertEquals(extractedAlarms.get(0).getAlarmCode(), AlarmCode.CHECKSUM_ALARM);
        Assert.assertEquals(extractedAlarms.get(0).getFileID(), fileId);
    }

    @Test(groups = {"regressiontest", "databasetest"})
    public void AlarmDatabaseLargeIngestionTest() throws Exception {
        addDescription("Testing the ingestion of a large texts into the database");
        addStep("Setup and create alarm", "");
        AlarmServiceDAO database = new AlarmServiceDAO(settings);
        
        Alarm alarm = new Alarm();
        alarm.setAlarmCode(AlarmCode.CHECKSUM_ALARM);
        alarm.setAlarmRaiser("TEST");
        alarm.setFileID(fileId);
        alarm.setOrigDateTime(CalendarUtils.getEpoch());
        
        StringBuilder text = new StringBuilder();
        for(int i = 0; i < 10; i++) {
            text.append(settings.getRepositorySettings().toString());
            text.append("\n");
            text.append(settings.getReferenceSettings().toString());
            text.append("\n");
        }
        alarm.setAlarmText(text.toString());
        
        addStep("Insert the data into the database", "Should be extractable again.");
        database.addAlarm(alarm);
        
        List<Alarm> extractedAlarms = database.extractAlarms(null, null, null, null, null, null, null, true);
        Assert.assertEquals(extractedAlarms.size(), 1);
        Assert.assertEquals(extractedAlarms.get(0), alarm);
    }

    private List<Alarm> makeAlarms() {
        List<Alarm> res = new ArrayList<Alarm>();
        
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
        alarm2.setFileID(fileId);
        alarm2.setOrigDateTime(CalendarUtils.getNow());
        alarm2.setCollectionID(collection2);
        res.add(alarm2);

        return res;
    }
}
