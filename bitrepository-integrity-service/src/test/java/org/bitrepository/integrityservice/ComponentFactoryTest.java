/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlarmDispatcher;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.audittrail.IntegrityAuditTrailDatabaseCreator;
import org.bitrepository.integrityservice.cache.IntegrityCache;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDatabaseCreator;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.checking.SimpleIntegrityChecker;
import org.bitrepository.integrityservice.collector.DelegatingIntegrityInformationCollector;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.service.scheduler.ServiceScheduler;
import org.bitrepository.service.scheduler.TimerbasedScheduler;
import org.bitrepository.settings.referencesettings.AlarmLevel;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.bitrepository.settings.repositorysettings.PillarIDs;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Simple test case for the component factory.
 */
public class ComponentFactoryTest extends IntegrationTest {

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifySchedulerFromFactory() throws Exception {
        addDescription("Test the instantiation of the Scheduler from the component factory.");
        ServiceScheduler scheduler = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settingsForCUT);
        Assert.assertNotNull(scheduler);
        Assert.assertTrue(scheduler instanceof TimerbasedScheduler);
        Assert.assertEquals(scheduler, IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settingsForCUT));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyCollectorFromFactory() throws Exception {
        addDescription("Test the instantiation of the Collector from the component factory.");
        IntegrityInformationCollector collector =
                IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                        "dummy-collection",
                AccessComponentFactory.getInstance().createGetFileIDsClient(settingsForCUT,
                        securityManager,
                        settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getID()),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settingsForCUT, securityManager,
                        settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getID()),
                new MockAuditManager());
        Assert.assertNotNull(collector);
        Assert.assertTrue(collector instanceof DelegatingIntegrityInformationCollector);
        Assert.assertEquals(collector, IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                "dummy-collection",
                AccessComponentFactory.getInstance().createGetFileIDsClient(settingsForCUT, securityManager,
                        settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getID()),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settingsForCUT, securityManager,
                        settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getID()),
                new MockAuditManager()));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyIntegrityCheckerFromFactory() throws Exception {
        addDescription("Test the instantiation of the IntegrityChecker from the component factory.");
        IntegrityAlerter alarmDispatcher = new IntegrityAlarmDispatcher(settingsForCUT, messageBus, AlarmLevel.ERROR);
        IntegrityChecker checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settingsForCUT,
                IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settingsForCUT, 
                        alarmDispatcher), new MockAuditManager());
        Assert.assertNotNull(checker);
        Assert.assertTrue(checker instanceof SimpleIntegrityChecker);
        Assert.assertEquals(checker, IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settingsForCUT,
                IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settingsForCUT,
                        alarmDispatcher), new MockAuditManager()));
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyCacheFromFactory() throws Exception {
        addDescription("Test the instantiation of the IntegrityModel from the component factory.");
        IntegrityAlerter alarmDispatcher = new IntegrityAlarmDispatcher(settingsForCUT, messageBus, AlarmLevel.ERROR);
        IntegrityModel integrityModel = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(
                settingsForCUT, alarmDispatcher);
        Assert.assertNotNull(integrityModel);
        Assert.assertTrue(integrityModel instanceof IntegrityCache);
        Assert.assertEquals(integrityModel,
                IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(
                        settingsForCUT,alarmDispatcher));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyAlarmsFromCacheWhenInstantiationErrors() throws Exception {
        addDescription("Test that an alarm is send, if the the instantiation of the IntegrityModel from the component factory fails.");
        addStep("Instantiate the database with the correct list of pillars", "Is created.");
        cleanIntegrityDatabase();
        try {
            IntegrityDAO dao = new IntegrityDAO(new DBConnector(
                    settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase()),
                    settingsForCUT.getRepositorySettings().getCollections());
            
            addStep("Start the database through the component factory with different (wrong) set of pillars.", 
                    "Through an exception and sends an alarm.");

            PillarIDs pillars = 
                    settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs();
            pillars.getPillarID().add("Wrong pillar " + new Date().getTime());
            pillars.getPillarID().add("Another wrong pillar " + new Date().getTime());
            settingsForCUT.getRepositorySettings().getCollections().getCollection().get(0).setPillarIDs(pillars);
            
            IntegrityAlerter alarmDispatcher = new IntegrityAlarmDispatcher(settingsForCUT, messageBus, AlarmLevel.ERROR);
            try {
                IntegrityModel integrityModel = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(
                        settingsForCUT, alarmDispatcher);
            } catch (Exception e) {
                // expected
            }
            AlarmMessage alarm = alarmReceiver.waitForMessage(AlarmMessage.class);
            Assert.assertNotNull(alarm);
            Assert.assertEquals(alarm.getAlarm().getAlarmCode(), AlarmCode.FAILED_OPERATION);
            Assert.assertEquals(alarm.getFrom(), settingsForCUT.getComponentID());
        } finally {
            cleanIntegrityDatabase();
        }
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyServiceFromFactory() throws Exception {
        addDescription("Test the instantiation of the IntegrityService from the component factory.");
        instantiateAuditContributorDatabase();
        IntegrityService service = IntegrityServiceComponentFactory.getInstance().createIntegrityService(settingsForCUT, securityManager);
        Assert.assertNotNull(service);
        Assert.assertTrue(service instanceof SimpleIntegrityService);
    }
    
    private void instantiateAuditContributorDatabase() throws Exception {
        DatabaseSpecifics auditTrailDB =
                settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getAuditTrailContributerDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(auditTrailDB);
        IntegrityAuditTrailDatabaseCreator pillarAuditTrailDatabaseCreator = new IntegrityAuditTrailDatabaseCreator();
        pillarAuditTrailDatabaseCreator.createIntegrityAuditTrailDatabase(settingsForCUT, null);
    }
    
    private void cleanIntegrityDatabase() throws Exception {
        DerbyDatabaseDestroyer.deleteDatabase(settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        IntegrityDatabaseCreator integrityDatabaseCreator = new IntegrityDatabaseCreator();        
        integrityDatabaseCreator.createIntegrityDatabase(settingsForCUT, null);
    }
}
