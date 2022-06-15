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
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.eventhandler.CompleteEventAwaiter;
import org.bitrepository.commandline.eventhandler.GetFileEventHandler;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Perform the GetFile operation.
 */
public class GetFileCmd extends CommandLineClient {
    private final GetFileClient client;
    private URL fileUrl = null;

    /**
     * @param args The arguments for performing the GetFile operation.
     */
    public static void main(String[] args) {
        try {
            GetFileCmd client = new GetFileCmd(args);
            client.runCommand();
        } catch (IllegalArgumentException iae) {
            System.exit(Constants.EXIT_ARGUMENT_FAILURE);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }

    /**
     * @param args The command line arguments for defining the operation.
     */
    protected GetFileCmd(String... args) {
        super(args);
        client = AccessComponentFactory.getInstance().createGetFileClient(settings, securityManager,
                getComponentID());
    }

    @Override
    protected boolean isFileIDArgumentRequired() {
        return true;
    }

    @Override
    protected void createOptionsForCmdArgumentHandler() {
        super.createOptionsForCmdArgumentHandler();

        Option pillarOption = new Option(Constants.PILLAR_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] " + Constants.PILLAR_DESC);
        pillarOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(pillarOption);

        Option checksumOption = new Option(Constants.LOCATION_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] The location where the file should be placed (either total path or directory). "
                        + "If no argument, then the file is placed in the directory where the script is located.");
        checksumOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumOption);
    }

    /**
     * Perform the GetFile operation.
     */
    public void performOperation() {
        String fileArg = cmdHandler.getOptionValue(Constants.FILE_ID_ARG);
        output.startupInfo("Getting " + fileArg);
        OperationEvent finalEvent = performConversation();
        output.debug("Results of the GetFile operation for the file '"
                + cmdHandler.getOptionValue(Constants.FILE_ID_ARG) + "'"
                + ": " + finalEvent);
        if (finalEvent.getEventType() == OperationEventType.COMPLETE) {
            downloadFile();
            output.resultLine(fileArg + " retrieved");
            System.exit(Constants.EXIT_SUCCESS);
        } else {
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }

    /**
     * Initiates the operation and waits for the results.
     *
     * @return The final event for the results of the operation. Either 'FAILURE' or 'COMPLETE'.
     */
    private OperationEvent performConversation() {
        String fileID = cmdHandler.getOptionValue(Constants.FILE_ID_ARG);
        fileUrl = extractUrl(fileID);

        CompleteEventAwaiter eventHandler = new GetFileEventHandler(settings, output);
        output.debug("Initiating the GetFile conversation.");

        if (cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            String pillarID = cmdHandler.getOptionValue(Constants.PILLAR_ARG);
            client.getFileFromSpecificPillar(getCollectionID(), fileID, null, fileUrl, pillarID, eventHandler, null);
        } else {
            client.getFileFromFastestPillar(getCollectionID(), fileID, null, fileUrl, eventHandler, null);
        }

        return eventHandler.getFinish();
    }

    /**
     * Downloads the file from the URL defined in the conversation.
     */
    private void downloadFile() {
        output.debug("Downloading the file.");
        File outputFile;
        if (cmdHandler.hasOption(Constants.LOCATION_ARG)) {
            File location = new File(cmdHandler.getOptionValue(Constants.LOCATION_ARG));
            if (location.isDirectory()) {
                outputFile = new File(location, cmdHandler.getOptionValue(Constants.FILE_ID_ARG));
            } else {
                outputFile = location;
            }
        } else {
            outputFile = new File(cmdHandler.getOptionValue(Constants.FILE_ID_ARG));
        }
        FileExchange fileexchange = ProtocolComponentFactory.getInstance().getFileExchange(settings);
        fileexchange.getFile(outputFile, fileUrl.toExternalForm());
    }

    /**
     * Extracts the URL for where the file should be delivered from the GetFile operation.
     *
     * @param fileID The id of the file.
     * @return The URL where the file should be located.
     */
    private URL extractUrl(String fileID) {
        try {
            FileExchange fileexchange = ProtocolComponentFactory.getInstance().getFileExchange(settings);
            return fileexchange.getURL(fileID);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Could not make an URL for the file '" + fileID + "'.", e);
        }
    }
}
