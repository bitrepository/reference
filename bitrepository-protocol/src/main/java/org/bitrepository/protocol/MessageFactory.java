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

import java.io.ByteArrayOutputStream;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class MessageFactory {
    private static final Logger LOG = LoggerFactory.getLogger(MessageFactory.class);
    private static final String XML_NAMESPACES
            = "org.bitrepository.bitrepositorymessages:"
            + "org.bitrepository.bitrepositoryelements:"
            + "org.bitrepository.bitrepositorydata";

    private MessageFactory () {
    }

    @SuppressWarnings("unchecked")
    public static <T> T createMessage(Class<T> messageClass, String xmlMessage) throws JAXBException {
        try {
            JAXBContext context = JAXBContext.newInstance(XML_NAMESPACES);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            return (T) unmarshaller.unmarshal(new StreamSource(new StringReader(xmlMessage)));
        } catch(JAXBException jex) {
            LOG.error("Failed to create message object from string: {}", xmlMessage);
            throw jex;
        }
    }
    
    /**
     * Method for retrieving the content of a message class as a string.
     * @param message The message object which should be made into XML.
     * @return The XML representation of the message object.
     * @throws JAXBException If the object could not be parsed as a JAXB object.
     */
    public static String extractMessage(Object message) throws JAXBException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Marshaller m = JAXBContext.newInstance(message.getClass()).createMarshaller();
        m.marshal(message, bos);
        return bos.toString();
    }
}
