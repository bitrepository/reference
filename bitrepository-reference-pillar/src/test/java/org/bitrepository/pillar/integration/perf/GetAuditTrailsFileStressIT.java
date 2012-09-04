package org.bitrepository.pillar.integration.perf;
/*
 * #%L
 * Bitrepository Reference Pillar
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

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getaudittrails.BlockingAuditTrailClient;
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

    @Test( groups = {"pillar-stress-test"}, dependsOnGroups={"stress-test-pillar-population"})
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

    @Test( groups = {"pillar-stress-test"})
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