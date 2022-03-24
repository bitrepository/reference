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
import org.bitrepository.modify.replacefile.conversation.ReplaceFileCompletePillarEvent;

/**
 * Complete event awaiter for Getfile.
 * Prints out checksum results, if any.
 */
public class ReplaceFileEventHandler extends CompleteEventAwaiter {

    /**
     * Constructor.
     *
     * @param settings      The settings.
     * @param outputHandler The output handler.
     */
    public ReplaceFileEventHandler(Settings settings, OutputHandler outputHandler) {
        super(settings, outputHandler);
    }

    @Override
    public void handleComponentComplete(OperationEvent event) {
        if (!(event instanceof ReplaceFileCompletePillarEvent)) {
            output.warn("ReplaceFileEventHandler received a component complete, which is not a " +
                    ReplaceFileCompletePillarEvent.class.getName());
        }

        assert event instanceof ReplaceFileCompletePillarEvent;
        ReplaceFileCompletePillarEvent pillarEvent = (ReplaceFileCompletePillarEvent) event;
        StringBuilder componentText = new StringBuilder();
        if (pillarEvent.getChecksumForReplacedFile() != null) {
            componentText.append("Checksum for replaced file: ")
                    .append(Base16Utils.decodeBase16(pillarEvent.getChecksumForReplacedFile().getChecksumValue())).append("\t");
        }

        if (pillarEvent.getChecksumForNewFile() != null) {
            componentText.append("Checksum for new file: ")
                    .append(Base16Utils.decodeBase16(pillarEvent.getChecksumForNewFile().getChecksumValue())).append("\t");
        }

        if (componentText.length() != 0) {
            output.resultLine(pillarEvent.getContributorID() + " : \t" + componentText);
        }
    }
}
