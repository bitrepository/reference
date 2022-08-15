package org.bitrepository.audittrails.webservice;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PreservationInfo {
    private String collectionID;
    private String lastStart;
    private String lastDuration;
    private String nextStart;
    private long preservedAuditCount;

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

    public String getLastDuration() {
        return lastDuration;
    }

    public void setLastDuration(String lastDuration) {
        this.lastDuration = lastDuration;
    }

    public String getNextStart() {
        return nextStart;
    }

    public void setNextStart(String nextStart) {
        this.nextStart = nextStart;
    }

    public long getPreservedAuditCount() {
        return preservedAuditCount;
    }

    public void setPreservedAuditCount(long preservedAuditCount) {
        this.preservedAuditCount = preservedAuditCount;
    }
}
