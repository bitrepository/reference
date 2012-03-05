/*
 * #%L
 * bitrepository-reference-pillar
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
package org.bitrepository.pillar.checksumpillar.messagehandler;

import java.math.BigInteger;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.checksumpillar.cache.ChecksumCache;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Abstract level for message handling. 
 */
public abstract class ChecksumPillarMessageHandler<T> {

    /** The constant for the VERSION of the messages.*/
    protected static final BigInteger VERSION = BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION);
    /** The constant for the MIN_VERSION of the messages.*/
    protected static final BigInteger MIN_VERSION = BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION);
    
    /** The classpath to the 'xsd'.*/
    protected static final String XSD_CLASSPATH = "xsd/";
    /** The name of the XSD containing the BitRepositoryData elements. */
    protected static final String XSD_BR_DATA = "BitRepositoryData.xsd";
    
    /** The response value for a positive identification.*/
    protected static final String RESPONSE_FOR_POSITIVE_IDENTIFICATION = "Operation acknowledged and accepted.";
    
    /** The dispatcher for sending alarm messages.*/
    protected final AlarmDispatcher alarmDispatcher;
    /** The settings for this setup.*/
    protected final Settings settings;
    /** The messagebus for communication.*/ 
    protected final MessageBus messagebus;
    /** The reference checksum cache.*/
    protected final ChecksumCache cache;
    /** */
    protected final ChecksumSpecTYPE checksumType;
    
    /**
     * Constructor. 
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param refCache The cache for the checksum data.
     */
    protected ChecksumPillarMessageHandler(Settings settings, MessageBus messageBus, AlarmDispatcher alarmDispatcher, 
            ChecksumCache refCache) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(alarmDispatcher, "alarmDispatcher");
        ArgumentValidator.checkNotNull(refCache, "ChecksumCache refCache");

        this.settings = settings;
        this.messagebus = messageBus;
        this.alarmDispatcher = alarmDispatcher;
        this.cache = refCache;
        this.checksumType = new ChecksumSpecTYPE();
    }
    
    /** 
     * The method for handling the message connected to the specific handler.
     * @param message The message to handle.
     */
    abstract void handleMessage(T message);
    
    /**
     * Validates the checksum specification.
     * A null as checksum argument is ignored.
     * @param csSpec The checksum specification to validate. 
     */
    protected void validateChecksumSpec(ChecksumSpecTYPE csSpec) {
        if(csSpec == null) {
            return;
        }
        
        if(!(checksumType.getChecksumType() == csSpec.getChecksumType()) 
                || !(new String(checksumType.getChecksumSalt()) == new String(csSpec.getChecksumSalt()))) {
            throw new IllegalArgumentException("Cannot handle the checksum specification '" + csSpec + "'."
                    + "This checksum pillar can only handle '" + checksumType + "'");
        }
    }
    
    /**
     * Validates that it is the correct BitrepositoryCollectionId.
     * @param bitrepositoryCollectionId The collection id to validate.
     */
    protected void validateBitrepositoryCollectionId(String bitrepositoryCollectionId) {
        if(!bitrepositoryCollectionId.equals(settings.getCollectionID())) {
            throw new IllegalArgumentException("The message had a wrong BitRepositoryIdCollection: "
                    + "Expected '" + settings.getCollectionID() + "' but was '" 
                    + bitrepositoryCollectionId + "'.");
        }
    }

    /**
     * Validates that it is the correct pillar id.
     * @param pillarId The pillar id.
     */
    protected void validatePillarId(String pillarId) {
        if(!pillarId.equals(settings.getReferenceSettings().getPillarSettings().getPillarID())) {
            throw new IllegalArgumentException("The message had a wrong PillarId: "
                    + "Expected '" + settings.getReferenceSettings().getPillarSettings().getPillarID() + "' but was '" 
                    + pillarId + "'.");
        }
    }
}
