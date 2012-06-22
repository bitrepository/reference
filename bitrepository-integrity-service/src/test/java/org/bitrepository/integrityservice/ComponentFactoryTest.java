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
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.DatabaseTestUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.integrityservice.cache.IntegrityDatabase;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.SimpleIntegrityChecker;
import org.bitrepository.integrityservice.collector.DelegatingIntegrityInformationCollector;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.mocks.MockAuditManager;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.scheduler.TimerbasedScheduler;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.sql.Connection;

/**
 * Simple test case for the component factory.
 */
public class ComponentFactoryTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    MessageBus messageBus;
    SecurityManager securityManager;
    String DATABASE_NAME = "integritydb";
    String DATABASE_DIRECTORY = "test-data";
    String DATABASE_URL = "jdbc:derby:" + DATABASE_DIRECTORY + "/" + DATABASE_NAME;
    File dbDir = null;

    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("ComponentFactoryUnderTest");
        securityManager = new DummySecurityManager();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
        settings.getReferenceSettings().getIntegrityServiceSettings().setIntegrityDatabaseUrl(DATABASE_URL);
        
        File dbFile = new File("src/test/resources/integritydb.jar");
        Assert.assertTrue(dbFile.isFile(), "The database file should exist");
        
        dbDir = FileUtils.retrieveDirectory(DATABASE_DIRECTORY);
        FileUtils.retrieveSubDirectory(dbDir, DATABASE_NAME);
        
        Connection dbCon = DatabaseTestUtils.takeDatabase(dbFile, DATABASE_NAME, dbDir);
        dbCon.close();
    }
    
    @AfterClass (alwaysRun = true)
    public void shutdown() throws Exception {
        if(dbDir != null) {
            FileUtils.delete(dbDir);
        }
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyCacheFromFactory() throws Exception {
        Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings)
                instanceof IntegrityDatabase,
                "The default Cache should be the '" + IntegrityDatabase.class.getName() + "' but was '"
                + IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings).getClass().getName() + "'");
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyCollectorFromFactory() throws Exception {
        IntegrityInformationCollector collector = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                AccessComponentFactory.getInstance().createGetFileIDsClient(settings, securityManager, 
                        settings.getReferenceSettings().getIntegrityServiceSettings().getID()),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings, securityManager, 
                        settings.getReferenceSettings().getIntegrityServiceSettings().getID()),
                new MockAuditManager());
        Assert.assertTrue(collector instanceof DelegatingIntegrityInformationCollector, 
                "The default Collector should be the '" + DelegatingIntegrityInformationCollector.class.getName() + "'");
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifyIntegrityCheckerFromFactory() throws Exception {
        IntegrityModel cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings);
        Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings, cache, new MockAuditManager())
                instanceof SimpleIntegrityChecker, 
                "The default IntegrityChecker should be the '" + SimpleIntegrityChecker.class.getName() + "'");
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void verifySchedulerFromFactory() throws Exception {
        Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settings)
                instanceof TimerbasedScheduler, 
                "The default Scheduler should be the '" + TimerbasedScheduler.class.getName() + "'");
    }

}
