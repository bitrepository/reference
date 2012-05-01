/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.service.contributor;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * The context for the contributor mediator.
 */
public class ContributorContext {
    /** Message sender for this context.*/
    private final MessageSender dispatcher;
    /** The settings for thi context.*/
    private final Settings settings;
    /** The ID of the contributor component for this context.*/
    private final String componentID;
    /** The destination for this context.*/
    private final String replyTo;

    /**
     * 
     * @param dispatcher
     * @param settings
     * @param componentID
     * @param replyTo
     */
    public ContributorContext(
            MessageSender dispatcher,
            Settings settings,
            String componentID,
            String replyTo) {
        this.dispatcher = dispatcher;
        this.settings = settings;
        this.componentID = componentID;
        this.replyTo = replyTo;
    }

    public MessageSender getDispatcher() {
        return dispatcher;
    }

    public Settings getSettings() {
        return settings;
    }

    public String getComponentID() {
        return componentID;
    }

    public String getReplyTo() {
        return replyTo;
    }
}
