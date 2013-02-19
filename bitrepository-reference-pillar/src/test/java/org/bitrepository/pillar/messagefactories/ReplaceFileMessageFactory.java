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
/*
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.messagefactories;

import java.math.BigInteger;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.common.settings.Settings;

public class ReplaceFileMessageFactory extends PillarTestMessageFactory {
    private final String pillarID;
    
    public ReplaceFileMessageFactory(String collectionID, Settings clientSettings, String pillarID,
                                     String pillarDestination) {
        super(collectionID, clientSettings, pillarDestination);
        this.pillarID = pillarID;
    }
    
    public IdentifyPillarsForReplaceFileRequest createIdentifyPillarsForReplaceFileRequest( 
            String fileId, Long fileSize) {
        
        IdentifyPillarsForReplaceFileRequest res = new IdentifyPillarsForReplaceFileRequest();
        initializeIdentifyRequest(res);
        res.setFileID(fileId);
        if(fileSize != null) {
            res.setFileSize(BigInteger.valueOf(fileSize));
        }
        
        return res;
    }

    public ReplaceFileRequest createReplaceFileRequest(
            ChecksumDataForFileTYPE existingChecksumData, 
            ChecksumDataForFileTYPE newChecksumData, 
            ChecksumSpecTYPE csExistingRequest, 
            ChecksumSpecTYPE csNewRequest, 
            String fileAddress,
            String fileId, 
            long fileSize) {
        ReplaceFileRequest res = new ReplaceFileRequest();
        initializeOperationRequest(res);
        res.setChecksumDataForExistingFile(existingChecksumData);
        res.setChecksumDataForNewFile(newChecksumData);
        res.setChecksumRequestForExistingFile(csExistingRequest);
        res.setChecksumRequestForNewFile(csNewRequest);
        res.setFileID(fileId);
        res.setFileSize(BigInteger.valueOf(fileSize));
        res.setFileAddress(fileAddress);
        res.setPillarID(pillarID);

        return res;
    }
}
