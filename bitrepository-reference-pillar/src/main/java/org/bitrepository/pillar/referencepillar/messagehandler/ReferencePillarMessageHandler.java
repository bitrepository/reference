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
package org.bitrepository.pillar.referencepillar.messagehandler;

import java.security.NoSuchAlgorithmException;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.common.PillarMessageHandler;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.utils.ChecksumUtils;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * Abstract level for message handling. 
 */
public abstract class ReferencePillarMessageHandler<T> extends PillarMessageHandler<T> {
    /** The reference archive.*/
    private final ReferenceArchive archive;
    
    /**
     * Constructor. 
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    protected ReferencePillarMessageHandler(PillarContext context, ReferenceArchive referenceArchive) {
        super(context);
        ArgumentValidator.checkNotNull(referenceArchive, "referenceArchive");

        this.archive = referenceArchive;
    }
    
    /**
     * @return The cache for this message handler.
     */
    protected ReferenceArchive getArchive() {
        return archive;
    }
    
    /**
     * Validates a given checksum calculation.
     * Ignores if the checksum type is null.
     * @param checksumSpec The checksum specification to validate.
     */
    protected void validateChecksumSpecification(ChecksumSpecTYPE checksumSpec) throws RequestHandlerException {
        if(checksumSpec == null) {
            return;
        }
        
        try {
            ChecksumUtils.verifyAlgorithm(checksumSpec);
        } catch (NoSuchAlgorithmException e) {
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FAILURE);
            fri.setResponseText(e.getMessage());
            throw new InvalidMessageException(fri, e);
        }
    }
}
