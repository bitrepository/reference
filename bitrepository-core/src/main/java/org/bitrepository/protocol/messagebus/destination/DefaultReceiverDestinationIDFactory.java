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
package org.bitrepository.protocol.messagebus.destination;

/**
 * Builds the receiver destination a single component uses for messages addressed specifically to it (replies to
 * its own requests, and requests sent directly to it, e.g. a chosen pillar's PutFileRequest). This is
 * point-to-point by nature - exactly one component ever consumes each such message.
 */
public class DefaultReceiverDestinationIDFactory implements ReceiverDestinationIDFactory {
    private static final String SCHEME_SEPARATOR = "://";
    private static final String QUEUE_SCHEME = "queue" + SCHEME_SEPARATOR;

    @Override
    public String getReceiverDestinationID(String componentID, String collectionDestinationID) {
        return QUEUE_SCHEME + withoutScheme(collectionDestinationID) + "-" + componentID;
    }

    private static String withoutScheme(String destinationID) {
        int schemeEnd = destinationID.indexOf(SCHEME_SEPARATOR);
        return schemeEnd == -1 ? destinationID : destinationID.substring(schemeEnd + SCHEME_SEPARATOR.length());
    }
}
