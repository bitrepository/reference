/*
 * #%L
 * Bitrepository Webclient
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository;

import java.util.ArrayList;
import java.util.List;

import javax.jms.JMSException;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.settings.repositorysettings.Collection;
import org.bitrepository.settings.repositorysettings.RepositorySettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicClient {
    private Settings settings;
    private final Logger log = LoggerFactory.getLogger(getClass());

    public BasicClient(Settings settings) {
        log.debug("---- Basic client instanciating ----");
        this.settings = settings;
        log.debug("---- Basic client instantiated ----");

    }
    
    public List<String> getCollectionIDs() {
        List<String> collections = new ArrayList<String>();
        for(Collection collection : settings.getRepositorySettings().getCollections().getCollection()) {
            collections.add(collection.getID());
        }
        return collections;
    }
    
    public String getCollectionName(String collectionID) {
        String name = null;
        
        for(Collection collection : settings.getRepositorySettings().getCollections().getCollection()) {
            if(collection.getID().equals(collectionID)) {
                if(collection.isSetName()) {
                    name = collection.getName();    
                } else {
                    name = collection.getID();
                }
                break;
            }
        }
        
        return name;
    }

    public void shutdown() {
        try {
            MessageBusManager.getMessageBus().close();
        } catch (JMSException e) {
            log.warn("Failed to shutdown message bus cleanly, " + e.getMessage());
        }
    }

    public String getSettingsSummary() {
        StringBuilder sb = new StringBuilder();
        RepositorySettings repositorySettings = settings.getRepositorySettings();
        sb.append("Collections:<br>");
        for (Collection collection: settings.getCollections()) {
            sb.append("<i>ID:" + collection.getID());
            sb.append("<i>Pillars:");
            for (String pillarID: collection.getPillarIDs().getPillarID()) {
                sb.append("&nbsp; " + pillarID);
            }
        }
        sb.append("</i><br>");
        sb.append("</i>");
        sb.append("Messagebus URL: <br> &nbsp;&nbsp;&nbsp; <i>"); 
        sb.append(repositorySettings.getProtocolSettings().getMessageBusConfiguration().getURL() + "</i><br>");
        return sb.toString();
    }

    public List<String> getPillarList() {
        return settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID();
    }
}
