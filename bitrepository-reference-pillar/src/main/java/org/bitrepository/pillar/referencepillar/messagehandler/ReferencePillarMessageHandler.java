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
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.common.PillarMessageHandler;
import org.bitrepository.pillar.referencepillar.archive.CollectionArchiveManager;
import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * Abstract level for message handling. 
 */
public abstract class ReferencePillarMessageHandler<T> extends PillarMessageHandler<T> {
    /** The manager of the archives.*/
    private final CollectionArchiveManager archives;
    /** The manager of checksums.*/
    private final ReferenceChecksumManager csManager;
    
    /**
     * @param context The context for the pillar.
     * @param archivesManager The manager of the archives.
     * @param csManager The checksum manager for the pillar.
     */
    protected ReferencePillarMessageHandler(MessageHandlerContext context, CollectionArchiveManager archivesManager,
            ReferenceChecksumManager csManager) {
        super(context);
        ArgumentValidator.checkNotNull(archivesManager, "CollectionArchiveManager archivesManager");
        ArgumentValidator.checkNotNull(csManager, "ReferenceChecksumManager csManager");

        this.archives = archivesManager;
        this.csManager = csManager;
    }
    
    /**
     * @return The cache for this message handler.
     */
    protected CollectionArchiveManager getArchives() {
        return archives;
    }
    
    /**
     * @return The checksum manager for this handler.
     */
    protected ReferenceChecksumManager getCsManager() {
        return csManager;
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
            fri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
            fri.setResponseText(e.toString());
            throw new InvalidMessageException(fri, e);
        }
    }
}
