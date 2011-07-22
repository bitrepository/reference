/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id: ReferencePillar.java 210 2011-07-04 19:44:03Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-integration/src/main/java/org/bitrepository/pillar/ReferencePillar.java $
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar;

import java.math.BigInteger;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile.ChecksumDataItems;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.clienttest.MessageReceiver;
import org.bitrepository.common.IntegrationTest;
import org.bitrepository.protocol.TestMessageFactory;
import org.bitrepository.protocol.bus.MessageBusConfigurationFactory;
import org.bitrepository.protocol.bus.MessageBusWrapper;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

/**
 * Test class for testing the pillar externally.
 * 
 * IMPORTANT: start the required environment locally before running this test.
 *
 * First a local instance of ActiveMQ has to be started. 
 * 
 * Then the local reference pillar start is started by the following call:
 * java -cp $CLASSPATH -Dorg.bitrepository.config=configuration/xml org.bitrepository.pillar.ReferencePillarLauncher
 * 
 * Where the following libraries are in the CLASSPATH (perhaps not all, but at least most of these):
 * activemq-all-5.3.2.jar
 * commons-lang-2.6.jar
 * jaxb2-basics-tools-0.6.0.jar
 * jaxb2-basics-runtime-0.6.0.jar
 * jaxb2-basics-jaxb-xjc-2.1.13.MR2.jar
 * jaxb2-basics-0.6.0.jar
 * logback-classic-0.9.27.jar
 * logback-core-0.9.27.jar
 * slf4j-api-1.6.1.jar
 * bitrepository-common-0.2-SNAPSHOT.jar
 * bitrepository-integration-0.2-SNAPSHOT.jar
 * bitrepository-protocol-0.2-SNAPSHOT.jar
 * bitrepository-common-0.2-SNAPSHOT.jar
 * bitrepository-common-0.2-SNAPSHOT-tests.jar
 * 
 * And the configurations 'protocol-configuration.xml' and 'integrationclient-configuration.xml' is in the directory
 * 'configuration/xml', with the wanted configurations (e.g. the local message broker)
 * 
 * Then change the 'initialise' method to reflect the configurations. Currently they are set to mine (JOLF).
 * 
 */
public class LocalPillarTest extends IntegrationTest {

    protected static final String DEFAULT_FILE_ID = TestMessageFactory.FILE_ID_DEFAULT;

    protected PillarSettings settings;
    protected PillarTestMessageFactory msgFactory;
    
    MessageBusConfigurations messageBusConfigurations = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
    MessageBus messageBus = new MessageBusWrapper(MessageBusFactory.createMessageBus(messageBusConfigurations), testEventManager);

    /**
     * Defines the configuration for pillar.
     */
    @BeforeTest(alwaysRun=true)
    public void initialise() {
        MutablePillarSettings pillarSettings = new MutablePillarSettings();
        pillarSettings.setBitRepositoryCollectionID("JOLF-PILLAR-TEST");
        pillarSettings.setBitRepositoryCollectionTopicID("JOLF-PILLAR-TESTING-TOPIC");
        pillarSettings.setMessageBusConfiguration(messageBusConfigurations);
        pillarSettings.setFileDirName("target/fileDir"); // irrelevant.
        pillarSettings.setLocalQueue("LocalPillarQueue"); // irrelevant.
        pillarSettings.setTimeToDownloadMeasure("MILLISECONDS"); // irrelevant.
        pillarSettings.setTimeToDownloadValue(1L); // irrelevant.
        pillarSettings.setTimeToUploadMeasure("MILLISECONDS"); // irrelevant.
        pillarSettings.setTimeToUploadValue(1L); // irrelevant.
        settings = pillarSettings;
        
        msgFactory = new PillarTestMessageFactory(settings);
    }
    
    @Test( groups = { "SomeGroup" })
    public void testLocalPillarPut() throws Exception {
        addDescription("Tests the put functionality of the reference pillar.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        Long FILE_SIZE = 27L;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String FILE_CHECKSUM = "940a51b250e7aa82d8e8ea31217ff267";
        Date startDate = new Date();
        
        String clientDestinationId = "JOLF-Client-id";
        MessageReceiver clientTopic = new MessageReceiver("client topic receiver", testEventManager);
        messageBus.addListener(clientDestinationId, clientTopic.getMessageListener());    

        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest 
                = msgFactory.createIdentifyPillarsForPutFileRequest(clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        System.out.println(receivedIdentifyResponse);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        receivedIdentifyResponse.getCorrelationID(),
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getPillarID(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
       
        addStep("Create and send the actual Put message to the pillar.", 
                "Should be received and handled by the pillar.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(
                receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, FILE_SIZE,
                receivedIdentifyResponse.getPillarID(), clientDestinationId, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the ProgressResponse for the put request", "The put response should be sent by the pillar.");
        PutFileProgressResponse progressResponse = clientTopic.waitForMessage(PutFileProgressResponse.class);
        System.out.println(progressResponse);
        Assert.assertEquals(progressResponse,
                msgFactory.createPutFileProgressResponse(
                        progressResponse.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        progressResponse.getFileID(), 
                        progressResponse.getPillarID(), 
                        progressResponse.getPillarChecksumSpec(), 
                        progressResponse.getProgressResponseInfo(), 
                        progressResponse.getReplyTo(), 
                        progressResponse.getTo()));

        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
        System.out.println(finalResponse);
        Assert.assertEquals(finalResponse,
                msgFactory.createPutFileFinalResponse(
                        finalResponse.getChecksumsDataForNewFile(),
                        finalResponse.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getFinalResponseInfo(), 
                        finalResponse.getPillarID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        finalResponse.getReplyTo(), 
                        finalResponse.getTo()));
        
        // validating the checksum
        ChecksumsDataForNewFile receivedChecksums = finalResponse.getChecksumsDataForNewFile();
        Assert.assertEquals(receivedChecksums.getFileID(), FILE_ID);
        Assert.assertEquals(receivedChecksums.getNoOfItems(), BigInteger.valueOf(1L));
        ChecksumDataItems checksumItems = receivedChecksums.getChecksumDataItems();
        Assert.assertEquals(checksumItems.getChecksumDataForFile().size(), 1);
        ChecksumDataForFileTYPE checksumdata = checksumItems.getChecksumDataForFile().get(0);
        Assert.assertEquals(checksumdata.getChecksumValue(), FILE_CHECKSUM);
        Assert.assertNull(checksumdata.getChecksumSpec().getChecksumSalt(), "should be no salt");
        Assert.assertEquals(checksumdata.getChecksumSpec().getChecksumType(), "MD5");
        Assert.assertTrue(checksumdata.getCalculationTimestamp().toGregorianCalendar().getTime().getTime() > startDate.getTime());

    }
}
