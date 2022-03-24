/*
 * #%L
 * Bitrepository Client
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;

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
