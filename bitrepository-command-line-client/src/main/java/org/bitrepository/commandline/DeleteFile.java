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
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
import org.bitrepository.commandline.utils.CompleteEventAwaiter;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.deletefile.DeleteFileClient;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Deleting a file from the collection.
 */
public class DeleteFile {
    /**
     * @param args The arguments for performing the DeleteFile operation.
     */
    public static void main(String[] args) {
        DeleteFile deletefile = new DeleteFile(args);
        deletefile.performOperation();
    }

    /** For handling the output.*/
    private final OutputHandler output = new DefaultOutputHandler(getClass());
    /** The component id. */
    private final static String COMPONENT_ID = "DeleteFileClient";

    /** The settings for the delete file client.*/
    private final Settings settings;
    /** The security manager.*/
    private final SecurityManager securityManager;
    /** The client for performing the DeleteFile operation.*/
    private final DeleteFileClient client;
    /** The handler for the command line arguments.*/
    private final CommandLineArgumentsHandler cmdHandler;

    /**
     * Constructor.
     * @param args The command line arguments for defining the operation.
     */
    private DeleteFile(String ... args) {
        output.startupInfo("Initialising arguments for the DeleteFile operation");
        cmdHandler = new CommandLineArgumentsHandler();

        try {
            createOptionsForCmdArgumentHandler();
            cmdHandler.parseArguments(args);
        } catch (Exception e) {
            output.error(cmdHandler.listArguments(), e);
            System.exit(Constants.EXIT_ARGUMENT_FAILURE);
        }

        settings = cmdHandler.loadSettings(COMPONENT_ID);
        securityManager = cmdHandler.loadSecurityManager(settings);

        output.debug("Instantiating the DeleteFileClient");
        client = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(settings, securityManager,
                COMPONENT_ID);
    }

    /**
     * Creates the options for the command line argument handler.
     */
    private void createOptionsForCmdArgumentHandler() {
        cmdHandler.createDefaultOptions();

        Option collectionOption = new Option(Constants.COLLECTION_ID_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] The id for the collection to "
                        + "retrieve the checksum for. If no argument, then first collection in the settings are used.");
        collectionOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(collectionOption);

        Option fileOption = new Option(Constants.FILE_ARG, Constants.HAS_ARGUMENT, "The id for the file to delete.");
        fileOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
        cmdHandler.addOption(fileOption);

        Option pillarOption = new Option(Constants.PILLAR_ARG, Constants.HAS_ARGUMENT, "The id of the pillar"
                + " where the file should be delete. If no argument, then the file will be deleted at all pillars.");
        cmdHandler.addOption(pillarOption);

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

    /**
     * Perform the DeleteFile operation.
     */
    public void performOperation() {
        output.debug("Performing the DeleteFile operation.");
        OperationEvent finalEvent = deleteTheFile();
        output.completeEvent("Results of the DeleteFile operation for the file '"
                + cmdHandler.getOptionValue(Constants.FILE_ARG) + "'"
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
        String fileId = cmdHandler.getOptionValue(Constants.FILE_ARG);

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
     * @return The collection to use. If no collection has been idicated the first collection in the settings is used
     * . .
     */
    public String getCollectionID() {
        if(cmdHandler.hasOption(Constants.COLLECTION_ID_ARG)) {
            return cmdHandler.getOptionValue(Constants.COLLECTION_ID_ARG);
        } else {
            return settings.getCollections().get(0).getID();
        }
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
