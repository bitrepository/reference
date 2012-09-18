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
package org.bitrepository.access.getfile.selectors;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.selector.ComponentSelector;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.utils.TimeMeasurementUtils;

public class FastestPillarSelectorForGetFile extends ComponentSelector {
    private TimeMeasureTYPE fastestTimeToDeliver;

    public synchronized void selectComponent(MessageResponse message) throws UnexpectedResponseException {
        IdentifyPillarsForGetFileResponse response = (IdentifyPillarsForGetFileResponse)message;
        if (fastestTimeToDeliver == null ) {
            fastestTimeToDeliver = response.getTimeToDeliver();
        }
        if (isFaster(response)) {
            fastestTimeToDeliver = response.getTimeToDeliver();
            selectedComponents.clear();
            super.selectComponent(message);
        }
    }

    protected boolean isFaster(IdentifyPillarsForGetFileResponse response) {
        return TimeMeasurementUtils.compare(response.getTimeToDeliver(), fastestTimeToDeliver) <= 0;
    }
}
