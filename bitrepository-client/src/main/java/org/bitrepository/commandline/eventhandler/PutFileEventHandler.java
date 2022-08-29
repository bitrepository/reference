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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.modify.putfile.conversation.PutFileCompletePillarEvent;

/**
 * Complete event awaiter for GetFile.
 * Prints out checksum results, if any.
 */
public class PutFileEventHandler extends CompleteEventAwaiter {
    private final Boolean printOutput;
    private final ChecksumSpecTYPE checksumSpecTYPE;

    /**
     * Constructor.
     *
     * @param settings      The {@link Settings}
     * @param outputHandler The {@link OutputHandler} for handling output
     */
    public PutFileEventHandler(Settings settings, OutputHandler outputHandler, ChecksumSpecTYPE checksumSpecTYPE) {
        super(settings, outputHandler);
        this.checksumSpecTYPE = checksumSpecTYPE;
        this.printOutput = checksumSpecTYPE.getChecksumType() != ChecksumUtils.getDefaultChecksumType(settings);

        if (printOutput) {
            output.resultHeader("PillarId \t Alg \t Checksum");
        }
    }

    @Override
    public void handleComponentComplete(OperationEvent event) {
        if (!(event instanceof PutFileCompletePillarEvent)) {
            output.warn("PutFileEventHandler received a component complete, which is not a " + PutFileCompletePillarEvent.class.getName());
        }

        assert event instanceof PutFileCompletePillarEvent;
        PutFileCompletePillarEvent pillarEvent = (PutFileCompletePillarEvent) event;
        boolean isDuplicate = pillarEvent.getResponseInfo().getResponseCode().equals(ResponseCode.DUPLICATE_FILE_FAILURE);
        if (printOutput && pillarEvent.getChecksums() != null) {
            if (isDuplicate) {
                output.resultLine(pillarEvent.getContributorID() + " \t  \t File already exists, use get-checksums.");
            } else {
                String checksum = Base16Utils.decodeBase16(pillarEvent.getChecksums().getChecksumValue());
                output.resultLine(pillarEvent.getContributorID() + " \t " + checksumSpecTYPE.getChecksumType().value() + " \t " + checksum);
            }
        }
    }
}
