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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
import org.bitrepository.commandline.utils.CompleteEventAwaiter;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Perform the GetFileIDs operation.
 */
public class GetFileIDs {
    /**
     * @param args The arguments for performing the GetFileIDs operation.
     */
    public static void main(String[] args) {
        GetFileIDs getFileIDs = new GetFileIDs(args);
        getFileIDs.performOperation();
    }
    
    /** The component id. */
    private final static String COMPONENT_ID = "GetFileIDsClient";
    
    /** The settings for the client.*/
    private final Settings settings;
    /** The security manager.*/
    private final SecurityManager securityManager;
    /** The client for performing the GetFileIDs operation.*/
    private final GetFileIDsClient client;
    /** The handler for the command line arguments.*/
    private final CommandLineArgumentsHandler cmdHandler;
    
    /**
     * Constructor.
     * @param args The command line arguments for defining the operation.
     */
    private GetFileIDs(String ... args) {
        System.out.println("Initialising arguments");
        cmdHandler = new CommandLineArgumentsHandler();
        try {
            createOptionsForCmdArgumentHandler();
            cmdHandler.parseArguments(args);
            
            settings = cmdHandler.loadSettings(COMPONENT_ID);
            securityManager = cmdHandler.loadSecurityManager(settings);
            
            System.out.println("Instantiating the GetFileIDsClient");
            client = AccessComponentFactory.getInstance().createGetFileIDsClient(settings, securityManager, 
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
        
        Option fileOption = new Option(Constants.FILE_ARG, Constants.HAS_ARGUMENT, "[OPTIONAL] The id for the file to "
                + "retrieve. If no argument, then the file id of all files are retrieved.");
        fileOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(fileOption);
        
        Option pillarOption = new Option(Constants.PILLAR_ARG, Constants.HAS_ARGUMENT, "[OPTIONAL] The id of the "
                + "pillar where the fileids should be retrieved from. If no argument, then the fileids will be "
                + "retrieved from the all pillars.");
        pillarOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(pillarOption);
    }
    
    /**
     * Perform the GetFileIDs operation.
     */
    public void performOperation() {
        System.out.println("Performing the GetFileIDs operation.");
        OperationEvent finalEvent = performConversation();
        System.out.println("Results of the GetFileIDs operation: " + finalEvent);
        if(finalEvent.getType() == OperationEventType.COMPLETE) {
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
        FileIDs fileids = getFileIDs();
        
        List<String> pillarids = getPillarIds();
        
        CompleteEventAwaiter eventHandler = new CompleteEventAwaiter(settings);
        
        client.getFileIDs(pillarids, fileids, null, eventHandler);
        
        return eventHandler.getFinish();
    }
    
    /**
     * @return The file ids to request. If a specific file has been given as argument, then it will be returned, 
     * otherwise all file ids will be requested.
     */
    public FileIDs getFileIDs() {
        if(cmdHandler.hasOption(Constants.FILE_ARG)) {
            return FileIDsUtils.getSpecificFileIDs(cmdHandler.getOptionValue(Constants.FILE_ARG));            
        } else {
            return FileIDsUtils.getAllFileIDs();
        }
    }
    
    /**
     * Extract the pillar ids. If a specific pillar is given as argument, then it will be returned, but if no such
     * argument has been given, then the list of all pillar ids are given.
     * @return The list of pillars to request for the file ids.
     */
    private List<String> getPillarIds() {
        if(cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            return Arrays.asList(cmdHandler.getOptionValue(Constants.PILLAR_ARG));
        }
        
        return new ArrayList<String>(settings.getCollectionSettings().getClientSettings().getPillarIDs());
    }
}
