/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id: ReferencePillar.java 210 2011-07-04 19:44:03Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-integration/src/main/java/org/bitrepository/pillar/ReferencePillar.java $
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
package org.bitrepository.integration;

import org.bitrepository.integration.configuration.integrationconfiguration.IntegrationConfiguration;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.configuration.ProtocolConfiguration;
import org.bitrepository.protocol.messagebus.MessageBus;

public class ConfigPrinter {

    public static void main(String[] args) {
        IntegrationComponentFactory ifactory = IntegrationComponentFactory.getInstance();
        IntegrationConfiguration iConfig = ifactory.getConfig();
        
        System.out.println(iConfig.getBitrepositoryCollectionId());
        System.out.println(iConfig.getPillarId());
        System.out.println(iConfig.getQueue());
        
        ProtocolComponentFactory pFactory = ProtocolComponentFactory.getInstance();
        ProtocolConfiguration cConfig = pFactory.getProtocolConfiguration();
        
        System.out.println(cConfig.getFileExchangeConfigurations());
        System.out.println(cConfig.getMessageBusConfigurations());
        
        MessageBus bus = pFactory.getMessageBus();
        
    }
}
