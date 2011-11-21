package org.bitrepository.integrityclient;

import org.bitrepository.integrityclient.cache.MemoryBasedIntegrityCache;
import org.bitrepository.integrityclient.collector.DelegatingIntegrityInformationCollector;
import org.bitrepository.integrityclient.scheduler.TimerIntegrityInformationScheduler;
import org.bitrepository.protocol.IntegrationTest;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Simple test case for the component factory.
 */
public class ComponentFactoryTester extends IntegrationTest {

    @Test(groups = {"regressiontest"})
    public void verifyCacheFromFactory() throws Exception {
        Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage()
                instanceof MemoryBasedIntegrityCache, 
                "The default Cache should be the '" + MemoryBasedIntegrityCache.class.getName() + "' but was '"
                + IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage().getClass().getName() + "'");
    }

    @Test(groups = {"regressiontest"})
    public void verifyCollectorFromFactory() throws Exception {
        Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(messageBus, settings)
                instanceof DelegatingIntegrityInformationCollector, 
                "The default Collector should be the '" + DelegatingIntegrityInformationCollector.class.getName() + "'");
    }

    @Test(groups = {"regressiontest"})
    public void verifySchedulerFromFactory() throws Exception {
        Assert.assertTrue(IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settings)
                instanceof TimerIntegrityInformationScheduler, 
                "The default Scheduler should be the '" + TimerIntegrityInformationScheduler.class.getName() + "'");
    }

}
