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

import java.net.URL;

import org.apache.commons.cli.Option;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.PutFileEventHandler;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;

/**
 * Putting a file to the collection.
 */
public class PutFileCmd extends CommandLineClient {

    /** The client for performing the PutFile operation.*/
    private final PutFileClient client;

    /**
     * @param args The arguments for performing the PutFile operation.
     */
    public static void main(String[] args) {
        try {
            PutFileCmd client = new PutFileCmd(args);
            client.runCommand();
        } catch (IllegalArgumentException iae) {
            System.exit(Constants.EXIT_ARGUMENT_FAILURE);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }

    /**
     * @param args The arguments:
     * k = Location of file with private security key.
     * s = Location of folder with setting files.
     * c = ID of the Collection
     * f = The actual file to put (Either this or the URL).
     * u = The URL for the file (Either this or the actual file, though this requires both file id and checksum).
     * i = The id of the file to put.
     * C = The checksum of the file.
     * 
     */
    protected PutFileCmd(String ... args) {
        super(args);
        client = ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, getComponentID());
    }

    @Override
    protected boolean isFileIDArgumentRequired() {
        return false;
    }

    /**
     * Perform the PutFile operation.
     */
    @Override
    public void performOperation() {
        output.startupInfo("Putting .");
        OperationEvent finalEvent = putTheFile();
        output.completeEvent("Results of the PutFile operation for the file '" + getFileIDForMessage(), finalEvent);
        if(finalEvent.getEventType() == OperationEventType.COMPLETE) {
            System.exit(Constants.EXIT_SUCCESS);
        } else {
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }

    @Override
    protected void createOptionsForCmdArgumentHandler() {
        super.createOptionsForCmdArgumentHandler();

        Option fileOption = new Option(Constants.FILE_ARG, Constants.HAS_ARGUMENT,
                "The path to the file, which is wanted to be put. Is required, unless a URL is given.");
        fileOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(fileOption);

        Option urlOption = new Option(Constants.URL_ARG, Constants.HAS_ARGUMENT, 
                "The URL for the file to be put. Is required, unless the actual file is given.");
        urlOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(urlOption);

        Option checksumOption = new Option(Constants.CHECKSUM_ARG, Constants.HAS_ARGUMENT, 
                "The checksum for the file to be retreived. Required if using an URL.");
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

        Option deleteOption = new Option(Constants.DELETE_FILE_ARG, Constants.NO_ARGUMENT, 
                "If this argument is present, then the file will be removed from the server, "
                        + "when the operation is complete.");
        deleteOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(deleteOption);
    }

    /**
     * Run the default validation, and validates that only file or URL is given.
     * Also, if it is an URL is given, then it must also be given the checksum and the file id.
     */
    @Override
    protected void validateArguments() {
        super.validateArguments();

        if(cmdHandler.hasOption(Constants.FILE_ARG) && cmdHandler.hasOption(Constants.URL_ARG)) {
            throw new IllegalArgumentException("Cannot take both a file (-f) and an URL (-u) as argument.");
        }
        if(!(cmdHandler.hasOption(Constants.FILE_ARG) || cmdHandler.hasOption(Constants.URL_ARG))) {
            throw new IllegalArgumentException("Requires either the file argument (-f) or the URL argument (-u).");
        }
        if(cmdHandler.hasOption(Constants.URL_ARG) && !cmdHandler.hasOption(Constants.CHECKSUM_ARG)) {
            throw new IllegalArgumentException("The URL argument requires also the checksum argument (-c).");
        }
        if(cmdHandler.hasOption(Constants.URL_ARG) && !cmdHandler.hasOption(Constants.FILE_ID_ARG)) {
            throw new IllegalArgumentException("The URL argument requires also the argument for the ID of the "
                    + "file (-i).");
        }

        validateRequestChecksumSpec();
    }

    /**
     * Initiates the operation and waits for the results.
     * @return The final event for the results of the operation. Either 'FAILURE' or 'COMPLETE'.
     */
    private OperationEvent putTheFile() {
        output.debug("Uploading the file to the FileExchange.");
        URL url = getURLOrUploadFile();
        String fileId = retrieveFileID();

        output.debug("Initiating the PutFile conversation.");
        ChecksumDataForFileTYPE validationChecksum = getValidationChecksum();
        ChecksumSpecTYPE requestChecksum = getRequestChecksumSpecOrNull();

        boolean printChecksums = cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG);

        CompleteEventAwaiter eventHandler = new PutFileEventHandler(settings, output, printChecksums);
        client.putFile(getCollectionID(), url, fileId, getSizeOfFileOrZero(), validationChecksum, requestChecksum, eventHandler, null);

        OperationEvent finalEvent = eventHandler.getFinish(); 

        if(cmdHandler.hasOption(Constants.DELETE_FILE_ARG)) {
            deleteFileAfterwards(url);
        }

        return finalEvent;
    }


    /**
     * Retrieves the Checksum for the pillars to validate, either taken from the actual file, 
     * or from the checksum argument.
     * It will be in the default checksum spec type from settings.
     * @return The checksum validation type.
     */
    protected ChecksumDataForFileTYPE getValidationChecksum() {
        if(cmdHandler.hasOption(Constants.FILE_ARG)) {
            return getValidationChecksumDataForFile(findTheFile());            
        } else {
            return getValidationChecksumDataFromArgument(Constants.CHECKSUM_ARG);
        }
    }

    /**
     * @return The filename for the file to upload. 
     */
    private String getFileIDForMessage() {
        return cmdHandler.getOptionValue(Constants.FILE_ARG) + (cmdHandler.hasOption(Constants.FILE_ID_ARG) ? 
                " (with the id '" + cmdHandler.getOptionValue(Constants.FILE_ID_ARG) + "')" : "");
    }
}
