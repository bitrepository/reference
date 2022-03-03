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

package org.bitrepository.client.componentid;

import org.bitrepository.client.componentid.ComponentIDFactory;
import org.bitrepository.client.componentid.DefaultCommandlineComponentID;

/**
 * Provides destination utilities.
 */
public class ComponentIDHelper {
    private ComponentIDFactory clientIDFactory;

    /**
     * Create a <code>ClientIDHelper</code> object with the specified charactaristica
     * @param clientIDFactoryClass The class to use for constructing clientID.
     */
    public ComponentIDHelper(String clientIDFactoryClass) {
        clientIDFactory = createClientIDFactory(clientIDFactoryClass);
    }

    /**
     * @return a generated clientID to use for receiving messages for the indicated component.
     */
    public String getComponentID() {
        return clientIDFactory.getComponentID();
    }

    private ComponentIDFactory createClientIDFactory(String clientIDFactoryClass) {
        if (clientIDFactoryClass == null) {
            return new DefaultCommandlineComponentID();
        } else {
            try {
                return (ComponentIDFactory)Class.forName(clientIDFactoryClass).getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate ClientIDFactory " +
                        clientIDFactoryClass +
                        " as defined in ReferenceSettings->ClientSettings->ClientIDFactoryClass", e);
            }
        }
    }
}
