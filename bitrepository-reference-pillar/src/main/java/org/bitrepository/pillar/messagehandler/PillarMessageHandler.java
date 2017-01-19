/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id$
 * $HeadURL$
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
package org.bitrepository.pillar.messagehandler;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.FileIDValidator;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.handler.AbstractRequestHandler;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract level for message handling for both types of pillar.
 * @param <T> The type of request message to handle.
 */
public abstract class PillarMessageHandler<T extends MessageRequest> extends AbstractRequestHandler<T> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The response value for a positive identification.*/
    protected static final String RESPONSE_FOR_POSITIVE_IDENTIFICATION = "Operation acknowledged and accepted.";
    
    /** The context for the message handler.*/
    protected final MessageHandlerContext context;
    /** The file id validator for validating the file id.*/
    private final FileIDValidator fileIDValidator;
    /** The model for the pillar.*/
    private final StorageModel pillarModel;

    /**
     * @param context       The context to use for message handling.
     * @param fileInfoStore The storage model for the pillar.
     */
    protected PillarMessageHandler(MessageHandlerContext context, StorageModel fileInfoStore) {
        super(context);
        this.context = context;
        this.fileIDValidator = new FileIDValidator(context.getSettings());
        this.pillarModel = fileInfoStore;
    }

    /**
     * @return The cache for this message handler.
     */
    protected StorageModel getPillarModel() {
        return pillarModel;
    }
    
    /**
     * @return The settings for this message handler.
     */
    protected Settings getSettings() {
        return context.getSettings();
    }
    
    /**
     * @return The audit trail manager for this message sender.
     */
    protected AuditTrailManager getAuditManager() {
        return context.getAuditTrailManager();
    }
    
    /**
     * Validates that it is the correct pillar id.
     * @param pillarID The pillar id.
     */
    protected void validatePillarId(String pillarID) {
        if(!pillarID.equals(getSettings().getComponentID())) {
            throw new IllegalArgumentException("The message had a wrong PillarId: "
                    + "Expected '" + getSettings().getComponentID()
                    + "' but was '" + pillarID + "'.");
        }
    }
    
    /**
     * Verifies that we have a given fileID on a given collection, otherwise throws an exception. 
     * If no specific FileID is given (the AllFileIDs), then it is ignored.
     * @param fileIDs The FileIDs containing the fileID which we should have.  
     * @param collectionID The collection which should contain the requested file.
     * @throws RequestHandlerException If a specific file is given, but we do not have it.
     */
    protected void verifyFileIDExistence(FileIDs fileIDs, String collectionID) throws RequestHandlerException {
        if(fileIDs.getFileID() == null) {
            return;
        }

        if(!getPillarModel().hasFileID(fileIDs.getFileID(), collectionID)) {
            log.warn("The following file is missing '" + fileIDs.getFileID() + "' at collection '" + collectionID 
                    + "'.");
            throw new InvalidMessageException(ResponseCode.FILE_NOT_FOUND_FAILURE, "File not found.", 
                    collectionID);
        }
    }
    
    /**
     * Uses the FileIDValidator to validate the format of a given file id.
     * @param fileID The id to validate.
     * @throws RequestHandlerException If the id of the file was invalid.
     */
    protected void validateFileIDFormat(String fileID) throws RequestHandlerException {
        fileIDValidator.validateFileID(fileID);
    }
}
