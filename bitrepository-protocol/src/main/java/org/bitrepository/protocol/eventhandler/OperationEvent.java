package org.bitrepository.protocol.eventhandler;

/**
 * Container for information regarding events occurring during an operation on the Bit Repository.  
 */
public interface OperationEvent {
    /**
     * Defines the different types of events that can be received. These are:<ol>
     * <li>PillarIdentified: An identify response has been received from a pillar.
     * <li>PillarSelected: Enough responses has now been received from pillar to select a pillar to perform the 
     * operation on.
     * <li>RequestSent: A request for the operation has ben sent to the relevant pillar(s). 
     * <li>Progress: In case of longer operations (e.g. requiring file transfers) progress information might be 
     * received from the pillars.
     * <li>Complete: The operation has finished
     * </ol>
     * The following error types exist:<ol>
     * <li>Failed: A general failure occurred during the operation
     * <li>NoPillarFound: No relevant response was received before a timeout occurred.
     * <li>TimeOut: The operation did't finish before a timeout occurred.
     * </ol>
     */
    public enum OperationEventType {
        PillarIdentified,
        PillarSelected,
        RequestSent,
        Progress,
        Complete, 
        Failed,
        NoPillarFound,
        TimeOut
    }
    
    /**
     * A string representation of what has happened
     * @return
     */
    String getInfo();
    
    /**
     * Used to get the type of event.
     * @return A <code>OperationEventType</code> categorizing this event.
     */
    OperationEventType getType();
}
