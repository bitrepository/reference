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
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.cli.Option;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
import org.bitrepository.commandline.utils.CompleteEventAwaiter;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Perform the GetFile operation.
 */
public class GetFile {
    /**
     * @param args The arguments for performing the GetFile operation.
     */
    public static void main(String[] args) {
        GetFile getfile = new GetFile(args);
        getfile.performOperation();
    }
    
    /** The component id. */
    private final static String COMPONENT_ID = "GetFileClient";
    
    /** The settings for the get file client.*/
    private final Settings settings;
    /** The security manager.*/
    private final SecurityManager securityManager;
    /** The client for performing the GetFile operation.*/
    private final GetFileClient client;
    /** The handler for the command line arguments.*/
    private final CommandLineArgumentsHandler cmdHandler;
    /** The URL for where the file from the GetFile*/
    private URL fileUrl = null;
    
    /**
     * Constructor.
     * @param args The command line arguments for defining the operation.
     */
    private GetFile(String ... args) {
        System.out.println("Initialising arguments");
        cmdHandler = new CommandLineArgumentsHandler();
        try {
            createOptionsForCmdArgumentHandler();
            cmdHandler.parseArguments(args);
            
            settings = cmdHandler.loadSettings(COMPONENT_ID);
            securityManager = cmdHandler.loadSecurityManager(settings);
            
            System.out.println("Instantiating the GetFileClient");
            client = AccessComponentFactory.getInstance().createGetFileClient(settings, securityManager, 
                    COMPONENT_ID);
        } catch (Exception e) {
            System.err.println(cmdHandler.listArguments());
            throw new IllegalArgumentException(e);
        }
    }
    
    /**
     * Creates the options for the command line argument handler.
     */
    private void createOptionsForCmdArgumentHandler() {
        cmdHandler.createDefaultOptions();
        
        Option fileOption = new Option(Constants.FILE_ARG, Constants.HAS_ARGUMENT, "The id for the file to retrieve.");
        fileOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
        cmdHandler.addOption(fileOption);
        
        Option pillarOption = new Option(Constants.PILLAR_ARG, Constants.HAS_ARGUMENT, "[OPTIONAL] The id of the "
                + "pillar where the file should be retrieved from. If no argument, then the file will be retrieved "
                + "from the fastest pillars.");
        pillarOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(pillarOption);
        
        Option checksumOption = new Option(Constants.LOCATION, Constants.HAS_ARGUMENT, 
                "[OPTIONAL] The location where the file should be placed (either total path or directory). "
                + "If no argument, then in the directory where the script is located.");
        checksumOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumOption);
    }
    
    /**
     * Perform the GetFile operation.
     */
    public void performOperation() {
        System.out.println("Performing the GetFile operation.");
        OperationEvent finalEvent = performConversation();
        System.out.println("Results of the GetFile operation for the file '"
                + cmdHandler.getOptionValue(Constants.FILE_ARG) + "'" 
                + ": " + finalEvent);
        if(finalEvent.getEventType() == OperationEventType.COMPLETE) {
            downloadFile();
            System.exit(0);
        } else {
            System.exit(-1);
        }
    }
    
    /**
     * Initiates the operation and waits for the results.
     * @return The final event for the results of the operation. Either 'FAILURE' or 'COMPLETE'.
     */
    private OperationEvent performConversation() {
        String fileId = cmdHandler.getOptionValue(Constants.FILE_ARG);
        fileUrl = extractUrl(fileId);
        
        CompleteEventAwaiter eventHandler = new CompleteEventAwaiter(settings);
        
        if(cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            String pillarId = cmdHandler.getOptionValue(Constants.PILLAR_ARG);
            client.getFileFromSpecificPillar(fileId, null, fileUrl, pillarId, eventHandler);
        } else {
            client.getFileFromFastestPillar(fileId, null, fileUrl, eventHandler);
        }
        
        return eventHandler.getFinish();
    }
    
    /**
     * Downloads the file from the URL defined in the conversation.
     */
    @SuppressWarnings("deprecation")
    private void downloadFile() {
        File outputFile;
        if(cmdHandler.hasOption(Constants.LOCATION)) {
            File location = new File(cmdHandler.getOptionValue(Constants.LOCATION));
            if(location.isDirectory()) {
                outputFile = new File(location, cmdHandler.getOptionValue(Constants.FILE_ARG));
            } else {
                outputFile = location;
            }
        } else {
            outputFile = new File(cmdHandler.getOptionValue(Constants.FILE_ARG));
        }
        FileExchange fileexchange = ProtocolComponentFactory.getInstance().getFileExchange();
        fileexchange.downloadFromServer(outputFile, fileUrl.toExternalForm());
    }
    
    /**
     * Extracts the URL for where the file should be delivered from the GetFile operation.
     * @param fileId The id of the file.
     * @return The URL where the file should be located.
     */
    @SuppressWarnings("deprecation")
    private URL extractUrl(String fileId) {
        try {
            FileExchange fileexchange = ProtocolComponentFactory.getInstance().getFileExchange();
            return fileexchange.getURL(fileId);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Could not make an URL for the file '" + fileId + "'.", e);
        }
    }
}
