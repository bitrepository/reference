/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.bus;

import org.apache.activemq.broker.BrokerService;
import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LocalActiveMQBroker {
    private final Logger log = LoggerFactory.getLogger(getClass());
    
    private BrokerService broker;
    
    public LocalActiveMQBroker(MessageBusConfiguration configuration) {
        broker = new BrokerService();
        broker.setPersistent(false);
        broker.setDataDirectory("target/activemq-data");
        try {
            broker.addConnector(configuration.getURL());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        log.info("Created embedded broker {}", broker);
    }
    
    public void start() {
        try {
            broker.start();
            broker.waitUntilStarted();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void stop() throws Exception {
        broker.stop();
        broker.waitUntilStopped();
    }
}
