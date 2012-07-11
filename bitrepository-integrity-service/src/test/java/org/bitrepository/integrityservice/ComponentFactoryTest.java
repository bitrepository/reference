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

import java.io.File;
import java.sql.Connection;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.common.utils.DatabaseTestUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.integrityservice.cache.IntegrityDatabase;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.checking.SimpleIntegrityChecker;
import org.bitrepository.integrityservice.collector.DelegatingIntegrityInformationCollector;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.scheduler.ServiceScheduler;
import org.bitrepository.service.scheduler.TimerbasedScheduler;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Simple test case for the component factory.
 */
public class ComponentFactoryTest extends IntegrityDatabaseTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    MessageBus messageBus;
    SecurityManager securityManager;
    
    File auditDir = null;

    @BeforeMethod (alwaysRun = true)
    @Override
    public void setup() throws Exception {
        super.setup();
        securityManager = new DummySecurityManager();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
    }
    
    @AfterClass (alwaysRun = true)
    @Override
    public void cleanup() throws Exception {
        super.cleanup();
        if(auditDir != null) {
            FileUtils.delete(auditDir);
        }
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void verifySchedulerFromFactory() throws Exception {
        addDescription("Test the instantiation of the Scheduler from the component factory.");
        ServiceScheduler scheduler = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settings);
        Assert.assertNotNull(scheduler);
        Assert.assertTrue(scheduler instanceof TimerbasedScheduler);
        Assert.assertEquals(scheduler, IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settings));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyCollectorFromFactory() throws Exception {
        addDescription("Test the instantiation of the Collector from the component factory.");
        IntegrityInformationCollector collector = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                AccessComponentFactory.getInstance().createGetFileIDsClient(settings, securityManager, 
                        settings.getReferenceSettings().getIntegrityServiceSettings().getID()),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings, securityManager, 
                        settings.getReferenceSettings().getIntegrityServiceSettings().getID()),
                new MockAuditManager());
        Assert.assertNotNull(collector);
        Assert.assertTrue(collector instanceof DelegatingIntegrityInformationCollector);
        Assert.assertEquals(collector, IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                AccessComponentFactory.getInstance().createGetFileIDsClient(settings, securityManager, 
                        settings.getReferenceSettings().getIntegrityServiceSettings().getID()),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings, securityManager, 
                        settings.getReferenceSettings().getIntegrityServiceSettings().getID()),
                new MockAuditManager()));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyIntegrityCheckerFromFactory() throws Exception {
        addDescription("Test the instantiation of the IntegrityChecker from the component factory.");
        IntegrityChecker checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings, 
                IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings), 
                new MockAuditManager());
        Assert.assertNotNull(checker);
        Assert.assertTrue(checker instanceof SimpleIntegrityChecker);
        Assert.assertEquals(checker, IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings, 
                IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings), 
                new MockAuditManager()));
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyCacheFromFactory() throws Exception {
        addDescription("Test the instantiation of the IntegrityModel from the component factory.");
        IntegrityModel integrityModel = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings);
        Assert.assertNotNull(integrityModel);
        Assert.assertTrue(integrityModel instanceof IntegrityDatabase);
        Assert.assertEquals(integrityModel, IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyServiceFromFactory() throws Exception {
        addDescription("Test the instantiation of the IntegrityService from the component factory.");
        instantiateAuditContributorDatabase();
        IntegrityService service = IntegrityServiceComponentFactory.getInstance().createIntegrityService(settings, securityManager);
        Assert.assertNotNull(service);
        Assert.assertTrue(service instanceof SimpleIntegrityService);
    }
    
    private void instantiateAuditContributorDatabase() throws Exception {
        String DATABASE_NAME = "auditcontributerdb";
        String DATABASE_DIRECTORY = "test-data";
        String DATABASE_URL = "jdbc:derby:" + DATABASE_DIRECTORY + "/" + DATABASE_NAME;
        settings.getReferenceSettings().getIntegrityServiceSettings().setAuditContributerDatabaseUrl(DATABASE_URL);
        
        addStep("Initialise the database", "Should be unpacked from a jar-file.");
        File dbFile = new File("../bitrepository-core/src/test/resources/auditcontributerdb.jar");
        Assert.assertTrue(dbFile.isFile(), "The database file should exist");
        
        auditDir = FileUtils.retrieveDirectory(DATABASE_DIRECTORY);
        FileUtils.retrieveSubDirectory(auditDir, DATABASE_NAME);
        
        Connection dbCon = DatabaseTestUtils.takeDatabase(dbFile, DATABASE_NAME, auditDir);
        dbCon.close();
    }
}
