package org.bitrepository.common.webobjects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatisticsCollectionSize {

    private String collectionID;
    private Long dataSize;
    
    public StatisticsCollectionSize() {}

    public String getCollectionID() {
        return collectionID;
    }

    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }

    public Long getDataSize() {
        return dataSize;
    }

    public void setDataSize(Long dataSize) {
        this.dataSize = dataSize;
    }
    
}
