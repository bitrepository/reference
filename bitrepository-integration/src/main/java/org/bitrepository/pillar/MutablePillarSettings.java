/*
 * #%L
 * Bitrepository Integration
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
package org.bitrepository.pillar;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;

/**
 * Instantiable class for the PillarSettings.
 */
public class MutablePillarSettings implements PillarSettings {

    public MutablePillarSettings() {
        super();
    }
    
    public MutablePillarSettings(PillarSettings settings) {
        this.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        this.setBitRepositoryCollectionTopicID(settings.getBitRepositoryCollectionTopicID());
        this.setFileDirName(settings.getFileDirName());
        this.setLocalQueue(settings.getLocalQueue());
        this.setMessageBusConfiguration(settings.getMessageBusConfiguration());
        this.setPillarId(settings.getPillarId());
        this.setTimeToDownloadMeasure(settings.getTimeToDownloadMeasure());
        this.setTimeToDownloadValue(settings.getTimeToDownloadValue());
        this.setTimeToUploadMeasure(settings.getTimeToUploadMeasure());
        this.setTimeToUploadValue(settings.getTimeToUploadValue());
    }
    
    /** @see PillarSettings#getPillarId() */
    private String pillarId;
    @Override
    public String getPillarId() {
        return pillarId;
    }
    /** @see PillarSettings#getPillarId() */
    public void setPillarId(String pid) {
        this.pillarId = pid;
    }

    /** @see PillarSettings#getFileDirName() */
    private String filedirName;
    @Override
    public String getFileDirName() {
        return filedirName;
    }
    /** @see PillarSettings#getFileDirName() */
    public void setFileDirName(String fname) {
        this.filedirName = fname;
    }

    /** @see PillarSettings#getLocalQueue() */
    private String localQueue;
    @Override
    public String getLocalQueue() {
        return localQueue;
    }
    /** @see PillarSettings#getLocalQueue() */
    public void setLocalQueue(String lqueue) {
        this.localQueue = lqueue;
    }

    /** @see PillarSettings#getTimeToUploadValue() */
    private Long timeToUploadValue;
    @Override
    public Long getTimeToUploadValue() {
        return timeToUploadValue;
    }
    /** @see PillarSettings#getTimeToUploadValue() */
    public void setTimeToUploadValue(Long time) {
        this.timeToUploadValue = time;
    }

    /** @see PillarSettings#getTimeToUploadMeasure() */
    private TimeMeasureTYPE.TimeMeasureUnit timeToUploadMeasure;
    @Override
    public TimeMeasureTYPE.TimeMeasureUnit getTimeToUploadMeasure() {
        return timeToUploadMeasure;
    }
    /** @see PillarSettings#getTimeToUploadMeasure() */
    public void setTimeToUploadMeasure(TimeMeasureTYPE.TimeMeasureUnit measure) {
        this.timeToUploadMeasure = measure;
    }
    
    /** @see PillarSettings#getTimeToDownloadValue() */
    private Long timeToDownloadValue;
    @Override
    public Long getTimeToDownloadValue() {
        return timeToDownloadValue;
    }
    /** @see PillarSettings#getTimeToDownloadValue() */
    public void setTimeToDownloadValue(Long time) {
        this.timeToDownloadValue = time;
    }

    /** @see PillarSettings#getTimeToDownloadMeasure() */
    private TimeMeasureTYPE.TimeMeasureUnit timeToDownloadMeasure;
    @Override
    public TimeMeasureTYPE.TimeMeasureUnit getTimeToDownloadMeasure() {
        return timeToDownloadMeasure;
    }
    /** @see PillarSettings#getTimeToDownloadMeasure() */
    public void setTimeToDownloadMeasure(TimeMeasureTYPE.TimeMeasureUnit measure) {
        this.timeToDownloadMeasure = measure;
    }

    /** @see PillarSettings#getBitRepositoryCollectionID() */
    private String bitRepositoryCollectionID;
    @Override
    public String getBitRepositoryCollectionID() {
        return bitRepositoryCollectionID;
    }
    /** @see PillarSettings#getBitRepositoryCollectionID() */
    public void setBitRepositoryCollectionID(String brCollectinID) {
        this.bitRepositoryCollectionID = brCollectinID;
    }
    
    /** @see PillarSettings#getMessageBusConfiguration() */
    private MessageBusConfigurations messagebusConfigs;
    @Override
    public MessageBusConfigurations getMessageBusConfiguration() {
        return messagebusConfigs;
    }
    /** @see PillarSettings#getMessageBusConfiguration() */
    public void setMessageBusConfiguration(MessageBusConfigurations messagebusConfigurations) {
        this.messagebusConfigs = messagebusConfigurations;
    }

    /** @see PillarSettings#getBitRepositoryCollectionTopicID() */
    private String bitrepositoryCollectionTopicID;
    @Override
    public String getBitRepositoryCollectionTopicID() {
        return bitrepositoryCollectionTopicID;
    }
    /** @see PillarSettings#getBitRepositoryCollectionTopicID() */
    public void setBitRepositoryCollectionTopicID(String brcTopicID) {
        this.bitrepositoryCollectionTopicID = brcTopicID;
    }

}
