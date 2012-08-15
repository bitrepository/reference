package org.bitrepository.pillar.integration.func.putfile;
/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.net.MalformedURLException;
import java.net.URL;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.putfile.conversation.IdentifyPillarsForPutFile;
import org.bitrepository.modify.putfile.conversation.PutFileConversationContext;
import org.bitrepository.protocol.messagebus.MessageSender;

public class PutFileProtocolAdaptor {
    private final MessageSender messageSender;
    private final Settings settings;

    public PutFileProtocolAdaptor(Settings settings, MessageSender messageSender) {
        this.settings = settings;
        this.messageSender = messageSender;
    }


    /**
     *
     * @return The destination for putFile for the indicated pillar.
     */
    protected String identifyPillarForPut(String pillarID)  {
        PutFileConversationContext context = null;
        try {
            context = new PutFileConversationContext(
                    "dummyFileID", new URL("http://dummyURL"), 10L,
                    null, null,
                    settings, messageSender, "PutFileProtocolAdaptor", null,
                    null
            );
        } catch (MalformedURLException e) {
            // Never happens.
        }
        IdentifyPillarsForPutFile identifier = new IdentifyPillarsForPutFile(context);
        return null;
    }
}
