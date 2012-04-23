package org.bitrepository.monitoringservice;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.common.utils.CalendarUtils;

/**
 * Class to encapsulate the status of a component.  
 */
public class ComponentStatus {

    /**
     * Enumeration of possible status states for a component.  
     */
    public enum ComponentStatusCode {
        UNKNOWN,
        OK,
        WARNING,
        ERROR,
        UNRESPONSIVE;

        public String value() {
            return name();
        }

        public static ComponentStatusCode fromValue(String v) {
            return valueOf(v);
        }
    }
    
    private int numberOfMissingReplys;
    private ComponentStatusCode status;
    private XMLGregorianCalendar lastReply;
    private String info;
    
    /**
     * Constructor 
     */
    public ComponentStatus() {
        numberOfMissingReplys = 0;
        status = ComponentStatusCode.UNKNOWN;
        lastReply = CalendarUtils.getEpoch();
        info = "No status requested yet.";
    }
    
    /**
     * Update the status of a component 
     */
    public void updateStatus(ResultingStatus resultingStatus) {
        numberOfMissingReplys = 0;
        status = ComponentStatusCode.fromValue(resultingStatus.getStatusInfo().getStatusCode().toString());
        lastReply = resultingStatus.getStatusTimestamp();
        info = resultingStatus.getStatusInfo().getStatusText();
    }

    public void updateReplys() {
        numberOfMissingReplys++;
    }
    
    public void markAsUnresponsive() {
        status = ComponentStatusCode.UNRESPONSIVE;
    }
    
    public int getNumberOfMissingReplys() {
        return numberOfMissingReplys;
    }

    public ComponentStatusCode getStatus() {
        return status;
    }

    public XMLGregorianCalendar getLastReply() {
        return lastReply;
    }

    public String getInfo() {
        return info;
    }
    
    
}
