/*
 * #%L
 * Bitrepository Command Line
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
package org.bitrepository.commandline;

import org.apache.commons.cli.Option;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.utils.CompleteEventAwaiter;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.deletefile.DeleteFileClient;

/**
 * Deleting a file from the collection.
 */
public class DeleteFile extends CommandLineClient {
    private final static String COMPONENT_ID = "DeleteFileClient";
    /** The client for performing the DeleteFile operation.*/
    private final DeleteFileClient client;

    /**
     * @param args The arguments for performing the DeleteFile operation.
     */
    public static void main(String[] args) {
    	try {
    		DeleteFile deletefile = new DeleteFile(args);
            deletefile.performOperation();
    	} catch (RuntimeException e) {
    		e.printStackTrace();
    		System.exit(Constants.EXIT_OPERATION_FAILURE);
    	}
    }

    /**
     * @param args The command line arguments for defining the operation.
     */
    private DeleteFile(String ... args) {
        super(args);
        client = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(settings, securityManager,
                COMPONENT_ID);
    }

    @Override
    protected String getComponentID() {
        return COMPONENT_ID;
    }

    @Override
    protected boolean isFileIDArgumentRequired() {
        return true;
    }

    @Override
    protected void createOptionsForCmdArgumentHandler() {
        super.createOptionsForCmdArgumentHandler();

        Option checksumOption = new Option(Constants.CHECKSUM_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] The checksum of the file to be delete.");
        checksumOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumOption);

        Option checksumTypeOption = new Option(Constants.REQUEST_CHECKSUM_TYPE_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] The algorithm of checksum to request in the response from the pillars.");
        checksumTypeOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumTypeOption);
        Option checksumSaltOption = new Option(Constants.REQUEST_CHECKSUM_SALT_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] The salt of checksum to request in the response. Requires the ChecksumType argument.");
        checksumSaltOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumSaltOption);
    }

    protected void validateArguments() {
        super.validateArguments();
        if(!cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            throw new IllegalArgumentException("The pillar argument -p must defined for the delete operation, " +
                    "only single pillar deletes are allowed");
        }
        if (!cmdHandler.hasOption(Constants.CHECKSUM_ARG) &&
                settings.getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests()) {
            throw new IllegalArgumentException("Checksum argument (-C) are mandatory for delete and replace operations" +
                    "as defined in RepositorySettings.");
        }
    }

    /**
     * Perform the DeleteFile operation.
     */
    public void performOperation() {
        output.debug("Performing the DeleteFile operation.");
        OperationEvent finalEvent = deleteTheFile();
        output.completeEvent("Results of the DeleteFile operation for the file '"
                + cmdHandler.getOptionValue(Constants.FILE_ID_ARG) + "'"
                + ": ", finalEvent);
        if(finalEvent.getEventType() == OperationEventType.COMPLETE) {
            System.exit(Constants.EXIT_SUCCESS);
        } else {
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }

    /**
     * Initiates the operation and waits for the results.
     * @return The final event for the results of the operation. Either 'FAILURE' or 'COMPLETE'.
     */
    private OperationEvent deleteTheFile() {
        String fileId = cmdHandler.getOptionValue(Constants.FILE_ID_ARG);

        ChecksumDataForFileTYPE validationChecksum = getValidationChecksum();
        ChecksumSpecTYPE requestChecksum = getRequestChecksumSpec();

        output.debug("Initiating the DeleteFile conversation.");
        CompleteEventAwaiter eventHandler = new CompleteEventAwaiter(settings, output);
        String pillarId = cmdHandler.getOptionValue(Constants.PILLAR_ARG);
        client.deleteFile(getCollectionID(), fileId, pillarId, validationChecksum, requestChecksum, eventHandler,
                "Delete file from commandline for file '" + fileId + "' at pillar '" + pillarId + "'.");

        return eventHandler.getFinish();
    }

    /**
     * Creates the data structure for encapsulating the validation checksums for validation on the pillars.
     * @return The ChecksumDataForFileTYPE for the pillars to validate the DeleteFile operation.
     */
    private ChecksumDataForFileTYPE getValidationChecksum() {
        if(!cmdHandler.hasOption(Constants.CHECKSUM_ARG)) {
            return null;
        }

        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        res.setCalculationTimestamp(CalendarUtils.getNow());
        res.setChecksumSpec(ChecksumUtils.getDefault(settings));
        res.setChecksumValue(Base16Utils.encodeBase16(cmdHandler.getOptionValue(Constants.CHECKSUM_ARG)));

        return res;
    }

    /**
     * @return The requested checksum spec, or null if the arguments does not exist.
     */
    private ChecksumSpecTYPE getRequestChecksumSpec() {
        if(!cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG)) {
            return null;
        }

        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumType(ChecksumType.fromValue(cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));

        if(cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_SALT_ARG)) {
            res.setChecksumSalt(Base16Utils.encodeBase16(cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));
        }
        return res;
    }
}
