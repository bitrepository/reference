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
import org.bitrepository.commandline.eventhandler.ReplaceFileEventHandler;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.replacefile.ReplaceFileClient;

import static org.bitrepository.commandline.Constants.*;

/**
 * Replace a file from the collection.
 * 
 */
public class ReplaceFileCmd extends CommandLineClient {
    /** The client for performing the ReplaceFile operation.*/
    private final ReplaceFileClient client;

    /**
     * @param args The arguments for performing the ReplaceFile operation.
     */
    public static void main(String[] args) {
        try {
            ReplaceFileCmd client = new ReplaceFileCmd(args);
            client.runCommand();
        } catch (IllegalArgumentException iae) {
            System.exit(EXIT_ARGUMENT_FAILURE);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(EXIT_OPERATION_FAILURE);
        }
    }

    /**
     * @param args The command line arguments for defining the operation.
     */
    protected ReplaceFileCmd(String ... args) {
        super(args);
        client = ModifyComponentFactory.getInstance().retrieveReplaceFileClient(settings, securityManager,
                getComponentID());
    }

    @Override
    protected boolean isFileIDArgumentRequired() {
        return false;
    }

    @Override
    protected void createOptionsForCmdArgumentHandler() {
        super.createOptionsForCmdArgumentHandler();

        Option checksumOption = new Option(CHECKSUM_ARG, HAS_ARGUMENT,
                "[OPTIONAL] The checksum of the file to be replaced.");
        checksumOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumOption);
        Option fileOption = new Option(FILE_ARG, HAS_ARGUMENT,
                "The path to the new file for the replacement. Required unless using the URL argument.");
        fileOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(fileOption);
        Option urlOption = new Option(URL_ARG, HAS_ARGUMENT,
                "The URL for the file to be retreived. Is required, unless the actual file is given.");
        urlOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(urlOption);
        Option replaceChecksumOption = new Option(REPLACE_CHECKSUM_ARG, HAS_ARGUMENT,
                "The checksum for the file to replace with. Required when using the URL argument.");
        replaceChecksumOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(replaceChecksumOption);

        Option checksumTypeOption = new Option(REQUEST_CHECKSUM_TYPE_ARG, HAS_ARGUMENT,
                "[OPTIONAL] The algorithm of checksum to request in the response from the pillars.");
        checksumTypeOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumTypeOption);
        Option checksumSaltOption = new Option(REQUEST_CHECKSUM_SALT_ARG, HAS_ARGUMENT,
                "[OPTIONAL] The salt of checksum to request in the response. Requires the ChecksumType argument.");
        checksumSaltOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumSaltOption);

        Option deleteOption = new Option(DELETE_FILE_ARG, NO_ARGUMENT,
                "If this argument is present, then the file will be removed from the server, "
                        + "when the operation is complete.");
        deleteOption.setRequired(ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(deleteOption);
    }

    /**
     * Run the default validation, and the following replace-file specific validations.
     * Requires the pillar argument, since the client only may replace at one pillar at the time.
     * If settings require a checksum for "destructive requests" - like replace - then the checksum argument is 
     * also required.
     * 
     * It must take either the actual file or an URL is given for the file to replace.
     * Also, if it is an URL is given, then it must also be given the checksum and the file id.
     */
    @Override
    protected void validateArguments() {
        super.validateArguments();
        if(!cmdHandler.hasOption(PILLAR_ARG)) {
            throw new IllegalArgumentException("The pillar argument (-p) must defined for the Replace operation, " +
                    "only single pillar Replaces are allowed");
        }
        if (!cmdHandler.hasOption(CHECKSUM_ARG) &&
                settings.getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests()) {
            throw new IllegalArgumentException("Checksum argument (-C) are mandatory for Replace and replace "
                    + "operations as defined in RepositorySettings.");
        }

        if(cmdHandler.hasOption(FILE_ARG) && cmdHandler.hasOption(URL_ARG)) {
            throw new IllegalArgumentException("Cannot take both a file (-f) and an URL (-u) as argument.");
        }
        if(!cmdHandler.hasOption(FILE_ARG) && !cmdHandler.hasOption(URL_ARG)) {
            throw new IllegalArgumentException("Requires either the file argument (-f) or the URL argument (-u).");
        }
        if(cmdHandler.hasOption(URL_ARG) && !cmdHandler.hasOption(REPLACE_CHECKSUM_ARG)) {
            throw new IllegalArgumentException("The URL argument requires also the checksum argument for the file "
                    + "to replace with (-r).");
        }
        if(cmdHandler.hasOption(URL_ARG) && !cmdHandler.hasOption(FILE_ID_ARG)) {
            throw new IllegalArgumentException("The URL argument requires also the argument for the ID of the "
                    + "file (-i).");
        }
    }

    @Override
    public void performOperation() {
        output.debug("Performing the ReplaceFile operation.");
        OperationEvent finalEvent = replaceTheFile();
        output.completeEvent("Results of the ReplaceFile operation for the file '"
                + cmdHandler.getOptionValue(FILE_ID_ARG) + "'"
                + ": ", finalEvent);
        if(finalEvent.getEventType() == OperationEventType.COMPLETE) {
            System.exit(EXIT_SUCCESS);
        } else {
            System.exit(EXIT_OPERATION_FAILURE);
        }
    }

    /**
     * Initiates the operation and waits for the results.
     * @return The final event for the results of the operation. Either 'FAILURE' or 'COMPLETE'.
     */
    private OperationEvent replaceTheFile() {
        URL url = getURLOrUploadFile();
        String fileID = retrieveFileID();

        ChecksumDataForFileTYPE replaceValidationChecksum = getChecksumDataForDeleteValidation();
        ChecksumDataForFileTYPE newValidationChecksum = getValidationChecksum();
        ChecksumSpecTYPE requestChecksum = getRequestChecksumSpecOrNull();

        output.debug("Initiating the ReplaceFile conversation.");
        CompleteEventAwaiter eventHandler = new ReplaceFileEventHandler(settings, output);
        String pillarID = cmdHandler.getOptionValue(PILLAR_ARG);

        if (requestChecksum != null) {
            output.resultHeader("PillarId results");
        }

        client.replaceFile(getCollectionID(), fileID, pillarID, replaceValidationChecksum,
                requestChecksum, url, getSizeOfFileOrZero(), newValidationChecksum, 
                requestChecksum, eventHandler, null);

        OperationEvent finalEvent = eventHandler.getFinish(); 

        if(cmdHandler.hasOption(DELETE_FILE_ARG)) {
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
        if(cmdHandler.hasOption(FILE_ARG)) {
            return getValidationChecksumDataForFile(findTheFile());            
        } else {
            return getValidationChecksumDataFromArgument(REPLACE_CHECKSUM_ARG);
        }
    }
}
