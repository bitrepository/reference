package org.bitrepository.monitoringservice.status;

/**
 * Enumeration of possible status states for a component.  
 */
public enum ComponentStatusCode {
    /** If no status is known for the given component.*/
    UNKNOWN,
    /** If the component is fine.*/
    OK,
    /** If the component has reported a warning.*/
    WARNING,
    /** If the component has reported an error.*/
    ERROR,
    /** If the component has become unresponsive.*/
    UNRESPONSIVE;
    
    /**
     * @return The value of the status.
     */
    public String value() {
        return name();
    }
}
