package org.bitrepository.pillar;

import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;

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
    
    private String pillarId;
    @Override
    public String getPillarId() {
        return pillarId;
    }
    public void setPillarId(String pid) {
        this.pillarId = pid;
    }

    private String filedirName;
    @Override
    public String getFileDirName() {
        return filedirName;
    }
    public void setFileDirName(String fname) {
        this.filedirName = fname;
    }

    private String localQueue;
    @Override
    public String getLocalQueue() {
        return localQueue;
    }
    public void setLocalQueue(String lqueue) {
        this.localQueue = lqueue;
    }

    private Long timeToUploadValue;
    @Override
    public Long getTimeToUploadValue() {
        return timeToUploadValue;
    }
    public void setTimeToUploadValue(Long time) {
        this.timeToUploadValue = time;
    }

    private String timeToUploadMeasure;
    @Override
    public String getTimeToUploadMeasure() {
        return timeToUploadMeasure;
    }
    public void setTimeToUploadMeasure(String measure) {
        this.timeToUploadMeasure = measure;
    }
    
    private Long timeToDownloadValue;
    @Override
    public Long getTimeToDownloadValue() {
        return timeToDownloadValue;
    }
    public void setTimeToDownloadValue(Long time) {
        this.timeToDownloadValue = time;
    }

    private String timeToDownloadMeasure;
    @Override
    public String getTimeToDownloadMeasure() {
        return timeToDownloadMeasure;
    }
    public void setTimeToDownloadMeasure(String measure) {
        this.timeToDownloadMeasure = measure;
    }

    private String bitRepositoryCollectionID;
    @Override
    public String getBitRepositoryCollectionID() {
        return bitRepositoryCollectionID;
    }
    public void setBitRepositoryCollectionID(String brCollectinID) {
        this.bitRepositoryCollectionID = brCollectinID;
    }
    
    private MessageBusConfigurations messagebusConfigs;
    @Override
    public MessageBusConfigurations getMessageBusConfiguration() {
        return messagebusConfigs;
    }
    public void setMessageBusConfiguration(MessageBusConfigurations messagebusConfigurations) {
        this.messagebusConfigs = messagebusConfigurations;
    }

    private String bitrepositoryCollectionTopicID;
    @Override
    public String getBitRepositoryCollectionTopicID() {
        return bitrepositoryCollectionTopicID;
    }
    public void setBitRepositoryCollectionTopicID(String brcTopicID) {
        this.bitrepositoryCollectionTopicID = brcTopicID;
    }

}
