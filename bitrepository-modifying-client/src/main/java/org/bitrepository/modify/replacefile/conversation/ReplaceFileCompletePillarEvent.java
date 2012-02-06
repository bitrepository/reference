/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: ChecksumsCompletePillarEvent.java 548 2011-11-21 16:43:43Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/getchecksums/conversation/ChecksumsCompletePillarEvent.java $
 * %%
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
package org.bitrepository.modify.replacefile.conversation;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;

/**
* Contains the result of a ReplaceFile request sent to a single pillar.
*/
public class ReplaceFileCompletePillarEvent extends PillarOperationEvent {
    /** @see #getChecksumForDeletedFile(). */
    private final ChecksumDataForFileTYPE deletedFileChecksum;
    /** @see #getChecksumForNewFile(). */
    private final ChecksumDataForFileTYPE newFileChecksum;
    
    /**
     * @param deletedFile The results of the checksum request for the deleted file from the pillar.
     * @param newFile The results of the checksum request for the new file from the pillar.
     * @param pillarID The pillar which generated the result
     * @param info Additional information.
     */
    public ReplaceFileCompletePillarEvent(ChecksumDataForFileTYPE deletedFile, ChecksumDataForFileTYPE newFile, 
            String pillarID, String info) {
        super(OperationEventType.COMPONENT_COMPLETE, info, pillarID);
        this.newFileChecksum = newFile;
        this.deletedFileChecksum = deletedFile;
    }

    /** 
     * @return The checksum result for the deleted file from a single pillar. 
     */
    public ChecksumDataForFileTYPE getChecksumForDeletedFile() {
        return deletedFileChecksum;
    }
    
    /** 
     * @return The checksum result for the new file from a single pillar. 
     */
    public ChecksumDataForFileTYPE getChecksumForNewFile() {
        return newFileChecksum;
    }
    
    @Override
    public String toString() {
        StringBuffer res = new StringBuffer();
        res.append(super.toString());
        res.append(", checksum for new file: '" + newFileChecksum + "'");
        res.append(", checksum for deleted file: '" + deletedFileChecksum + "'");
        return  res.toString();
    }
}
