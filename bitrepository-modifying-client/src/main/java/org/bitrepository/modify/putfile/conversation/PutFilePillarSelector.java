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
package org.bitrepository.modify.putfile.conversation;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.selector.MultipleComponentSelector;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;

/**
 * Extends the <code>MultipleComponentSelector</code> with test of correct message type.
 */
public class PutFilePillarSelector extends MultipleComponentSelector {
        
    /**
     * @param pillarsWhichShouldRespond The IDs of the pillars to be selected.
     */
    public PutFilePillarSelector(Collection<String> pillarsWhichShouldRespond) {
        super(pillarsWhichShouldRespond);
    }

    @Override
    public void processResponse(MessageResponse response) throws UnexpectedResponseException {
        if (response instanceof IdentifyPillarsForPutFileResponse) {
            responseStatus.responseReceived(response.getFrom());
            if(response.getResponseInfo().getResponseCode().equals(ResponseCode.IDENTIFICATION_POSITIVE)) {
                selectedComponents.add(new SelectedComponentInfo(response.getFrom(), response.getReplyTo()));                
            }
        } else {
            throw new UnexpectedResponseException("Are currently only expecting IdentifyPillarsForPutFileResponse's");
        }
    }
    
    @Override
    public boolean isFinished() throws UnableToFinishException {
        return responseStatus.haveAllComponentsResponded();
    }
}
