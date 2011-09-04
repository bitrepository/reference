/*
 * #%L
 * bitrepository-common
 * 
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
package org.bitrepository.protocol.bitrepositorycollection;

import java.util.Arrays;

import org.bitrepository.collection.settings.standardsettings.StandardClientSettings;

/**
 * The concrete implementation of the ClientSettings interface. 
 * 
 * Contains set operations for all attributes, eg. it is mutable.
 */
public class MutableClientSettings 
extends MutableCollectionSettings 
implements ClientSettings {
 
    private StandardClientSettings standardClientSettings;
    @Override
    public StandardClientSettings getStandardClientSettings() {
        return standardClientSettings;
    }
    /** @see #getStandardSettings() */
    public void setStandardClientSettings(StandardClientSettings standardClientSettings) {
        this.standardClientSettings = standardClientSettings;
    }
    
    /** @see #getClientTopicId() */
    private String clientTopicId;
    @Override
    public String getClientTopicID() {
        return clientTopicId;
    }
    /** @see #getClientTopicId() */
    public void setClientTopicID(String clientTopicId) {
        this.clientTopicId = clientTopicId;
    }

    /** @see #getPillarIDs() */
    private String[] pillarIDs;
    @Override
    public String[] getPillarIDs() {
        return pillarIDs;
    }
    /** @see #getPillarIDs() */
    public void setPillarIDs(String[] pillarIDs) {
        this.pillarIDs = pillarIDs;
    }

    /** @see #getLocalFileStorage() */
    private String fileStorage;
    @Override
    public String getLocalFileStorage() {
        return fileStorage;
    }
    /** @see #getLocalFileStorage() */
    public void setLocalFileStorage(String fileStorage) {
        this.fileStorage = fileStorage;
    }

    /** Default value is 1 day */
    private long conversationTimeout = 1000*60*60*24;
    @Override
    public long getConversationTimeout() {
        return conversationTimeout;
    }
    /** @see #getConversationTimeout() */
    public void setConversationTimeout(int conversationTimeout) {
        this.conversationTimeout = conversationTimeout;
    }

    /** Default value is 30 seconds */
    private long identifyPillarsTimeout = 1000*30;
    @Override
    public long getIdentifyPillarsTimeout() {
        return identifyPillarsTimeout;
    }
    /** @see #getIdentifyPillarsTimeout() */
    public void setIdentifyPillarsTimeout(int identifyPillarsTimeout) {
        this.identifyPillarsTimeout = identifyPillarsTimeout;
    }

    /** Default value is 5 minutes */
    private long mediatorCleanInterval = 1000*60*5;
    @Override
    public long getMediatorCleanInterval() {
        return mediatorCleanInterval;
    }
    /** @see #getMediatorCleanInterval() */
    public void setMediatorCleanInterval(int mediatorCleanInterval) {
        this.mediatorCleanInterval = mediatorCleanInterval;
    }

    @Override
    public String toString() {
        return "MutableClientSettings [standardClientSettings="
                + standardClientSettings + ", clientTopicId=" + clientTopicId
                + ", pillarIDs=" + Arrays.toString(pillarIDs)
                + ", fileStorage=" + fileStorage + ", conversationTimeout="
                + conversationTimeout + ", identifyPillarsTimeout="
                + identifyPillarsTimeout + ", mediatorCleanInterval="
                + mediatorCleanInterval + ", getStandardSettings()="
                + getStandardSettings() + "]";
    }
}
