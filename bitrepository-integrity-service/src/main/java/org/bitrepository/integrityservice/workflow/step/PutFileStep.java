/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.integrityservice.collector.IntegrityEventCompleteAwaiter;
import org.bitrepository.integrityservice.workflow.IntegrityWorkflowContext;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

import java.net.URL;

/**
 * Workflow step to perform the PutFile operation.
 */
public class PutFileStep extends AbstractWorkFlowStep {
    protected final IntegrityWorkflowContext context;
    protected final String collectionId;
    private final String fileId;
    private final URL uploadUrl;
    private final String checksum;

    /**
     * @param context      The context for the workflow.
     * @param collectionId The id of the collection to put the file to.
     * @param fileId       The id of the file to put.
     * @param uploadUrl    The URL for the file of the PutFile operation.'
     * @param checksum     The checksum of the file.
     */
    public PutFileStep(IntegrityWorkflowContext context, String collectionId, String fileId, URL uploadUrl, String checksum) {
        this.context = context;
        this.collectionId = collectionId;
        this.fileId = fileId;
        this.uploadUrl = uploadUrl;
        this.checksum = checksum;
    }

    @Override
    public String getName() {
        return "Performing PutFile for '" + fileId + "'.";
    }

    @Override
    public void performStep() {
        IntegrityEventCompleteAwaiter eventHandler = new IntegrityEventCompleteAwaiter(context.getSettings());

        ChecksumDataForFileTYPE checksumValidationData = new ChecksumDataForFileTYPE();
        checksumValidationData.setCalculationTimestamp(CalendarUtils.getNow());
        checksumValidationData.setChecksumSpec(ChecksumUtils.getDefault(context.getSettings()));
        checksumValidationData.setChecksumValue(Base16Utils.encodeBase16(checksum));

        context.getCollector()
                .putFile(collectionId, fileId, uploadUrl, checksumValidationData, eventHandler, "IntegrityService: " + getName());

        OperationEvent event = eventHandler.getFinish();
        if (event.getEventType() == OperationEventType.FAILED) {
            throw new IllegalStateException(
                    "Aborting workflow due to failure putting the file '" + fileId + "'. " + "Cause: " + event.toString());
        }
    }
}
