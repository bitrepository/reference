package org.bitrepository.common.webobjects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatisticsPillarSize {

    private String pillarID;
    private Long dataSize;
    
    public StatisticsPillarSize() {}

    public String getPillarID() {
        return pillarID;
    }

    public void setPillarID(String pillarID) {
        this.pillarID = pillarID;
    }

    public Long getDataSize() {
        return dataSize;
    }

    public void setDataSize(Long dataSize) {
        this.dataSize = dataSize;
    }
    
}
