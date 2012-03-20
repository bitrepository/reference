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
import org.bitrepository.integrityservice.IntegrityServiceComponentFactory;
import org.bitrepository.integrityservice.cache.IntegrityDatabase;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.SimpleIntegrityChecker;
import org.bitrepository.integrityservice.collector.DelegatingIntegrityInformationCollector;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.TimerWorkflowScheduler;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Simple test case for the component factory.
 */
public class ComponentFactoryTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    MessageBus messageBus;
    SecurityManager securityManager;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
        securityManager = new DummySecurityManager();
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager);
    }
    
    @Test(groups = {"regressiontest"})
    public void verifyCacheFromFactory() throws Exception {
        Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings)
//                instanceof MemoryBasedIntegrityCache,
                instanceof IntegrityDatabase,
                "The default Cache should be the '" + IntegrityDatabase.class.getName() + "' but was '"
                + IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings).getClass().getName() + "'");
    }

    @Test(groups = {"regressiontest"})
    public void verifyCollectorFromFactory() throws Exception {
        IntegrityModel cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings);
        IntegrityInformationCollector collector = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                cache, 
                IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings, cache), 
                AccessComponentFactory.getInstance().createGetFileIDsClient(settings, securityManager),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings, securityManager),
                settings, messageBus);
        Assert.assertTrue(collector instanceof DelegatingIntegrityInformationCollector, 
                "The default Collector should be the '" + DelegatingIntegrityInformationCollector.class.getName() + "'");
    }

    @Test(groups = {"regressiontest"})
    public void verifyIntegrityCheckerFromFactory() throws Exception {
        IntegrityModel cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings);
        Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings, cache)
                instanceof SimpleIntegrityChecker, 
                "The default IntegrityChecker should be the '" + SimpleIntegrityChecker.class.getName() + "'");
    }

    @Test(groups = {"regressiontest"})
    public void verifySchedulerFromFactory() throws Exception {
        Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settings)
                instanceof TimerWorkflowScheduler, 
                "The default Scheduler should be the '" + TimerWorkflowScheduler.class.getName() + "'");
    }

}
