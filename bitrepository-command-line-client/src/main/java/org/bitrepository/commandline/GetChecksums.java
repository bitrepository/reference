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
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.ContributorQueryUtils;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.commandline.clients.PagingGetChecksumsClient;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.outputformatter.GetChecksumsInfoFormatter;
import org.bitrepository.commandline.outputformatter.GetChecksumsOutputFormatter;
import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
import org.bitrepository.commandline.utils.CompleteEventAwaiter;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Perform the GetChecksums operation.
 */
public class GetChecksums {
    /**
     * @param args The arguments for performing the GetChecksums operation.
     */
    public static void main(String[] args) {
        GetChecksums getFileIDs = new GetChecksums(args);
        getFileIDs.performOperation();
    }
    
    /** For handling the output.*/
    private final OutputHandler output = new DefaultOutputHandler(getClass());
    
    /** The component id. */
    private final static String COMPONENT_ID = "GetChecksumsClient";
    
    /** The settings for the client.*/
    private final Settings settings;
    /** The security manager.*/
    private final SecurityManager securityManager;
    /** The client for performing the GetChecksums operation.*/
    private final PagingGetChecksumsClient pagingClient;
    /** The handler for the command line arguments.*/
    private final CommandLineArgumentsHandler cmdHandler;
    
    /**
     * Constructor.
     * @param args The command line arguments for defining the operation.
     */
    private GetChecksums(String ... args) {
        output.startupInfo("Initialising arguments for the GetChecksums operation");
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
        
        output.debug("Instantiating the GetChecksumsClient");
        GetChecksumsClient client = AccessComponentFactory.getInstance().createGetChecksumsClient(settings, 
                securityManager, COMPONENT_ID);
        GetChecksumsOutputFormatter outputFormatter = new GetChecksumsInfoFormatter(output);
        pagingClient = new PagingGetChecksumsClient(client, getTimeout(), outputFormatter, output); 
    }
    
    /**
     * Creates the options for the command line argument handler.
     */
    private void createOptionsForCmdArgumentHandler() {
        cmdHandler.createDefaultOptions();
        
        Option fileOption = new Option(Constants.FILE_ARG, Constants.HAS_ARGUMENT, "[OPTIONAL] The id for the file to "
                + "retrieve the checksum for. If no argument, then the checksum of all files are retrieved.");
        fileOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(fileOption);

        Option collectionOption = new Option(Constants.COLLECTION_ID_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] The id for the collection to "
                + "retrieve the checksum for. If no argument, then first collection in the settings are used.");
        collectionOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(collectionOption);
        
        Option pillarOption = new Option(Constants.PILLAR_ARG, Constants.HAS_ARGUMENT, "[OPTIONAL] The id of the "
                + "pillar where the checksums should be retrieved from. If no argument, then the checksums will be "
                + "retrieved from the all pillars.");
        pillarOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(pillarOption);
        
        Option checksumTypeOption = new Option(Constants.REQUEST_CHECKSUM_TYPE_ARG, Constants.HAS_ARGUMENT, 
                "[OPTIONAL] The algorithm of checksum to request in the response from the pillars. "
                + "If no such argument is given, then the default from settings is retrieved.");
        checksumTypeOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumTypeOption);
        Option checksumSaltOption = new Option(Constants.REQUEST_CHECKSUM_SALT_ARG, Constants.HAS_ARGUMENT, 
                "[OPTIONAL] The salt of checksum to request in the response. Requires the ChecksumType argument.");
        checksumSaltOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(checksumSaltOption);
    }
    
    /**
     * Perform the GetChecksums operation.
     */
    public void performOperation() {
        output.debug("Performing the GetChecksums operation.");
        Boolean success = pagingClient.getChecksums(getCollectionID(), getFileIDs(), 
                getPillarIDs(), getRequestChecksumSpec());
        if(success) {
            System.exit(Constants.EXIT_SUCCESS);
        } else {
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }
    
    /**
     * @return The file ids to request. If a specific file has been given as argument, then it will be returned, 
     * otherwise all file ids will be requested.
     */
    public String getFileIDs() {
        if(cmdHandler.hasOption(Constants.FILE_ARG)) {
            return cmdHandler.getOptionValue(Constants.FILE_ARG);
        } else {
            return null;
        }
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
     * Extract the pillar ids. If a specific pillar is given as argument, then it will be returned, but if no such
     * argument has been given, then the list of all pillar ids are given.
     * @return The list of pillars to request for the file ids.
     */
    private ContributorQuery[] getContributorQuerys() {
        if(cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            String pillarId = cmdHandler.getOptionValue(Constants.PILLAR_ARG);
            return ContributorQueryUtils.createFullContributorQuery(Arrays.asList(pillarId));
        } else return null;
    }
    
    /**
     * @return The requested checksum spec, or the default checksum from settings if the arguments does not exist.
     */
    private ChecksumSpecTYPE getRequestChecksumSpec() {
        if(!cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG)) {
            return ChecksumUtils.getDefault(settings);
        }
        
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumType(ChecksumType.fromValue(cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));
        
        if(cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_SALT_ARG)) {
            res.setChecksumSalt(Base16Utils.encodeBase16(cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG)));
        }
        return res;
    }
    
    /**
     * Extract the pillar ids. If a specific pillar is given as argument, then it will be returned, but if no such
     * argument has been given, then the list of all pillar ids are given.
     * @return The list of pillars to request for the file ids.
     */
    private List<String> getPillarIDs() {
        if(cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            return Arrays.asList(cmdHandler.getOptionValue(Constants.PILLAR_ARG));
        }
        
        return new ArrayList<String>(settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID());
    }
    
    private long getTimeout() {
        return settings.getRepositorySettings().getClientSettings().getIdentificationTimeout().longValue()
                + settings.getRepositorySettings().getClientSettings().getOperationTimeout().longValue();
    }
}
