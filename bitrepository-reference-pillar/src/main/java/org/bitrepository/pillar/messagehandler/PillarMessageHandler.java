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

import java.security.NoSuchAlgorithmException;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileIDValidator;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.FileInfoStore;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.handler.AbstractRequestHandler;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * Abstract level for message handling for both types of pillar.
 */
public abstract class PillarMessageHandler<T> extends AbstractRequestHandler<T> {
    /** The response value for a positive identification.*/
    protected static final String RESPONSE_FOR_POSITIVE_IDENTIFICATION = "Operation acknowledged and accepted.";
    
    /** The context for the message handler.*/
    protected final MessageHandlerContext context;
    /** The file id validator for validating the file id.*/
    private final FileIDValidator fileIdValidator;
    /** The interface to data storage.*/
    private final FileInfoStore fileInfoStore;
    
    /**
     * @param context The context to use for message handling.
     */
    protected PillarMessageHandler(MessageHandlerContext context, FileInfoStore fileInfoStore) {
        super(context);
        this.context = context;
        this.fileIdValidator = new FileIDValidator(context.getSettings());
        this.fileInfoStore = fileInfoStore;
    }

    /**
     * @return The cache for this message handler.
     */
    protected FileInfoStore getFileInfoStore() {
        return fileInfoStore;
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
     * @param pillarId The pillar id.
     */
    protected void validatePillarId(String pillarId) {
        if(!pillarId.equals(getSettings().getComponentID())) {
            throw new IllegalArgumentException("The message had a wrong PillarId: "
                    + "Expected '" + getSettings().getComponentID()
                    + "' but was '" + pillarId + "'.");
        }
    }

    /**
     * Validates a given checksum calculation.
     * Ignores if the checksum type is null.
     * @param checksumSpec The checksum specification to validate.
     * @param collectionId The id of the collection which this is relevant for.
     */
    protected void validateChecksumSpecification(ChecksumSpecTYPE checksumSpec, String collectionId) 
            throws RequestHandlerException {
        if(checksumSpec == null) {
            return;
        }
        
        try {
            ChecksumUtils.verifyAlgorithm(checksumSpec);
        } catch (NoSuchAlgorithmException e) {
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
            fri.setResponseText(e.toString());
            throw new InvalidMessageException(fri, collectionId, e);
        }
    }

    
    /**
     * Uses the FileIDValidator to validate a given file id.
     * @param fileId The id to validate.
     * @throws RequestHandlerException If the id of the file was invalid.
     */
    protected void validateFileID(String fileId) throws RequestHandlerException {
        fileIdValidator.validateFileID(fileId);
    }
}
