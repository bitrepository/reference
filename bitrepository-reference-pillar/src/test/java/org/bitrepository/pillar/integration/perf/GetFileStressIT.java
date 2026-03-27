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
import org.bitrepository.access.getfile.BlockingGetFileClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.integration.perf.metrics.Metrics;
import org.bitrepository.pillar.messagefactories.GetFileMessageFactory;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

public class GetFileStressIT extends PillarPerformanceTest {
    public static final String FOLDER_NAME = "src/test/resources";
    protected GetFileClient getFileClient;

    @BeforeEach
    public void initialiseReferenceTest() throws Exception {
        getFileClient = AccessComponentFactory.getInstance().createGetFileClient(
                settingsForTestClient, createSecurityManager(), settingsForTestClient.getComponentID()
        );
    }

    @AfterAll
    static void removeUnnecessaryFiles() throws IOException {
        removeFiles("noIdentfy", FOLDER_NAME);
        removeFiles("parallel", FOLDER_NAME);
        removeFiles("single", FOLDER_NAME);
    }

    private static void removeFiles(String fileStartsWith, String folderName) throws IOException {
        Path directory = Paths.get(folderName);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, fileStartsWith + "*")) {
            for (Path entry : stream) {
                Files.delete(entry);
            }
        }
    }

    @Test
    @Tag("pillar-stress-test")
    public void singleGetFilePerformanceTest() throws Exception {
        final int NUMBER_OF_FILES = 1000;
        final int PART_STATISTIC_INTERVAL = 100;
        addDescription("Attempt to get " + NUMBER_OF_FILES + " files from the pillar, one at a time.");
        BlockingGetFileClient blockingGetFileClient = new BlockingGetFileClient(getFileClient);
        String[] fileIDs = TestFileHelper.createFileIDs(NUMBER_OF_FILES, "singleTreadedGetTest");
        Metrics metrics = new Metrics("get", NUMBER_OF_FILES, PART_STATISTIC_INTERVAL);
        metrics.addAppenders(metricAppenders);
        metrics.start();
        addStep("Getting " + NUMBER_OF_FILES + " files", "Not errors should occur");
        for (String fileID : fileIDs) {
            blockingGetFileClient.getFileFromSpecificPillar(
                    collectionID, defaultFileId, null, httpServerConfiguration.getURL(nonDefaultFileId), getPillarID(), null,
                    "performing singleGetFilePerformanceTest");
            metrics.mark(fileID);
        }
    }

    @Test
    @Tag("pillar-stress-test")
    public void parallelGetFilePerformanceTest() throws Exception {
        final int numberOfFiles =
                testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.numberOfFiles");
        final int partStatisticsInterval =
                testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.partStatisticsInterval");
        final int numberOfParallelGets =
                testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.numberOfParallelGets");
        addDescription("Attempt to get " + numberOfFiles + " files from " + getPillarID() + ", " + numberOfParallelGets +
                " at the 'same' time.");
        final Metrics metrics = new Metrics("get", numberOfFiles, partStatisticsInterval);
        metrics.addAppenders(metricAppenders);
        metrics.start();
        addStep("Getting " + numberOfFiles + " files", "Not errors should occur");
        ParallelOperationLimiter getLimiter = new ParallelOperationLimiter(numberOfParallelGets);
        EventHandler eventHandler = new OperationEventHandlerForMetrics(metrics, getLimiter);
        for (int i = 1; i <= numberOfFiles; i++) {
            getLimiter.addJob(defaultFileId);
            getFileClient.getFileFromSpecificPillar(
                    collectionID, defaultFileId, null, httpServerConfiguration.getURL(nonDefaultFileId + "-" + i),
                    getPillarID(), eventHandler, " performing parallelGetFilePerformance");
        }
        awaitAsynchronousCompletion(metrics, numberOfFiles);
    }

    @Test
    @Tag("pillar-stress-test")
    public void noIdentfyGetFilePerformanceTest() throws Exception {
        final int numberOfFiles =
                testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.numberOfFiles");
        final int partStatisticsInterval =
                testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.partStatisticsInterval");
        final int numberOfParallelGets =
                testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.numberOfParallelGets");
        addDescription("Attempt to get " + numberOfFiles + " files from " + getPillarID() + ", " + numberOfParallelGets +
                " at the 'same' time without individual identifies.");
        String pillarDestination = lookupGetFileDestination();
        final Metrics metrics = new Metrics("get", numberOfFiles, partStatisticsInterval);
        metrics.addAppenders(metricAppenders);
        metrics.start();
        addStep("Getting " + numberOfFiles + " files", "Not errors should occur");
        ParallelOperationLimiter getLimiter = new ParallelOperationLimiter(numberOfParallelGets);
        messageBus.addListener(settingsForTestClient.getReceiverDestinationID(), new MessageHandlerForMetrics(metrics, getLimiter));
        GetFileMessageFactory msgFactory = new GetFileMessageFactory(collectionID, settingsForTestClient,
                getPillarID(), null);
        for (int i = 1; i <= numberOfFiles; i++) {
            String correlationID = msgFactory.getNewCorrelationID();
            getLimiter.addJob(correlationID);
            GetFileRequest getRequest =
                    msgFactory.createGetFileRequest("noIdentfyGetFilePerformanceTest", correlationID,
                            httpServerConfiguration.getURL(nonDefaultFileId + "-" + i).toExternalForm(),
                            defaultFileId, null, getPillarID(), getPillarID(),
                            settingsForTestClient.getReceiverDestinationID(), pillarDestination);
            messageBus.sendMessage(getRequest);
        }

        awaitAsynchronousCompletion(metrics, numberOfFiles);
    }

    public String lookupGetFileDestination() {
        MessageReceiver clientReceiver = new MessageReceiver(settingsForTestClient.getReceiverDestinationID());
        messageBus.addListener(clientReceiver.getDestination(), clientReceiver.getMessageListener());
        GetFileMessageFactory pillarLookupmMsgFactory =
                new GetFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        IdentifyPillarsForGetFileRequest identifyRequest =
                pillarLookupmMsgFactory.createIdentifyPillarsForGetFileRequest(defaultFileId);
        messageBus.sendMessage(identifyRequest);
        String pillarDestination = clientReceiver.waitForMessage(IdentifyPillarsForGetFileResponse.class).getReplyTo();
        messageBus.removeListener(clientReceiver.getDestination(), clientReceiver.getMessageListener());
        return pillarDestination;
    }
}