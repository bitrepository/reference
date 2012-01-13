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
package org.bitrepository.protocol.messagebus;

import javax.xml.bind.JAXBException;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract implementation of message listener.
 *
 * This implementation will log received messages as warnings, and throw an
 * exception about these being unsupported.
 */
public abstract class SimpleAbstractMessageListener implements SimpleMessageListener {
    /** Logger for this class. */
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Report an unsupported message by logging a warning and throwing an exception.
     *
     * @param message The unsupported message received.
     */
    protected void reportUnsupported(Object message) {
        JaxbHelper jaxbHelper = new JaxbHelper("xsd/", "BitRepositoryMessages.xsd");
        try {
            log.warn("Received unsupported message '{}'", jaxbHelper.serializeToXml(message));
        } catch (JAXBException e) {
            log.warn("Received unsupported message of type '" + message.getClass().getName()
                             + "', which could not be serialized as XML.", e);
        }
        throw new UnsupportedOperationException(
                "The message listener does not accept messages of this type: '" + message.getClass().getName() + "'");
    }

    @Override
    public void onMessage(Message message) {
        reportUnsupported(message);
    }
}
