package org.bitrepository.monitoringservice.webservice;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.monitoringservice.status.ComponentStatus;

@XmlRootElement
public class WebStatus {

    private String componentID;
    private String status;
    private String info;
    private String timeStamp;
    
    public WebStatus() {}
    
    public WebStatus(String componentID, ComponentStatus status) {
        this.componentID = componentID;
        this.status = status.getStatus().toString();
        this.info = status.getInfo();
        XMLGregorianCalendar cal = status.getLastReply();
        if(cal != null) {
            timeStamp = TimeUtils.shortDate(cal);
        } else {
            timeStamp = "N/A";    
        }
    }
    
    @XmlElement(name = "componentID")
    public String getComponentID() {
        return componentID;
    }
    
    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }
    
    @XmlElement(name = "status")
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    @XmlElement(name = "info")
    public String getInfo() {
        return info;
    }
    
    public void setInfo(String info) {
        this.info = info;
    }
    
    @XmlElement(name = "timeStamp")
    public String getTimeStamp() {
        return timeStamp;
    }
    
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
    
    
}
