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

import java.io.File;
import java.net.URL;

import org.apache.commons.cli.Option;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.ReplaceFileEventHandler;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.replacefile.ReplaceFileClient;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;

/**
 * Replace a file from the collection.
 * 
 */
public class ReplaceFile extends CommandLineClient {
    private final static String COMPONENT_ID = "ReplaceFileClient";
    /** The client for performing the ReplaceFile operation.*/
    private final ReplaceFileClient client;

    /**
     * @param args The arguments for performing the ReplaceFile operation.
     */
    public static void main(String[] args) {
        ReplaceFile client = new ReplaceFile(args);
        client.runCommand();
    }

    /**
     * @param args The command line arguments for defining the operation.
     */
    private ReplaceFile(String ... args) {
        super(args);
        client = ModifyComponentFactory.getInstance().retrieveReplaceFileClient(settings, securityManager,
                COMPONENT_ID);
    }

    @Override
    protected String getComponentID() {
        return COMPONENT_ID;
    }

    @Override
    protected boolean isFileIDArgumentRequired() {
        return false;
    }

    @Override
    protected void createOptionsForCmdArgumentHandler() {
        super.createOptionsForCmdArgumentHandler();

        Option checksumOption = new Option(Constants.CHECKSUM_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] The checksum of the file to be replaced.");
        checksumOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumOption);
        Option fileOption = new Option(Constants.FILE_ARG, Constants.HAS_ARGUMENT,
                "The path to the new file for the replacement");
        fileOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
        cmdHandler.addOption(fileOption);

        Option checksumTypeOption = new Option(Constants.REQUEST_CHECKSUM_TYPE_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] The algorithm of checksum to request in the response from the pillars.");
        checksumTypeOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumTypeOption);
        Option checksumSaltOption = new Option(Constants.REQUEST_CHECKSUM_SALT_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] The salt of checksum to request in the response. Requires the ChecksumType argument.");
        checksumSaltOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumSaltOption);
    }

    @Override
    protected void validateArguments() {
        super.validateArguments();
        if(!cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            throw new IllegalArgumentException("The pillar argument -p must defined for the Replace operation, " +
                    "only single pillar Replaces are allowed");
        }
        if (!cmdHandler.hasOption(Constants.CHECKSUM_ARG) &&
                settings.getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests()) {
            throw new IllegalArgumentException("Checksum argument (-C) are mandatory for Replace and replace operations" +
                    "as defined in RepositorySettings.");
        }
    }

    /**
     * Perform the ReplaceFile operation.
     */
    public void performOperation() {
        output.debug("Performing the ReplaceFile operation.");
        OperationEvent finalEvent = replaceTheFile();
        output.completeEvent("Results of the ReplaceFile operation for the file '"
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
    private OperationEvent replaceTheFile() {
        
        File f = findTheFile();
        FileExchange fileexchange = ProtocolComponentFactory.getInstance().getFileExchange(settings);
        URL url = fileexchange.uploadToServer(f);

        ChecksumDataForFileTYPE replaceValidationChecksum = getValidationChecksumForOldFile();
        ChecksumDataForFileTYPE newValidationChecksum = getValidationChecksum(f);
        ChecksumSpecTYPE requestChecksum = getRequestChecksumSpec();

        output.debug("Initiating the ReplaceFile conversation.");
        CompleteEventAwaiter eventHandler = new ReplaceFileEventHandler(settings, output);
        String pillarId = cmdHandler.getOptionValue(Constants.PILLAR_ARG);
        
        if (requestChecksum != null) {
            output.resultHeader("PillarId results");
        }
        
        client.replaceFile(getCollectionID(), retrieveTheName(f), pillarId, replaceValidationChecksum, 
                requestChecksum, url, f.length(), newValidationChecksum, 
                requestChecksum, eventHandler, "");

        return eventHandler.getFinish();
    }
    
    /**
     * Finds the file from the arguments.
     * @return The requested file.
     */
    private File findTheFile() {
        String filePath = cmdHandler.getOptionValue(Constants.FILE_ARG);

        File file = new File(filePath);
        if(!file.isFile()) {
            throw new IllegalArgumentException("The file '" + filePath + "' is invalid. It does not exists or it "
                    + "is a directory.");
        }

        return file;
    }

    /**
     * Extracts the id of the file to be replaced.
     * @return The either the value of the file id argument, or no such option, then the name of the file.
     */
    private String retrieveTheName(File f) {
        if(cmdHandler.hasOption(Constants.FILE_ID_ARG)) {
            return cmdHandler.getOptionValue(Constants.FILE_ID_ARG);
        } else {
            return f.getName();
        }
    }

    /**
     * Creates the data structure for encapsulating the validation checksums for validation of the PutFile operation.
     * @param file The file to have the checksum calculated.
     * @return The ChecksumDataForFileTYPE for the pillars to validate the PutFile operation.
     */
    private ChecksumDataForFileTYPE getValidationChecksum(File file) {
        ChecksumSpecTYPE csSpec = ChecksumUtils.getDefault(settings);
        String checksum = ChecksumUtils.generateChecksum(file, csSpec);

        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        res.setCalculationTimestamp(CalendarUtils.getNow());
        res.setChecksumSpec(csSpec);
        res.setChecksumValue(Base16Utils.encodeBase16(checksum));

        return res;
    }
    
    /**
     * Creates the data structure for encapsulating the validation checksums for validation on the pillars.
     * @return The ChecksumDataForFileTYPE for the pillars to validate the ReplaceFile operation.
     */
    private ChecksumDataForFileTYPE getValidationChecksumForOldFile() {
        if(!cmdHandler.hasOption(Constants.CHECKSUM_ARG)) {
            return null;
        }

        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        res.setCalculationTimestamp(CalendarUtils.getNow());
        res.setChecksumSpec(ChecksumUtils.getDefault(settings));
        res.setChecksumValue(Base16Utils.encodeBase16(cmdHandler.getOptionValue(Constants.CHECKSUM_ARG)));

        return res;
    }
}
