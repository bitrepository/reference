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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.BlockingPutFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.pillar.integration.perf.metrics.Metrics;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PutFileStressIT extends PillarPerformanceTest {
    protected PutFileClient putClient;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        putClient = ModifyComponentFactory.getInstance().retrievePutClient(
                settingsForTestClient, createSecurityManager(), settingsForTestClient.getComponentID()
        );
    }

    @Test( groups = {"pillar-stress-test", "stress-test-pillar-population"})
    public void singleTreadedPut() throws Exception {
        final int NUMBER_OF_FILES = 10;
        final int PART_STATISTIC_INTERVAL = 2;
        addDescription("Attempt to put " + NUMBER_OF_FILES + " files into the pillar, one at a time.");
        BlockingPutFileClient blockingPutFileClient = new BlockingPutFileClient(putClient);
        String[] fileIDs = TestFileHelper.createFileIDs(NUMBER_OF_FILES, "singleTreadedPutTest");
        Metrics metrics = new Metrics("put", NUMBER_OF_FILES, PART_STATISTIC_INTERVAL);
        metrics.addAppenders(metricAppenders);
        metrics.start();
        addStep("Add " + NUMBER_OF_FILES + " files", "Not errors should occur");
        for (String fileID:fileIDs) {
            blockingPutFileClient.putFile(collectionID, httpServerConfiguration.getURL(TestFileHelper.DEFAULT_FILE_ID), fileID, 10L,
                    TestFileHelper.getDefaultFileChecksum(), null, null, "singleTreadedPut stress test file");
            metrics.mark(fileID);
        }

        addStep("Check that the files are now present on the pillar(s)", "No missing files should be found.");
        //ToDo assert that the files are present
    }

    @Test( groups = {"pillar-stress-test"})
    public void parallelPut() throws Exception {
        final int numberOfFiles = testConfiguration.getInt("pillarintegrationtest.PutFileStressIT.parallelPut.numberOfFiles");
        final int  partStatisticsInterval = testConfiguration.getInt("pillarintegrationtest.PutFileStressIT.parallelPut.partStatisticsInterval");
        final int  numberOfParallelPuts =
                testConfiguration.getInt("pillarintegrationtest.PutFileStressIT.parallelPut.numberOfParallelPuts");
        addDescription("Attempt to put " + numberOfFiles + " files into the pillar, " + numberOfParallelPuts + " at 'same' time.");
        String[] fileIDs = TestFileHelper.createFileIDs(numberOfFiles, "parallelPutTest");
        final Metrics metrics = new Metrics("put", numberOfFiles, partStatisticsInterval);
        metrics.addAppenders(metricAppenders);
        metrics.start();
        addStep("Add " + numberOfFiles + " files", "Not errors should occur");
        ParallelPutLimiter putLimiter = new ParallelPutLimiter(numberOfParallelPuts);
        EventHandler eventHandler = new PutEventHandlerForMetrics(metrics, putLimiter);
        for (String fileID:fileIDs) {
            putLimiter.addJob(fileID);
            putClient.putFile(collectionID, httpServerConfiguration.getURL(TestFileHelper.DEFAULT_FILE_ID), fileID, 10L,
                    TestFileHelper.getDefaultFileChecksum(), null, eventHandler, "parallelPut stress test file");
        }

        awaitAsynchronousCompletion(metrics, numberOfFiles);

        addStep("Check that the files are now present on the pillar(s)", "No missing files should be found.");
        existingFiles = fileIDs;
    }

    private class PutEventHandlerForMetrics implements EventHandler {
        private final Metrics metrics;
        private final ParallelPutLimiter putLimiter;
        public PutEventHandlerForMetrics(Metrics metrics, ParallelPutLimiter putLimiter) {
            this.metrics = metrics;
            this.putLimiter = putLimiter;
        }

        @Override
        public void handleEvent(OperationEvent event) {
            if (event.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
                this.metrics.mark("#" + metrics.getCount());
                putLimiter.removeJob(event.getFileID());
            } else if (event.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
                this.metrics.registerError(event.getInfo());
                putLimiter.removeJob(event.getFileID());
            }
        }
    }

    private class ParallelPutLimiter {
        private final BlockingQueue<String> activePuts;

        ParallelPutLimiter(int limit) {
            activePuts = new LinkedBlockingQueue<>(limit);
        }

        void addJob(String fileID) {
            try {
                activePuts.put(fileID);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        void removeJob(String fileID) {
            activePuts.remove(fileID);
        }
    }
}