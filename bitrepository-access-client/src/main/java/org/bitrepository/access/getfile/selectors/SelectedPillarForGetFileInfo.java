package org.bitrepository.access.getfile.selectors;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.client.conversation.selector.SelectedPillarInfo;

/**
 * Container for information about a pillar which as been identified and are marked as 
 * selected for a GetFile request.
 */
public class SelectedPillarForGetFileInfo extends SelectedPillarInfo {
    /** @see #getTimeToDeliver() */
    private final TimeMeasureTYPE timeToDeliver;       
    
    /** 
     * Delegates to SelectedPillarInfo construct.
     * @see #getTimeToDeliver() 
     */
    public SelectedPillarForGetFileInfo(String pillarID, String pillarTopic, TimeMeasureTYPE timeToDeliver) {
        super(pillarID, pillarTopic);
        this.timeToDeliver = timeToDeliver;
    }

    /**
     * @return The estimated time to deliver for the selected pillar as specified in the identify response.
     */
    public TimeMeasureTYPE getTimeToDeliver() {
        return timeToDeliver;
    }
}