package org.bitrepository.protocol.pillarselector;

import org.bitrepository.common.exceptions.UnableToFinishException;

public interface SinglePillarSelector {

    /** Return the ID of the pillar chosen by this selector.*/
    public String getIDForSelectedPillar();

    /** If finished return the topic for sending messages to the pillar chosen by this selector. 
     * If unfinished null is returned 
     */
    public String getDestinationForSelectedPillar();
    
    /**
     * Returns true if all the need information to select a pillar has been processed. <p>
     * 
     * Note that a pillar might have been selected before finished, but the selection might change until the selector 
     * has finished.
     * @throws UnableToFinishException Indicates that the selector was unable to find a pillar. 
     */
    public boolean isFinished() throws UnableToFinishException;
}
