package org.bitrepository.common.webobjects;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class StatisticsDataSize {

    private Long dateMillis; 
    private String dateString;
    private Long dataSize;
    
    public StatisticsDataSize() { }

    public Long getDateMillis() {
        return dateMillis;
    }

    public void setDateMillis(Long dateMillis) {
        this.dateMillis = dateMillis;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public Long getDataSize() {
        return dataSize;
    }

    public void setDataSize(Long dataSize) {
        this.dataSize = dataSize;
    }
    
    
}
