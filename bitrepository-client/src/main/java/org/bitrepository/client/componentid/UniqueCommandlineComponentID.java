package org.bitrepository.client.componentid;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.bitrepository.client.componentid.ComponentIDFactory;

/**
 * Delivers a client ID that is unique to the host, user and process that runs the client.  
 */
public class UniqueCommandlineComponentID implements ComponentIDFactory {

    @Override
    public String getComponentID() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            String username = System.getProperty("user.name"); 
            String PID = ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
            
            return hostname + "-" + username + "-" + PID;    
        } catch (UnknownHostException e) {
            throw new RuntimeException("Failed to lookup hostname", e);
        }
    }

}
