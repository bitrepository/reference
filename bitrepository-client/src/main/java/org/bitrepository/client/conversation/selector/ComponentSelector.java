/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.client.conversation.selector;

import java.util.HashSet;
import java.util.Set;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

/** Can be used to select a single component to run an operation on by handling the identify responses. <p>
 * The algorithm for selecting the component is implemented in the concrete classes.
 */
public class ComponentSelector {
    /** Used for tracking who has answered. */
    protected final Set<SelectedComponentInfo> selectedComponents = new HashSet<>();

    public void  selectComponent(MessageResponse response) throws UnexpectedResponseException {
        selectedComponents.add(new SelectedComponentInfo(response.getFrom(), response.getReplyTo()));
    }

    public Set<SelectedComponentInfo> getSelectedComponents() {
        return selectedComponents;
    }
}
