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
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.settings.Settings;

public class PutFileMessageFactory extends PillarTestMessageFactory {
    private final String pillarID;
    
    public PutFileMessageFactory(Settings clientSettings, String pillarID, String pillarDestination) {
        super(clientSettings, pillarDestination);
        this.pillarID = pillarID;
    }
    
    public IdentifyPillarsForPutFileRequest createIdentifyPillarsForPutFileRequest(String fileId, Long fileSize) {
        IdentifyPillarsForPutFileRequest res = new IdentifyPillarsForPutFileRequest();
        initializeIdentifyRequest(res);
        res.setFileID(fileId);
        if(fileSize != null) {
            res.setFileSize(BigInteger.valueOf(fileSize));
        }
        
        return res;
    }

    public PutFileRequest createPutFileRequest(
            ChecksumDataForFileTYPE checksumDataForFile,
            ChecksumSpecTYPE csReturnSpec,
            String url, String fileId, Long fileSize) {
        PutFileRequest res = new PutFileRequest();
        initializeOperationRequest(res);
        res.setChecksumDataForNewFile(checksumDataForFile);
        res.setChecksumRequestForNewFile(csReturnSpec);
        res.setFileAddress(url);
        res.setFileID(fileId);
        if(fileSize != null) {
            res.setFileSize(BigInteger.valueOf(fileSize));
        }
        res.setPillarID(pillarID);
        
        return res;
    }
}
