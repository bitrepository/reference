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
package org.bitrepository.client.eventhandler;


/**
 * Indicates a operation has completed succesfully.
 */
public class CompleteEvent extends AbstractOperationEvent {
    private final String[] componentResults;

    /**
     * @param info See {@link #getComponentResults()}
     * @param componentResults See {@link #getInfo()}
     * @param conversationID See {@link #getConversationID()}
    */
    public CompleteEvent(String info, String[] componentResults, String conversationID) {
        super(OperationEventType.COMPLETE, info, conversationID);
        this.componentResults = componentResults;
    }

    /** Returns the results for the individual components contributing to this operation */
    public String[] getComponentResults() {
        return componentResults;
    }

    @Override
    public String additionalInfo() {
        return componentResults.toString();
    }
}
