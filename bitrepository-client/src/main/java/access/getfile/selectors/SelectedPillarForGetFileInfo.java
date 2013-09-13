/*
 * #%L
 * Bitrepository Access
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package access.getfile.selectors;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;

/**
 * Container for information about a pillar which as been identified and are marked as 
 * selected for a GetFile request.
 */
public class SelectedPillarForGetFileInfo extends SelectedComponentInfo {
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