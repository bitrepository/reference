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

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.AlarmDispatcher;
import org.bitrepository.pillar.AuditTrailManager;
import org.bitrepository.pillar.exceptions.InvalidMessageException;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.utils.ChecksumUtils;

/**
 * Abstract level for message handling. 
 */
public abstract class ReferencePillarMessageHandler<T> {

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
    private final AlarmDispatcher alarmDispatcher;
    /** The settings for this setup.*/
    private final Settings settings;
    /** The messagebus for communication.*/ 
    private final MessageBus messagebus;
    /** The reference archive.*/
    private final ReferenceArchive archive;
    /** The manager of audit trails.*/
    private final AuditTrailManager auditManager;
    
    /**
     * Constructor. 
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    protected ReferencePillarMessageHandler(Settings settings, MessageBus messageBus, AlarmDispatcher alarmDispatcher, 
            ReferenceArchive referenceArchive, AuditTrailManager auditManager) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(alarmDispatcher, "alarmDispatcher");
        ArgumentValidator.checkNotNull(referenceArchive, "referenceArchive");
        ArgumentValidator.checkNotNull(auditManager, "auditManager");

        this.settings = settings;
        this.messagebus = messageBus;
        this.alarmDispatcher = alarmDispatcher;
        this.archive = referenceArchive;
        this.auditManager = auditManager;
    }
    
    /**
     * @return The alarmDispatcher for this message handler.
     */
    protected AlarmDispatcher getAlarmDispatcher() {
        return alarmDispatcher;
    }

    /**
     * @return The cache for this message handler.
     */
    protected ReferenceArchive getArchive() {
        return archive;
    }

    /**
     * @return The messagebus for this message handler.
     */
    protected MessageBus getMessageBus() {
        return messagebus;
    }

    /**
     * @return The settings for this message handler.
     */
    protected Settings getSettings() {
        return settings;
    }
    
    /**
     * @return The audit trail manager for this message sender.
     */
    protected AuditTrailManager getAuditManager() {
        return auditManager;
    }
    
    /** 
     * The method for handling the message connected to the specific handler.
     * @param message The message to handle.
     */
    abstract void handleMessage(T message);
    
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
    
    /**
     * Validates a given checksum calculation.
     * Ignores if the checksum type is null.
     * @param checksumSpec The checksum specification to validate.
     */
    protected void validateChecksumSpecification(ChecksumSpecTYPE checksumSpec) {
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
