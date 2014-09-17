package org.bitrepository.client.componentid;

import org.bitrepository.client.componentid.ComponentIDFactory;

/**
 * Delivers the default commandline client ID. 
 */
public class DefaultCommandlineComponentID implements ComponentIDFactory {

    @Override
    public String getComponentID() {
        return System.getProperty("user.name") + "'s cmdline client";
    }

}
