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
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.PutFileEventHandler;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.putfile.PutFileClient;

import java.net.URL;

import static org.bitrepository.commandline.Constants.ARGUMENT_IS_NOT_REQUIRED;

public class PutFileCmd extends CommandLineClient {
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
     * @param args Valid arguments are: <p/>
     *             k = Location of file with private security key. <br/>
     *             s = Location of folder with setting files. <br/>
     *             c = ID of the Collection. <br/>
     *             f = The actual file to put (Either this or the URL). <br/>
     *             u = The URL for the file (Either this or the actual file, though this requires both file id and checksum). <br/>
     *             i = The id of the file to put. <br/>
     *             C = The checksum of the file. <br/>
     */
    protected PutFileCmd(String... args) {
        super(args);
        client = ModifyComponentFactory.getInstance().retrievePutClient(settings, securityManager, getComponentID());
    }

    @Override
    protected boolean isFileIDArgumentRequired() {
        return false;
    }

    /**
     * Performs the PutFile operation.
     */
    @Override
    public void performOperation() {
        output.startupInfo("Putting .");
        OperationEvent finalEvent = putTheFile();
        output.completeEvent("Results of the PutFile operation for the file '" + getFileIDForMessage() + "'", finalEvent);
        if (finalEvent.getEventType() == OperationEventType.COMPLETE) {
            System.exit(Constants.EXIT_SUCCESS);
        } else {
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }

    @Override
    protected void createOptionsForCmdArgumentHandler() {
        super.createOptionsForCmdArgumentHandler();

        Option fileOption = new Option(Constants.FILE_ARG, Constants.HAS_ARGUMENT,
                "The path to the file to be uploaded. Is required, unless a URL is given.");
        fileOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(fileOption);

        Option urlOption = new Option(Constants.URL_ARG, Constants.HAS_ARGUMENT,
                "The URL for the file to be uploaded. Is required, unless a local file is given.");
        urlOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(urlOption);

        Option checksumOption = new Option(Constants.CHECKSUM_ARG, Constants.HAS_ARGUMENT,
                "The checksum for the file to be retrieved. Is required if using a URL.");
        checksumOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumOption);

        Option checksumTypeOption = new Option(Constants.REQUEST_CHECKSUM_TYPE_ARG, Constants.HAS_ARGUMENT,
                Constants.REQUEST_CHECKSUM_TYPE_DESC);
        checksumTypeOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumTypeOption);

        Option checksumSaltOption = new Option(Constants.REQUEST_CHECKSUM_SALT_ARG, Constants.HAS_ARGUMENT,
                Constants.REQUEST_CHECKSUM_SALT_DESC);
        checksumSaltOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumSaltOption);

        Option deleteOption = new Option(Constants.DELETE_FILE_ARG, Constants.NO_ARGUMENT, Constants.DELETE_FILE_DESC);
        deleteOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(deleteOption);
    }

    /**
     * Run the default validation, and validates that either only file or URL is given.
     * Also, if it is a URL that is given, then it must also be given the checksum and the file id.
     */
    @Override
    protected void validateArguments() {
        super.validateArguments();

        if (cmdHandler.hasOption(Constants.FILE_ARG) && cmdHandler.hasOption(Constants.URL_ARG)) {
            throw new IllegalArgumentException("Cannot take both a file (-f) and a URL (-u) as argument.");
        }
        if (!(cmdHandler.hasOption(Constants.FILE_ARG) || cmdHandler.hasOption(Constants.URL_ARG))) {
            throw new IllegalArgumentException("Providing either file argument (-f) or URL argument (-u) is required.");
        }
        if (cmdHandler.hasOption(Constants.URL_ARG) && !cmdHandler.hasOption(Constants.CHECKSUM_ARG)) {
            throw new IllegalArgumentException("Using URL argument (-u) requires the checksum argument (-C).");
        }
        if (cmdHandler.hasOption(Constants.URL_ARG) && !cmdHandler.hasOption(Constants.FILE_ID_ARG)) {
            throw new IllegalArgumentException("Using URL argument (-u) requires the file ID argument (-i).");
        }
    }

    /**
     * Initiates the operation and waits for the results.
     *
     * @return The final event for the results of the operation. Either 'FAILURE' or 'COMPLETE'.
     */
    private OperationEvent putTheFile() {
        output.debug("Uploading the file to the FileExchange.");
        URL url = getURLOrUploadFile();
        String fileID = retrieveFileID();

        output.debug("Initiating the PutFile conversation.");
        ChecksumDataForFileTYPE validationChecksum = getValidationChecksum();
        ChecksumSpecTYPE requestChecksum = getRequestChecksumSpecOrNull();

        boolean printChecksums = cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG);

        CompleteEventAwaiter eventHandler = new PutFileEventHandler(settings, output, printChecksums);
        client.putFile(getCollectionID(), url, fileID, getSizeOfFileOrZero(), validationChecksum, requestChecksum, eventHandler, null);

        OperationEvent finalEvent = eventHandler.getFinish();

        if (cmdHandler.hasOption(Constants.DELETE_FILE_ARG)) {
            deleteFileAfterwards(url);
        }

        return finalEvent;
    }

    /**
     * Retrieves the Checksum of the file, used by the pillars to validate.
     * This checksum is either taken from the actual file, or from the checksum argument.
     * <p/>
     * The checksum will be of the default checksum spec-type, which is defined in the settings.
     *
     * @return The spec-type of the checksum as {@link ChecksumDataForFileTYPE}.
     */
    protected ChecksumDataForFileTYPE getValidationChecksum() {
        if (cmdHandler.hasOption(Constants.FILE_ARG)) {
            return getValidationChecksumDataForFile(findTheFile());
        } else {
            return getValidationChecksumDataFromArgument(Constants.CHECKSUM_ARG);
        }
    }

    /**
     * @return The filename (FileID) for the file to upload.
     */
    private String getFileIDForMessage() {
        if (cmdHandler.hasOption(Constants.URL_ARG)) {
            return cmdHandler.getOptionValue(Constants.URL_ARG) + " (with the id '" + cmdHandler.getOptionValue(Constants.FILE_ID_ARG) +
                    "')";
        }
        if (cmdHandler.hasOption(Constants.FILE_ARG)) {
            return cmdHandler.getOptionValue(Constants.FILE_ARG) + (cmdHandler.hasOption(Constants.FILE_ID_ARG) ?
                    " (with the id '" + cmdHandler.getOptionValue(Constants.FILE_ID_ARG) + "')" : "");
        }
        return "Failed";
    }
}
