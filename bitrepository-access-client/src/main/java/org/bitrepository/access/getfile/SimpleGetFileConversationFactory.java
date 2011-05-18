/*
 * #%L
 * Bitrepository Access Client
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
package org.bitrepository.access.getfile;

import org.bitrepository.protocol.ConversationFactory;
import org.bitrepository.protocol.MessageBus;

/** Factory that generates GetFile conversations. */
public class SimpleGetFileConversationFactory implements ConversationFactory<SimpleGetFileConversation> {
    /** The message bus used by the conversations to communicate. */
    private final MessageBus messageBus;
    /** The expected numbers of pillars involved in the GetFile conversations. */
    private final int expectedNumberOfPillars;
    /** The default timeout for GetFile operations. May be overridden by better data. */
    private long getFileDefaultTimeout;
    /**The directory where retrieved files are stored. */
    private String fileDir;

    /**
     * Initialise a factory that generates GetFile conversations.
     * @param messageBus The message bus used by the conversations to communicate.
     * @param expectedNumberOfPillars The expected numbers of pillars involved in the GetFile conversations.
     * @param getFileDefaultTimeout The default timeout for GetFile operations. May be overridden by better data.
     * @param fileDir The directory where retrieved files are stored.
     */
    public SimpleGetFileConversationFactory(MessageBus messageBus, int expectedNumberOfPillars,
                                            long getFileDefaultTimeout, String fileDir) {
        this.messageBus = messageBus;
        this.fileDir = fileDir;
        this.expectedNumberOfPillars = expectedNumberOfPillars;
        this.getFileDefaultTimeout = getFileDefaultTimeout;
    }

    @Override
    public SimpleGetFileConversation createConversation() {
        return new SimpleGetFileConversation(messageBus, expectedNumberOfPillars, getFileDefaultTimeout, fileDir);
    }
}
