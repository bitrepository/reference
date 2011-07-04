/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.modify;

import java.beans.ExceptionListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.modify_client.configuration.ModifyConfiguration;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tester class for testing the PutClient functionality. 
 * Is currently set up to use the SimplePutClient, but whichever implementation
 * of the PutClient should pass the same tests.
 *
 */
public class PutClientTester extends ExtendedTestCase {
    /** The time for waiting */
    private static final int WAITING_TIME_FOR_MESSAGE = 3000;

    @Test(groups={"regressionstest"})
    public void findPillarsToPutTester() throws Exception {
        addDescription("Tests whether a specific message is sent by the GetClient");
        String dataId = "dataId1";
        String slaId = "THE-SLA";
        String pillarId = "The-Test-Pillar";
        String queue = "" + (new Date()).getTime();
        ModifyConfiguration config = ModifyComponentFactory.getInstance().getConfig();
        config.setQueue(queue);
        SimplePutClient pc = new SimplePutClient();
        TestMessageListener listener = new TestMessageListener();
        ProtocolComponentFactory.getInstance().getMessageBus().addListener(queue, listener);
        
        File testFile = new File("src/test/resources/test.txt");

        pc.putFileWithId(testFile, dataId, slaId);
        
        synchronized(this) {
            try {
                wait(WAITING_TIME_FOR_MESSAGE);
            } catch (Exception e) {
                // print, but ignore!
                e.printStackTrace();
            }
        }
        
        addStep("Verify that the PutClient has sent a message for identifying "
                + "the pillars for put.", "Should be OK.");
        
        Assert.assertNotNull(listener.getMessage());
        Assert.assertEquals(listener.getMessageClass().getName(),
                IdentifyPillarsForPutFileRequest.class.getName());
        IdentifyPillarsForPutFileRequest identify
                = org.bitrepository.common.JaxbHelper.loadXml(IdentifyPillarsForPutFileRequest.class,
                                                              new ByteArrayInputStream(
                                                                      listener.getMessage().getBytes()));
        Assert.assertEquals(identify.getBitRepositoryCollectionID(), slaId);
        
        addStep("Respond to identify request.", "No problems.");
        
        IdentifyPillarsForPutFileResponse identifyResponse 
                = new IdentifyPillarsForPutFileResponse();
        identifyResponse.setCorrelationID(identify.getCorrelationID());
        identifyResponse.setPillarID(pillarId);
        identifyResponse.setBitRepositoryCollectionID(slaId);
        identifyResponse.setMinVersion(BigInteger.valueOf(1L));
        identifyResponse.setVersion(BigInteger.valueOf(1L));
        // TODO identifyReply.setTimeToDeliver(value) ???
        
        identifyResponse.setTo(queue);
        ProtocolComponentFactory.getInstance().getMessageBus().sendMessage(identifyResponse);
        
        synchronized(this) {
            try {
                wait(WAITING_TIME_FOR_MESSAGE);
            } catch (Exception e) {
                // print, but ignore!
                e.printStackTrace();
            }
        }
        
        addStep("Verifying that a 'PutFileRequest' has been sent for the file.", 
                "A PutFileRequest for the specific file.");
        Assert.assertEquals(listener.getMessageClass().getName(),
                PutFileRequest.class.getName());
        PutFileRequest put = org.bitrepository.common.JaxbHelper
                .loadXml(PutFileRequest.class, new ByteArrayInputStream(listener.getMessage().getBytes()));
        Assert.assertEquals(put.getFileID(), dataId);
        Assert.assertEquals(put.getBitRepositoryCollectionID(), slaId);
        Assert.assertEquals(put.getPillarID(), pillarId);
        
        addStep("Verify that the expected file can be downloaded.", 
                "Should be OK.");
        URL url = new URL(put.getFileAddress());
        File outputFile = new File(put.getFileID());
        FileOutputStream outStream = null;
        try {
            outStream = new FileOutputStream(outputFile);
            ProtocolComponentFactory.getInstance().getFileExchange().downloadFromServer(outStream, url);
        } finally {
            if(outStream != null) {
                outStream.close();
            }
        }
        
        Assert.assertTrue(outputFile.isFile());
        Assert.assertEquals(BigInteger.valueOf(outputFile.length()), 
                put.getFileSize(), "Different size than expected!");

        addStep("Check whether the file is missing for this pillar.",
                "Should be part of the outstanding.");
        Assert.assertTrue(pc.outstandings.isOutstanding(dataId), 
                "The dataId should be marked as outstanding.");
        Assert.assertTrue(pc.outstandings.isOutstandingAtPillar(dataId, pillarId),
                "The dataId should be marked as outstanding for the pillar.");

        addStep("Send PutFileProgressResponse for the request.", "Should be handled.");
        PutFileProgressResponse response = new PutFileProgressResponse();
        response.setCorrelationID(put.getCorrelationID());
        response.setFileAddress(put.getFileAddress());
        response.setFileID(put.getFileID());
        response.setPillarID(pillarId);
        response.setBitRepositoryCollectionID(slaId);
        response.setMinVersion(BigInteger.valueOf(1L));
        response.setVersion(BigInteger.valueOf(1L));
        
        ProtocolComponentFactory.getInstance().getMessageBus().sendMessage(response);
        
        synchronized(this) {
            try {
                wait(WAITING_TIME_FOR_MESSAGE);
            } catch (Exception e) {
                // print, but ignore!
                e.printStackTrace();
            }
        }
        
        addStep("Check whether the file is still missing for this pillar.",
                "Should be part of the outstanding.");
        Assert.assertTrue(pc.outstandings.isOutstanding(dataId), 
                "The dataId should be marked as outstanding.");
        Assert.assertTrue(pc.outstandings.isOutstandingAtPillar(dataId, pillarId),
                "The dataId should be marked as outstanding for the pillar.");

        addStep("Send PutFileFinalResponse for the request.", "Should be handled.");
        PutFileFinalResponse complete = new PutFileFinalResponse();
        complete.setCorrelationID(put.getCorrelationID());
        complete.setFileAddress(put.getFileAddress());
        complete.setFileID(put.getFileID());
        complete.setPillarID(pillarId);
        complete.setBitRepositoryCollectionID(slaId);
        complete.setMinVersion(BigInteger.valueOf(1L));
        complete.setVersion(BigInteger.valueOf(1L));
        FinalResponseInfo completeInfo = new FinalResponseInfo();
        completeInfo.setFinalResponseCode("1");
        completeInfo.setFinalResponseText("Done with put.");
        complete.setFinalResponseInfo(completeInfo);
        // Ignore the salt!
        
        ProtocolComponentFactory.getInstance().getMessageBus().sendMessage(complete);
        
        synchronized(this) {
            try {
                wait(WAITING_TIME_FOR_MESSAGE);
            } catch (Exception e) {
                // print, but ignore!
                e.printStackTrace();
            }
        }
        
        // verify that it is marked as complete
        addStep("Check whether the file is still missing for this pillar.",
                "Should be part of the outstanding.");
        Assert.assertFalse(pc.outstandings.isOutstandingAtPillar(dataId, pillarId),
                "The dataId should be marked as outstanding for the pillar.");
        Assert.assertFalse(pc.outstandings.isOutstanding(dataId), 
                "The dataId should be marked as outstanding.");
    }

    @SuppressWarnings("rawtypes")
    protected class TestMessageListener extends AbstractMessageListener
            implements ExceptionListener {
        private String message = null;
        private Class messageClass = null;

        @Override
        public void onMessage(PutFileRequest message) {
            onMessage((Object) message);
        }

        @Override
        public void onMessage(PutFileProgressResponse message) {
            onMessage((Object) message);
        }

        @Override
        public void onMessage(IdentifyPillarsForPutFileRequest message) {
            onMessage((Object) message);
        }

        public void onMessage(Object msg) {
            try {
                message = JaxbHelper.serializeToXml(msg);
                messageClass = msg.getClass();
            } catch (Exception e) {
                Assert.fail("Should not throw an exception: ", e);
            }
            // awaken the tester
            Thread.currentThread().notifyAll();
        }

        public String getMessage() {
            return message;
        }
        public Class getMessageClass() {
            return messageClass;
        }

		@Override
		public void exceptionThrown(Exception e) {
			// TODO Auto-generated method stub
			
		}
    }
}
