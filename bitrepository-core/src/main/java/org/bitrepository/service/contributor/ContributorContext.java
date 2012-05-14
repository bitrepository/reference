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
    /** @see getDispatcher.*/
    private final MessageSender dispatcher;
    /** @see getSettings.*/
    private final Settings settings;
    /** @see getComponentID.*/
    private final String componentID;
    /** @see getReplyTo.*/
    private final String replyTo;

    /**
     * Constructor.
     * @param dispatcher The dispatcher of messages.
     * @param settings The settings.
     * @param componentID The id of the component.
     * @param replyTo The destination where replies are wanted.
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

    /**
     * @return Message sender for this context.
     */
    public MessageSender getDispatcher() {
        return dispatcher;
    }

    /**
     * @return The settings for this context.
     */
    public Settings getSettings() {
        return settings;
    }

    /**
     * @return The ID of the contributor component for this context.
     */
    public String getComponentID() {
        return componentID;
    }

    /**
     * @return The destination for this context.
     */
    public String getReplyTo() {
        return replyTo;
    }
}
