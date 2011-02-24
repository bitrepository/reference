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
package org.bitrepository.protocol.activemq;

import org.bitrepository.protocol.Message;

import javax.jms.TextMessage;

/** An adapter from a JMS message to a message in the bitrepository protocol. */
public class WrappedJMSMessage implements Message {
    /** The wrapped JMS message */
    private javax.jms.Message message;

    /**
     * Wrap a JMS message as a message in the bitrepository protocol.
     *
     * @param message The JMS message to wrap.
     */
    public WrappedJMSMessage(javax.jms.Message message) {
        this.message = message;
    }

    @Override
    public final String getText() throws Exception {
        return ((TextMessage) message).getText();
    }

    @Override
    public Class getMessageType() {
        try {
            String type = message.getJMSType();
            if(type == null || type.isEmpty()) {
                return null;
            }
            return Class.forName("org.bitrepository.bitrepositorymessages." 
                    + type);
        } catch (Exception e) {
            // TODO log this scenario.
            e.printStackTrace();
            return null;
        }
    }
}
