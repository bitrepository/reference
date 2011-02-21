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
package org.bitrepository.protocol;

import java.io.File;
import java.io.StringReader;

import javax.jms.JMSException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.bitrepository.common.ConfigurationFactory;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.configuration.protocolconfiguration.ProtocolConfiguration;
import org.bitrepository.protocol.exceptions.CoordinationLayerException;

/**
 * Provides access to the different component in the protocol module (Spring wannabe)
 */
public class ProtocolComponentFactory {
	private static final ProtocolComponentFactory instance = new ProtocolComponentFactory();	
	private ProtocolComponentFactory() {}
	/** What we have here is a singleton */
	public static ProtocolComponentFactory getInstance() {
		return instance;
	}
	
	private ProtocolConfiguration protocolConfiguration;
	private MessageBus messagebus;

	/**
	 * Gets you a object for accessing the Bitrepositories message bus
	 */
	public MessageBus getMessageBus() {
		if (messagebus == null) {
			try {
				messagebus = new ActiveMQMessageBus(getProtocolConfiguration().getMessageBusConfigurations());
			} catch (JMSException e) {
				throw new CoordinationLayerException("Failed to get a handle on the message bus", e);
			}
		}
		return messagebus;
	}
	
	/**
	 * Gets you the configuration for this module
	 */
	private ProtocolConfiguration getProtocolConfiguration() {
		if (protocolConfiguration == null) {			
			protocolConfiguration = 
				ConfigurationFactory.createConfiguration("org.bitrepository.protocol.configuration.protocolconfiguration",
					ProtocolConfiguration.class, new File("src/main/resources/configurations/xml/protocol-configuration.xml"));
		}
		return protocolConfiguration;
	}
}
