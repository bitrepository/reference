package org.bitrepository.pillar.integration.perf;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.access.getaudittrails.client.BlockingAuditTrailClient;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.pillar.integration.perf.metrics.Metrics;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetAuditTrailsFileStressIT extends PillarPerformanceTest {
    protected AuditTrailClient auditTrailClient;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        auditTrailClient = AccessComponentFactory.getInstance().createAuditTrailClient(
                componentSettings, new DummySecurityManager(), componentSettings.getComponentID()
        );
    }

    @Test( groups = {"pillar-integration-test"}, dependsOnGroups={"stress-test-pillar-population"})
    public void singleTreadedGetAuditTrails() throws Exception {
        final int NUMBER_OF_AUDITS = 100;
        final int PART_STATISTIC_INTERVAL = NUMBER_OF_AUDITS/5;
        addDescription("Attempt to request " + NUMBER_OF_AUDITS + " full audit trails one at a time.");

        BlockingAuditTrailClient blockingAuditTrailFileClient = new BlockingAuditTrailClient(auditTrailClient);
        Metrics metrics = new Metrics("getAuditTrails", NUMBER_OF_AUDITS, PART_STATISTIC_INTERVAL);
        metrics.addAppenders(metricAppenders);
        metrics.start();
        addStep("Request " + NUMBER_OF_AUDITS + " full audit trails one", "Not errors should occur");
        for (int i = 0; i < NUMBER_OF_AUDITS;i++) {
            blockingAuditTrailFileClient.getAuditTrails(
                    null, null, null, null, "singleTreadedGetAuditTrails stress test");
            metrics.mark();
        }
    }

    @Test( groups = {"pillar-integration-test"})
    public void parallelGetAuditTrails() throws Exception {
        final int  NUMBER_OF_AUDITS = 10;
        final int  PART_STATISTIC_INTERVAL = NUMBER_OF_AUDITS/5;
        addDescription("Attempt to request " + NUMBER_OF_AUDITS + " full audit trails one at a time.");

        final Metrics metrics = new Metrics("put", NUMBER_OF_AUDITS, PART_STATISTIC_INTERVAL);
        metrics.addAppenders(metricAppenders);
        metrics.start();
        addStep("Add " + NUMBER_OF_AUDITS + " files", "Not errors should occur");
        EventHandler eventHandler = new EventHandlerForMetrics(metrics);
        for (int i = 0; i > NUMBER_OF_AUDITS;i++) {
            auditTrailClient.getAuditTrails(
                    null, null, null, eventHandler, "singleTreadedGetAuditTrails stress test");
        }

        awaitAsynchronousCompletion(metrics, NUMBER_OF_AUDITS);
    }
}