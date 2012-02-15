/*
 * #%L
 * Bitrepository Access
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.access.audittrails;

/**
 * Encapsulates the information need to communicate with a Bit Repository component over the message bus.
 */
public class ComponentDestination {
    private final String componentID;
    private final String componentDestination;
    
    public ComponentDestination(String componentID, String componentDestination) {
        super();
        this.componentID = componentID;
        this.componentDestination = componentDestination;
    }
    
    /**
     * @return The componentID for the component.
     */
    public String getComponentID() {
        return componentID;
    }

    /**
     * @return The replyTo destination for the component.
     */
    public String getComponentDestination() {
        return componentDestination;
    }
    
    @Override
    public String toString() {
        return "ComponentDestination [componentID=" + componentID + ", componentDestination=" + componentDestination
                + "]";
    }
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((componentDestination == null) ? 0 : componentDestination.hashCode());
        result = prime * result + ((componentID == null) ? 0 : componentID.hashCode());
        return result;
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ComponentDestination other = (ComponentDestination) obj;
        if (componentDestination == null) {
            if (other.componentDestination != null)
                return false;
        } else if (!componentDestination.equals(other.componentDestination))
            return false;
        if (componentID == null) {
            if (other.componentID != null)
                return false;
        } else if (!componentID.equals(other.componentID))
            return false;
        return true;
    }
}
