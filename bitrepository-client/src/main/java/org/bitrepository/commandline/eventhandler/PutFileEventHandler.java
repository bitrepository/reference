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
package org.bitrepository.commandline.eventhandler;

import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.modify.putfile.conversation.PutFileCompletePillarEvent;

/**
 * Complete event awaiter for Getfile.
 * Prints out checksum results, if any.
 */
public class PutFileEventHandler extends CompleteEventAwaiter {

    private final Boolean printOutput;
    
    /**
     * Constructor.
     * @param settings The {@link Settings}
     * @param outputHandler The {@link OutputHandler} for handling output
     * @param printOutput Setting for determining if output should be printet
     */
    public PutFileEventHandler(Settings settings, OutputHandler outputHandler, boolean printOutput) {
        super(settings, outputHandler);
        this.printOutput = printOutput;
        
        if(printOutput) {
            output.resultHeader("PillarId \t Checksum");
        }
    }

    @Override
    public void handleComponentComplete(OperationEvent event) {
        if(!(event instanceof PutFileCompletePillarEvent)) {
            output.warn("PutFileEventHandler received a component complete, which is not a "
                    + PutFileCompletePillarEvent.class.getName());
        }
        
        PutFileCompletePillarEvent pillarEvent = (PutFileCompletePillarEvent) event;
        if(printOutput && pillarEvent.getChecksums() != null) {
            output.resultLine(pillarEvent.getContributorID() + " \t " + Base16Utils.decodeBase16(pillarEvent.getChecksums().getChecksumValue()));
        }
    }

}
