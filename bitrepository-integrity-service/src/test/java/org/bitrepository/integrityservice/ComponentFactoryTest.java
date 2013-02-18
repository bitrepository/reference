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

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.integrityservice.audittrail.IntegrityAuditTrailDatabaseCreator;
import org.bitrepository.integrityservice.cache.IntegrityDatabase;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.checking.SimpleIntegrityChecker;
import org.bitrepository.integrityservice.collector.DelegatingIntegrityInformationCollector;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.service.scheduler.ServiceScheduler;
import org.bitrepository.service.scheduler.TimerbasedScheduler;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
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
                AccessComponentFactory.getInstance().createGetFileIDsClient(settingsForCUT, securityManager,
                        settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getID()),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settingsForCUT, securityManager,
                        settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getID()),
                new MockAuditManager(), settingsForCUT.getCollectionID());
        Assert.assertNotNull(collector);
        Assert.assertTrue(collector instanceof DelegatingIntegrityInformationCollector);
        Assert.assertEquals(collector, IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                AccessComponentFactory.getInstance().createGetFileIDsClient(settingsForCUT, securityManager,
                        settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getID()),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settingsForCUT, securityManager,
                        settingsForCUT.getReferenceSettings().getIntegrityServiceSettings().getID()),
                new MockAuditManager(), settingsForCUT.getCollectionID()));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyIntegrityCheckerFromFactory() throws Exception {
        addDescription("Test the instantiation of the IntegrityChecker from the component factory.");
        IntegrityChecker checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settingsForCUT,
                IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settingsForCUT),
                new MockAuditManager());
        Assert.assertNotNull(checker);
        Assert.assertTrue(checker instanceof SimpleIntegrityChecker);
        Assert.assertEquals(checker, IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settingsForCUT,
                IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settingsForCUT),
                new MockAuditManager()));
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyCacheFromFactory() throws Exception {
        addDescription("Test the instantiation of the IntegrityModel from the component factory.");
        IntegrityModel integrityModel = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settingsForCUT);
        Assert.assertNotNull(integrityModel);
        Assert.assertTrue(integrityModel instanceof IntegrityDatabase);
        Assert.assertEquals(integrityModel, IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settingsForCUT));
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
}
