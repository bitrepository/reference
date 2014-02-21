package org.bitrepository.audittrails.webservice;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class CollectorInfo {

    String collectionID;
    String lastStart;
    String lastDuration; 
    String nextStart;
    long collectedAudits;
    
    public String getCollectionID() {
        return collectionID;
    }
    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }
    public String getLastStart() {
        return lastStart;
    }
    public void setLastStart(String lastStart) {
        this.lastStart = lastStart;
    }
    public String getNextStart() {
        return nextStart;
    }
    public void setNextStart(String nextStart) {
        this.nextStart = nextStart;
    }
    public String getLastDuration() {
        return lastDuration;
    }
    public void setLastDuration(String lastDuration) {
        this.lastDuration = lastDuration;
    }
    public long getCollectedAudits() {
        return collectedAudits;
    }
    public void setCollectedAudits(long collectedAudits) {
        this.collectedAudits = collectedAudits;
    }
    
    
}
