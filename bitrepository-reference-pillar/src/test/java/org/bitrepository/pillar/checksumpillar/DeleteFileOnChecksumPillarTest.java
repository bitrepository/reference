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
package org.bitrepository.pillar.checksumpillar;

import java.io.File;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMediator;
import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DeleteFileOnChecksumPillarTest extends DefaultFixturePillarTest {
    DeleteFileMessageFactory msgFactory;
    
    MemoryCache cache;
    ChecksumPillarMediator mediator;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseDeleteFileTests() throws Exception {
        msgFactory = new DeleteFileMessageFactory(settings);
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        addStep("Initialize the pillar.", "Should not be a problem.");
        cache = new MemoryCache();
        mediator = new ChecksumPillarMediator(messageBus, settings, cache);
    }
    
    @AfterMethod (alwaysRun=true) 
    public void closeArchive() {
        if(cache != null) {
            cache.cleanUp();
        }
        if(mediator != null) {
            mediator.close();
        }
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarDeleteFileTestSuccessCase() throws Exception {
        addDescription("Testing the delete operation for the checksum pillar.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String AUDIT = "DELETE-FILE-TEST";
        String CHECKSUM = "delete-checksum";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Send message for identification of the pillar.", 
                "The checksum pillar receive and handle the message.");
        IdentifyPillarsForDeleteFileRequest identifyReq = msgFactory.createIdentifyPillarsForDeleteFileRequest(AUDIT, 
                FILE_ID, clientDestinationId);
        mediator.onMessage(identifyReq);
        
        addStep("Receive and validate response from the checksum pillar.",
                "The pillar should make a positive response.");
        IdentifyPillarsForDeleteFileResponse identifyRes = clientTopic.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertEquals(identifyRes, msgFactory.createIdentifyPillarsForDeleteFileResponse(
                identifyRes.getCorrelationID(), FILE_ID, csSpec, pillarId,identifyRes.getReplyTo(),  
                identifyRes.getResponseInfo(), identifyRes.getTimeToDeliver(), clientDestinationId));
        
        addStep("Send the actual delete request.", "Should be caught and handled by the checksum replica.");
        
        
        addStep("Receive and validate the progress response from the checksum pillar.", 
                "Should tell about progress.");
        
        addStep("Receive and validate the final response from the checksum pillar.", 
                "Should tell about a successfull deletion.");
        
        addStep("Validate the cache.", "The file shoud no longer be found in the cache.");
        
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarDeleteFileTestFailedNoSuchFile() throws Exception {
        // TODO
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarDeleteFileTestFailedWrongChecksum() throws Exception {
        // TODO
    }


}
