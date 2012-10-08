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

import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.BlockingPutFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.pillar.integration.perf.metrics.Metrics;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PutFileStressIT extends PillarPerformanceTest {
    protected PutFileClient putClient;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        putClient = ModifyComponentFactory.getInstance().retrievePutClient(
                settingsForCUT, new DummySecurityManager(), settingsForCUT.getComponentID()
        );
    }

    @Test( groups = {"pillar-stress-test", "stress-test-pillar-population"})
    public void singleTreadedPut() throws Exception {
        final int NUMBER_OF_FILES = 100;
        final int PART_STATISTIC_INTERVAL = 10;
        addDescription("Attempt to put " + NUMBER_OF_FILES + " files into the pillar, one at a time.");
        BlockingPutFileClient blockingPutFileClient = new BlockingPutFileClient(putClient);
        String[] fileIDs = TestFileHelper.createFileIDs(NUMBER_OF_FILES, "singleTreadedPutTest");
        Metrics metrics = new Metrics("put", NUMBER_OF_FILES, PART_STATISTIC_INTERVAL);
        metrics.addAppenders(metricAppenders);
        metrics.start();
        addStep("Add " + NUMBER_OF_FILES + " files", "Not errors should occur");
        for (String fileID:fileIDs) {
            blockingPutFileClient.putFile(httpServer.getURL(TestFileHelper.DEFAULT_FILE_ID), fileID, 10L,
                    TestFileHelper.getDefaultFileChecksum(), null, null, "singleTreadedPut stress test file");
            metrics.mark(fileID);
        }

        addStep("Check that the files are now present on the pillar(s)", "No missing files should be found.");
        //ToDo assert that the files are present
    }

    @Test( groups = {"pillar-stress-test"})
    public void parallelPut() throws Exception {
        final int  NUMBER_OF_FILES = 100;
        final int  PART_STATISTIC_INTERVAL = 10;
        addDescription("Attempt to put " + NUMBER_OF_FILES + " files into the pillar at the 'same' time.");
        String[] fileIDs = TestFileHelper.createFileIDs(NUMBER_OF_FILES, "parallelPutTest");
        final Metrics metrics = new Metrics("put", NUMBER_OF_FILES, PART_STATISTIC_INTERVAL);
        metrics.addAppenders(metricAppenders);
        metrics.start();
        addStep("Add " + NUMBER_OF_FILES + " files", "Not errors should occur");
        EventHandler eventHandler = new PutEventHandlerForMetrics(metrics);
        for (String fileID:fileIDs) {
            putClient.putFile(httpServer.getURL(TestFileHelper.DEFAULT_FILE_ID), fileID, 10L,
                    TestFileHelper.getDefaultFileChecksum(), null, eventHandler, "singleTreadedPut stress test file");
        }

        awaitAsynchronousCompletion(metrics, NUMBER_OF_FILES);

        addStep("Check that the files are now present on the pillar(s)", "No missing files should be found.");

        //ToDo Actually assert that the files are present.

        existingFiles = fileIDs;
    }

    private class PutEventHandlerForMetrics implements EventHandler {
        private final Metrics metrics;
        public PutEventHandlerForMetrics(Metrics metrics) {
            this.metrics = metrics;
        }

        @Override
        public void handleEvent(OperationEvent event) {
            if (event.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
                //CompleteEvent completeEvent = (CompleteEvent)event;
                // PutFileCompletePillarEvent fileID = (PutFileCompletePillarEvent)completeEvent.getComponentResults()[0];
                // Todo The current complete event should return a event, so we can detect which file has been affected
                this.metrics.mark("#" + metrics.getCount());
            } else if (event.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
                this.metrics.registerError(event.getInfo());
            }
        }
    }
}