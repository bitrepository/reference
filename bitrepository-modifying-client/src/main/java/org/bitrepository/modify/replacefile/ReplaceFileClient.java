/*
 * #%L
 * Bitmagasin modify client
 * 
 * $Id: DeleteFileClient.java 631 2011-12-13 17:56:54Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/deletefile/DeleteFileClient.java $
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
package org.bitrepository.modify.replacefile;

import java.net.URL;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.BitrepositoryClient;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.exceptions.OperationFailedException;

/**
 * Interface for the delete client.
 */
public interface ReplaceFileClient extends BitrepositoryClient {
    /**
     * Starts the conversation for replacing a file on a given pillar.
     * It is similar to performing the DeleteFile followed by the PutFile operations.
     * Since both of these takes two checksum arguments each, then this takes four!
     * @param fileId The id of the file to delete.
     * @param pillarId The id of the pillar, where the file should be replaced.
     * @param checksumForDeleteAtPillar The checksum of the file on the pillar which should be replaced. Used for 
     * validating at pillar-side.
     * @param checksumRequestedForDeletedFile [OPTIONAL] Request for calculation of the checksum of the file which 
     * should be replaced at the pillar. Used for client-side validation.
     * @param url The URL of the new file to replaced the old one.
     * @param sizeOfNewFile The size of the new file.
     * @param checksumForNewFileValidationAtPillar [OPTIONAL] The checksum of the new file. Used for pillar-side 
     * validation.
     * @param checksumRequestsForNewFile [OPTIONAL] Request for a checksum calculation of the new file. Used for 
     * client-side validation.
     * @param eventHandler [OPTIONAL] The handler which should receive notifications of the events occurring in 
     * connection with the pillar communication. This is allowed to be null.
     * In a good case scenario this will give the events: <br/> 
     * IdentifyPillarsRequestSent, PillarIdentified, PillarSelected, RequestSent, Progress, PillarComplete, Complete
     * @param auditTrailInformation The audit information for the given operation. E.g. who is behind the operation 
     * call.
     * @throws OperationFailedException If the operation cannot be instantiated.
     */
    void replaceFile(String fileId, String pillarId, ChecksumDataForFileTYPE checksumForDeleteAtPillar, 
            ChecksumSpecTYPE checksumRequestedForDeletedFile, URL url, long sizeOfNewFile, 
            ChecksumDataForFileTYPE checksumForNewFileValidationAtPillar, ChecksumSpecTYPE checksumRequestsForNewFile, 
            EventHandler eventHandler, String auditTrailInformation) throws OperationFailedException;

    /**
     * Starts the conversation for replacing a file on a given pillar.
     * It is similar to performing the DeleteFile followed by the PutFile operations.
     * Since both of these takes two checksum arguments each, then this takes four!
     * @param fileId The id of the file to delete.
     * @param checksumForDeleteAtPillar The checksum of the file on the pillar which should be replaced. Used for 
     * validating at pillar-side.
     * @param checksumRequestedForDeletedFile [OPTIONAL] Request for calculation of the checksum of the file which 
     * should be replaced at the pillar. Used for client-side validation.
     * @param url The URL of the new file to replaced the old one.
     * @param sizeOfNewFile The size of the new file.
     * @param checksumForNewFileValidationAtPillar [OPTIONAL] The checksum of the new file. Used for pillar-side 
     * validation.
     * @param checksumRequestsForNewFile [OPTIONAL] Request for a checksum calculation of the new file. Used for 
     * client-side validation.
     * @param eventHandler [OPTIONAL] The handler which should receive notifications of the events occurring in 
     * connection with the pillar communication. This is allowed to be null.
     * In a good case scenario this will give the events: <br/> 
     * IdentifyPillarsRequestSent, PillarIdentified, PillarSelected, RequestSent, Progress, PillarComplete, Complete
     * @param auditTrailInformation The audit information for the given operation. E.g. who is behind the operation 
     * call.
     * @throws OperationFailedException If the operation cannot be instantiated.
     */
    void replaceFileAtAllPillars(String fileId, ChecksumDataForFileTYPE checksumForDeleteAtPillar, 
            ChecksumSpecTYPE checksumRequestedForDeletedFile, URL url, long sizeOfNewFile, 
            ChecksumDataForFileTYPE checksumForNewFileValidationAtPillar, ChecksumSpecTYPE checksumRequestsForNewFile, 
            EventHandler eventHandler, String auditTrailInformation) throws OperationFailedException;

}
