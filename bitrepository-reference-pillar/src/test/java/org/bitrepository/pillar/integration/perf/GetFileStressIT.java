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

import org.bitrepository.access.getfile.BlockingGetFileClient;
import org.bitrepository.access.getfile.ConversationBasedGetFileClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.integration.perf.metrics.Metrics;
import org.bitrepository.pillar.messagefactories.GetFileMessageFactory;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class GetFileStressIT extends PillarPerformanceTest {
    protected GetFileClient getFileClient;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        SecurityManager securityManager1 = createSecurityManager();
        MessageBus messageBus = MessageBusManager.createMessageBus(settingsForTestClient, securityManager1);
        ConversationMediator conversationMediator = new CollectionBasedConversationMediator(settingsForTestClient,
                                                                                            messageBus);

        getFileClient = new ConversationBasedGetFileClient(
                messageBus,
                conversationMediator,
                settingsForTestClient, settingsForTestClient.getComponentID());
    }

    @Test( groups = {"pillar-stress-test"})
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
        for (String fileID:fileIDs) {
            blockingGetFileClient.getFileFromSpecificPillar(
                    collectionID, DEFAULT_FILE_ID, null, httpServerConfiguration.getURL(NON_DEFAULT_FILE_ID), getPillarID(), null,
                    "performing singleGetFilePerformanceTest");
            metrics.mark(fileID);
        }
    }

    @Test( groups = {"pillar-stress-test"})
    public void parallelGetFilePerformanceTest() throws Exception {
        final int numberOfFiles = testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.numberOfFiles");
        final int partStatisticsInterval = testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.partStatisticsInterval");
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
            getLimiter.addJob(DEFAULT_FILE_ID);
            getFileClient.getFileFromSpecificPillar(
                    collectionID, DEFAULT_FILE_ID, null, httpServerConfiguration.getURL(NON_DEFAULT_FILE_ID + "-" + i), getPillarID(),
                    eventHandler,
                    " performing parallelGetFilePerformance");
        }

        awaitAsynchronousCompletion(metrics, numberOfFiles);
    }

    @Test( groups = {"pillar-stress-test"})
    public void noIdentfyGetFilePerformanceTest() throws Exception {
        final int numberOfFiles = testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.numberOfFiles");
        final int partStatisticsInterval = testConfiguration.getInt("pillarintegrationtest.GetFileStressIT.parallelGet.partStatisticsInterval");
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
        GetFileMessageFactory msgFactory = new GetFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        for (int i = 1; i <= numberOfFiles; i++) {
            String correlationID = msgFactory.getNewCorrelationID();
            getLimiter.addJob(correlationID);
            GetFileRequest getRequest =
                    msgFactory.createGetFileRequest("noIdentfyGetFilePerformanceTest", correlationID,
                            httpServerConfiguration.getURL(NON_DEFAULT_FILE_ID + "-" + i).toExternalForm(), DEFAULT_FILE_ID, null,
                            getPillarID(), getPillarID(), settingsForTestClient.getReceiverDestinationID(),  pillarDestination);
            messageBus.sendMessage(getRequest);
        }

        awaitAsynchronousCompletion(metrics, numberOfFiles);
    }

    public String lookupGetFileDestination() {
        MessageReceiver clientReceiver = new MessageReceiver(settingsForTestClient.getReceiverDestinationID(), testEventManager);
        messageBus.addListener(clientReceiver.getDestination(), clientReceiver.getMessageListener());;
        GetFileMessageFactory pillarLookupmMsgFactory =
                new GetFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        IdentifyPillarsForGetFileRequest identifyRequest =
                pillarLookupmMsgFactory.createIdentifyPillarsForGetFileRequest(DEFAULT_FILE_ID);
        messageBus.sendMessage(identifyRequest);
        String pillarDestination = clientReceiver.waitForMessage(IdentifyPillarsForGetFileResponse.class).getReplyTo();
        messageBus.removeListener(clientReceiver.getDestination(), clientReceiver.getMessageListener());
        return pillarDestination;
    }
}