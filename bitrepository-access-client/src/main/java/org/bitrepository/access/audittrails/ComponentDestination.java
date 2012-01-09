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
