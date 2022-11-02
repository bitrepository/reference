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
import org.bitrepository.bitrepositoryelements.ResponseInfo;
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
 *
 * @param <T> The type of request message to handle.
 */
public abstract class PillarMessageHandler<T extends MessageRequest> extends AbstractRequestHandler<T> {
    private final Logger log = LoggerFactory.getLogger(getClass());
    protected static final String RESPONSE_FOR_POSITIVE_IDENTIFICATION = "Operation acknowledged and accepted.";
    protected final MessageHandlerContext context;
    private final FileIDValidator fileIDValidator;
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
     *
     * @param pillarID The pillar id.
     */
    protected void validatePillarID(String pillarID) {
        if (!pillarID.equals(getSettings().getComponentID())) {
            throw new IllegalArgumentException(
                    "The message had a wrong PillarId: " + "Expected '" + getSettings().getComponentID() + "' but was '" + pillarID + "'.");
        }
    }

    /**
     * Verifies that we have a given fileID on a given collection, otherwise throws an exception.
     * If no specific FileID is given (the AllFileIDs), then it is ignored.
     *
     * @param fileIDs      The FileIDs containing the fileID which we should have.
     * @param collectionID The collection which should contain the requested file.
     * @throws RequestHandlerException If a specific file is given, but we do not have it.
     */
    protected void verifyFileIDExistence(FileIDs fileIDs, String collectionID) throws RequestHandlerException {
        if (fileIDs.getFileID() == null) {
            return;
        }

        if (!getPillarModel().hasFileID(fileIDs.getFileID(), collectionID)) {
            log.warn("File '{}' is missing in collection '{}'", fileIDs.getFileID(), collectionID);
            throw new InvalidMessageException(ResponseCode.FILE_NOT_FOUND_FAILURE, "File not found.");
        }
    }

    /**
     * Uses the FileIDValidator to validate the format of a given file id.
     * This validation also catches the use of illegal starting characters that could refer to unauthorized directories.
     *
     * @param fileID The id to validate.
     * @throws RequestHandlerException If the id of the file was invalid.
     */
    protected void validateFileIDFormat(String fileID) throws RequestHandlerException {
        ResponseInfo ri = fileIDValidator.validateFileID(fileID);
        if (ri == null) {
            if (fileID.contains("/..") || fileID.contains("../")) {
                ri = new ResponseInfo();
                ri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
                ri.setResponseText("Invalid");
            }
        }

        if (ri != null) {
            throw new InvalidMessageException(ri);
        }
    }
}
