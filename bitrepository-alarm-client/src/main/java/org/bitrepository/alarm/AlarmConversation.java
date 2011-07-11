/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id: SimpleGetFileClient.java 214 2011-07-05 14:44:30Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/getfile/SimpleGetFileClient.java $
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
package org.bitrepository.alarm;

import java.net.URL;

import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.messagebus.MessageSender;

public class AlarmConversation extends AbstractConversation<URL> {

    public AlarmConversation(MessageSender messageSender, String conversationID) {
        super(messageSender, conversationID);
    }

    @Override
    public void failConversion(String arg0) { }

    @Override
    public URL getResult() {
        return null;
    }

    @Override
    public boolean hasEnded() {
        return true;
    }

    @Override
    public void startConversion() throws OperationFailedException {}
}
