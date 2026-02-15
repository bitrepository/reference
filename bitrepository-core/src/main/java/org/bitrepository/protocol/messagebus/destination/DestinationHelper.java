/*
 * #%L
 * * Bitmagasin integrationstest
 *
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
 * Provides destination utilities.
 */
public class DestinationHelper {
    private final String componentID;
    private final String collectionDestinationID;
    private ReceiverDestinationIDFactory receiverDestinationIDFactory;

    /**
     * Create a <code>DestinationHelper</code> object with the specified characteristics
     *
     * @param componentID                       The componentID to use for the destination.
     * @param receiverDestinationIDFactoryClass The class to use for constructing receiverDestinationID.
     *                                          See {@link ReceiverDestinationIDFactory} for possible subclasses.
     * @param collectionDestinationID           The collection ID for the destination
     */
    public DestinationHelper(String componentID, String receiverDestinationIDFactoryClass, String collectionDestinationID) {
        this.componentID = componentID;
        this.collectionDestinationID = collectionDestinationID;

        receiverDestinationIDFactory = createReceiverDestinationIDFactory(receiverDestinationIDFactoryClass);
    }

    /**
     * @return the destinationID to use for receiving messages for the indicated component.
     */
    public String getReceiverDestinationID() {
        return receiverDestinationIDFactory.getReceiverDestinationID(componentID, collectionDestinationID);
    }

    private ReceiverDestinationIDFactory createReceiverDestinationIDFactory(
            String receiverDestinationIDFactoryClass) {
        if (receiverDestinationIDFactoryClass == null) {
            receiverDestinationIDFactory = new DefaultReceiverDestinationIDFactory();
            return new DefaultReceiverDestinationIDFactory();
        } else {
            try {
                return (ReceiverDestinationIDFactory) Class.forName(receiverDestinationIDFactoryClass)
                        .getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate ReceiverDestinationIDFactory " +
                        receiverDestinationIDFactoryClass +
                        " as defined in ReferenceSettings->GeneralSettings->ReceiverDestinationIDFactory", e);
            }
        }
    }
}
