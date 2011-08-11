/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AccessMessageHandler.java 249 2011-08-02 11:05:51Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-integration/src/main/java/org/bitrepository/pillar/messagehandler/AccessMessageHandler.java $
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

/**
 * Abstract level for message handling. 
 */
public abstract class PillarMessageHandler<T> {

    /** The mediator.*/
    protected final PillarMediator mediator;
    
    /**
     * Constructor. Only accessible by inheritors of this interface.
     * @param mediator
     */
    protected PillarMessageHandler(PillarMediator mediator) {
        this.mediator = mediator;
    }
    
    abstract void handleMessage(T message);
    
    /**
     * Validates that it is the correct BitrepositoryCollectionId.
     * @param bitrepositoryCollectionId The collection id to validate.
     */
    protected void validateBitrepositoryCollectionId(String bitrepositoryCollectionId) {
        if(!bitrepositoryCollectionId.equals(mediator.settings.getBitRepositoryCollectionID())) {
            throw new IllegalArgumentException("The message had a wrong BitRepositoryIdCollection: "
                    + "Expected '" + mediator.settings.getBitRepositoryCollectionID() + "' but was '" 
                    + bitrepositoryCollectionId + "'.");
        }
    }

    /**
     * Validates that it is the correct pillar id.
     * @param pillarId The pillar id.
     */
    protected void validatePillarId(String pillarId) {
        if(!pillarId.equals(mediator.settings.getPillarId())) {
            throw new IllegalArgumentException("The message had a wrong PillarId: "
                    + "Expected '" + mediator.settings.getPillarId() + "' but was '" 
                    + pillarId + "'.");
        }
    }

}
