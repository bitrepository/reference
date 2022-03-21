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
import org.bitrepository.modify.deletefile.conversation.DeleteFileCompletePillarEvent;

/**
 * Complete event awaiter for GetFile.
 * Prints out checksum results, if any.
 */
public class DeleteFileEventHandler extends CompleteEventAwaiter {

    /**
     * Constructor.
     *
     * @param settings      The {@link Settings}
     * @param outputHandler The {@link OutputHandler} for handling output
     */
    public DeleteFileEventHandler(Settings settings, OutputHandler outputHandler) {
        super(settings, outputHandler);
    }

    @Override
    public void handleComponentComplete(OperationEvent event) {
        if (!(event instanceof DeleteFileCompletePillarEvent)) {
            output.warn("DeleteFileEventHandler received a component complete, which is not a "
                    + DeleteFileCompletePillarEvent.class.getName());
        }

        assert event instanceof DeleteFileCompletePillarEvent;
        DeleteFileCompletePillarEvent pillarEvent = (DeleteFileCompletePillarEvent) event;
        if (pillarEvent.getChecksums() != null) {
            output.resultLine(
                    pillarEvent.getContributorID() + " \t " + Base16Utils.decodeBase16(pillarEvent.getChecksums().getChecksumValue()));
        }
    }

}
