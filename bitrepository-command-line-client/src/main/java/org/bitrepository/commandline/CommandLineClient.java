/*
 * #%L
 * Bitrepository Integrity Service
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

import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.FileIDValidator;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Defines the common functionality for commandline clients.
 */
public abstract class CommandLineClient {
    /** For handling the output.*/
    protected final OutputHandler output = new DefaultOutputHandler(getClass());

    /** The settings for the client.*/
    protected final Settings settings;
    /** The security manager.*/
    protected final SecurityManager securityManager;
    /** The handler for the command line arguments.*/
    protected final CommandLineArgumentsHandler cmdHandler;
    private final FileIDValidator fileIDValidator;

    /**
     * @param args The generic command line arguments for defining the operation.
     */
    protected CommandLineClient(String ... args) {
        output.startupInfo("Initialising arguments...");
        cmdHandler = new CommandLineArgumentsHandler();
        createOptionsForCmdArgumentHandler();
        try {
            cmdHandler.parseArguments(args);
        } catch (ParseException pe) {
            output.error(cmdHandler.listArguments() +
                    "Missing argument: " + pe.getMessage());
            System.exit(Constants.EXIT_ARGUMENT_FAILURE);
        }
        settings = cmdHandler.loadSettings(getComponentID());
        securityManager = cmdHandler.loadSecurityManager(settings);
        fileIDValidator = new FileIDValidator(settings);

        try {
            validateArguments();
        } catch (IllegalArgumentException iae) {
            output.error("Invalid argument: " + iae.getMessage());
            System.exit(Constants.EXIT_ARGUMENT_FAILURE);
        }

        output.startupInfo("Creating client.");
    }

    /**
     * Defines the componentID of the concrete client. Must be specified by in the subclass.
     * @return The componentID of the concrete client.
     */
    protected abstract String getComponentID();

    /**
     * Used for determining whether the fileID Argument is required for the concrete operation.
     * Must be specified by in the subclass.
     * @return Indicates whether the -f
     */
    protected abstract boolean isFileIDArgumentRequired();

    /**
     * Creates the options for the command line argument handler. May be override.
     */
    protected void createOptionsForCmdArgumentHandler() {
        cmdHandler.createDefaultOptions();

        Option collectionOption = new Option(Constants.COLLECTION_ID_ARG, Constants.HAS_ARGUMENT,
                "The id for the collection to perform the operation on.");
        collectionOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
        cmdHandler.addOption(collectionOption);

        Option fileOption = new Option(Constants.FILE_ID_ARG, Constants.HAS_ARGUMENT,
                "The id for the file to perform the operation on.");
        fileOption.setRequired(isFileIDArgumentRequired());
        cmdHandler.addOption(fileOption);

        Option pillarOption = new Option(Constants.PILLAR_ARG, Constants.HAS_ARGUMENT, "[OPTIONAL] The id of the "
                + "pillar where the should be performed. If undefined the operations is performed on all pillars.");
        pillarOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(pillarOption);
    }

    /**
     * @throws IllegalArgumentException One of the arguments wasn't valid.
     */
    protected void validateArguments() {
        if(cmdHandler.hasOption(Constants.FILE_ARG)) {
            fileIDValidator.checkFileID(cmdHandler.getOptionValue(Constants.FILE_ARG));
        }
        if(cmdHandler.hasOption(Constants.COLLECTION_ID_ARG)) {
            List<String> collections = SettingsUtils.getAllCollectionsIDs(settings);
            String collectionArgument = cmdHandler.getOptionValue(Constants.COLLECTION_ID_ARG);
            if (!collections.contains(collectionArgument)) {
                throw new IllegalArgumentException(collectionArgument + " is not a valid collection." +
                        "\nThe following collections are defined: " + collections);
            }
        }
        if(cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            String pillarArgument = cmdHandler.getOptionValue(Constants.PILLAR_ARG);
            List<String> pillarsInCollection = SettingsUtils.getPillarIDsForCollection(settings, getCollectionID());
            if (!pillarsInCollection.contains(pillarArgument)) {
                throw new IllegalArgumentException(pillarArgument + " is not a valid pillar for collection " +
                        getCollectionID() + "\nThe collection contains the following pillars: " + pillarsInCollection);
            }
        }
    }

    /**
     * @return The file ids to request. If a specific file has been given as argument, then it will be returned,
     * otherwise all file ids will be requested.
     */
    protected String getFileIDs() {
        if(cmdHandler.hasOption(Constants.FILE_ARG)) {
            return cmdHandler.getOptionValue(Constants.FILE_ARG);
        } else {
            return null;
        }
    }

    /**
     * @return The collection to use.
     */
    protected String getCollectionID() {
        return cmdHandler.getOptionValue(Constants.COLLECTION_ID_ARG);
    }

    /**
     * Extract the pillar ids. If a specific pillar is given as argument, then it will be returned, but if no such
     * argument has been given, then the list of all pillar ids are given.
     * @return The list of pillars to request for the file ids.
     */
    protected List<String> getPillarIDs() {
        if(cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            return Arrays.asList(cmdHandler.getOptionValue(Constants.PILLAR_ARG));
        } else {
            return SettingsUtils.getPillarIDsForCollection(settings, getCollectionID());
        }
    }

    /**
     * @return The timeout to use for performing the full operation.
     */
    protected long getTimeout() {
        return settings.getRepositorySettings().getClientSettings().getIdentificationTimeout().longValue()
                + settings.getRepositorySettings().getClientSettings().getOperationTimeout().longValue();
    }
}
