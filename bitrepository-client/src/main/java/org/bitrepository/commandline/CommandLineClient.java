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

import org.apache.commons.cli.Option;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.DecoderException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.exceptions.InvalidChecksumException;
import org.bitrepository.commandline.output.DefaultOutputHandler;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.commandline.utils.ChecksumExtractionUtils;
import org.bitrepository.commandline.utils.CommandLineArgumentsHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileIDValidator;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.settings.referencesettings.ProtocolType;

import javax.jms.JMSException;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

/**
 * Defines the common functionality for command-line-clients.
 */
public abstract class CommandLineClient {
    private final String componentID;
    protected OutputHandler output = new DefaultOutputHandler(getClass());
    protected Settings settings;
    protected SecurityManager securityManager;
    protected CommandLineArgumentsHandler cmdHandler;
    private final FileIDValidator fileIDValidator;

    /**
     * Runs a specific command-line-client operation.
     * Handles also the closing of connections and deals with exceptions.
     *
     * @throws Exception if an error occurs
     */
    public void runCommand() throws Exception {
        try {
            performOperation();
        } catch (InvalidChecksumException | IllegalArgumentException ie) {
            output.warn(ie.getMessage());
            shutdown();
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        } catch (Exception e) {
            output.error("Unexpected Exception.", e);
            shutdown();
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        } finally {
            shutdown();
        }
    }

    /**
     * @param args The generic command line arguments for defining the operation.
     */
    protected CommandLineClient(String... args) {
        cmdHandler = new CommandLineArgumentsHandler();
        createOptionsForCmdArgumentHandler();
        try {
            cmdHandler.parseArguments(args);
        } catch (ParseException pe) {
            output.error(cmdHandler.listArguments() + "Missing argument: " + pe.getMessage());
            throw new IllegalArgumentException("Missing arguments", pe);
        }
        if (cmdHandler.hasOption(Constants.VERBOSITY_ARG)) {
            output.setVerbosity(true);
        }
        settings = cmdHandler.loadSettings();
        componentID = settings.getComponentID();
        securityManager = cmdHandler.loadSecurityManager(settings);
        fileIDValidator = new FileIDValidator(settings);

        try {
            validateArguments();
        } catch (IllegalArgumentException iae) {
            output.error("Invalid argument: " + iae.getMessage());
            throw iae;
        }

        output.startupInfo("Creating client.");
    }

    /**
     * Method for performing the operation of the specific commandline client.
     */
    protected abstract void performOperation();

    /**
     * Defines the componentID of the concrete client. Must be specified by in the subclass.
     *
     * @return The componentID of the concrete client.
     */
    protected String getComponentID() {
        return componentID;
    }

    /**
     * Used for determining whether the fileID Argument is required for the concrete operation.
     * Must be specified by in the subclass.
     *
     * @return Indicates whether the -f
     */
    protected abstract boolean isFileIDArgumentRequired();

    /**
     * Creates the options for the command line argument handler. May be overridden.
     */
    protected void createOptionsForCmdArgumentHandler() {
        cmdHandler.createDefaultOptions();

        Option collectionOption = new Option(Constants.COLLECTION_ID_ARG, Constants.HAS_ARGUMENT,
                "The id for the collection to perform the operation on.");
        collectionOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
        cmdHandler.addOption(collectionOption);

        Option fileIDOption = new Option(Constants.FILE_ID_ARG, Constants.HAS_ARGUMENT, "The id for the file to perform the operation on.");
        fileIDOption.setRequired(isFileIDArgumentRequired());
        cmdHandler.addOption(fileIDOption);
    }

    /**
     * @throws IllegalArgumentException One of the arguments wasn't valid.
     */
    protected void validateArguments() {
        if (cmdHandler.hasOption(Constants.FILE_ID_ARG)) {
            fileIDValidator.checkFileID(cmdHandler.getOptionValue(Constants.FILE_ID_ARG));
        }
        if (cmdHandler.hasOption(Constants.COLLECTION_ID_ARG)) {
            List<String> collections = SettingsUtils.getAllCollectionsIDs();
            String collectionArgument = cmdHandler.getOptionValue(Constants.COLLECTION_ID_ARG);
            if (!collections.contains(collectionArgument)) {
                throw new IllegalArgumentException(
                        String.format(Locale.ROOT, "'%s' is not a valid collection.\nValid collections are:\n%s",
                                collectionArgument, collections));
            }
        }
        if (cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            String pillarArgument = cmdHandler.getOptionValue(Constants.PILLAR_ARG);
            List<String> pillarsInCollection = SettingsUtils.getPillarIDsForCollection(getCollectionID());
            if (!pillarsInCollection.contains(pillarArgument)) {
                throw new IllegalArgumentException(String.format(Locale.ROOT,
                        "'%s' is not a valid pillar for collection '%s'\nAvailable pillars for the collection are:\n%s", pillarArgument,
                        getCollectionID(), pillarsInCollection));
            }
        }
    }

    /**
     * @return The file ids to request. If a specific file has been given as argument, then it will be returned,
     * otherwise all file ids will be requested.
     */
    protected String getFileIDs() {
        if (cmdHandler.hasOption(Constants.FILE_ID_ARG)) {
            return cmdHandler.getOptionValue(Constants.FILE_ID_ARG);
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
     *
     * @return The list of pillars to request for the file ids.
     */
    protected List<String> getPillarIDs() {
        if (cmdHandler.hasOption(Constants.PILLAR_ARG)) {
            return List.of(cmdHandler.getOptionValue(Constants.PILLAR_ARG));
        } else {
            return SettingsUtils.getPillarIDsForCollection(getCollectionID());
        }
    }

    /**
     * @return The timeout to use for performing the full operation.
     */
    protected Duration getTimeout() {
        return settings.getIdentificationTimeout().plus(settings.getOperationTimeout());
    }

    /**
     * @return The requested checksum spec, or the default checksum from settings if the arguments does not exist.
     */
    protected ChecksumSpecTYPE getRequestChecksumSpecOrDefault() {
        return getRequestChecksumSpec();
    }

    /**
     * @return Returns the default ChecksumSpecType by calling {@link ChecksumUtils#getDefault(Settings)}.
     */
    protected ChecksumSpecTYPE getDefaultChecksumSpec() {
        return ChecksumUtils.getDefault(settings);
    }

    /**
     * @return The requested checksum spec, or null.
     */
    protected ChecksumSpecTYPE getRequestChecksumSpecOrNull() {
        if (!cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG)) {
            return null;
        }

        return getRequestChecksumSpec();
    }

    /**
     * Create the ChecksumSpecTYPE based on the cmd-line arguments.
     * Do not use directly. Use either 'getRequestChecksumSpecOrNull' or 'getRequestChecksumSpecOrDefault' to handle
     * the case, when no request arguments have been defined.
     *
     * @return The requested checksum spec.
     */
    private ChecksumSpecTYPE getRequestChecksumSpec() {
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        try {
            res.setChecksumType(ChecksumExtractionUtils.extractChecksumType(cmdHandler, settings, output));

            if (cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_SALT_ARG)) {
                res.setChecksumSalt(Base16Utils.encodeBase16(cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_SALT_ARG)));
            }
            ChecksumUtils.verifyAlgorithm(res);
        } catch (NoSuchAlgorithmException | DecoderException | IllegalStateException e) {
            output.error(e.getMessage());
            System.exit(1);
        }

        return res;
    }

    /**
     * Finds the file from the arguments.
     *
     * @return The requested file, or null if no file argument was given.
     */
    protected File findTheFile() {
        if (!cmdHandler.hasOption(Constants.FILE_ARG)) {
            output.warn("No file argument was given, so no file can be found.");
            return null;
        }
        String filePath = cmdHandler.getOptionValue(Constants.FILE_ARG);

        File file = new File(filePath);
        if (!file.isFile()) {
            throw new IllegalArgumentException("The file '" + filePath + "' is invalid. It does not exists or it " + "is a directory.");
        }

        return file;
    }

    /**
     * Creates the data structure for encapsulating the validation checksums for validation on the pillars.
     *
     * @return The ChecksumDataForFileTYPE for the pillars to validate the DeleteFile or ReplaceFile operations.
     */
    protected ChecksumDataForFileTYPE getChecksumDataForDeleteValidation() {
        if (!cmdHandler.hasOption(Constants.CHECKSUM_ARG)) {
            return null;
        }

        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        res.setCalculationTimestamp(CalendarUtils.getNow());
        try {
            res.setChecksumValue(Base16Utils.encodeBase16(cmdHandler.getOptionValue(Constants.CHECKSUM_ARG)));
        } catch (DecoderException e) {
            output.error(e.getMessage());
            System.exit(1);
        }
        res.setChecksumSpec(ChecksumUtils.getDefault(settings));

        return res;
    }

    /**
     * Removes the file at the webserver after the operation has finished.
     *
     * @param url The URL where the file should be removed from.
     */
    protected void deleteFileAfterwards(URL url) {
        try {
            FileExchange fileexchange = ProtocolComponentFactory.getInstance().getFileExchange(settings);
            fileexchange.deleteFile(url);
        } catch (Exception e) {
            output.error("Issue regarding removing file from server: " + e.getMessage());
        }
    }

    /**
     * Retrieves the URL for the PutFile operation.
     * Either uploads the actual file to a webserver, or takes the URL argument.
     * Requires either the File argument or the URL argument.
     *
     * @return The URL for the file.
     */
    protected URL getURLOrUploadFile() {
        FileExchange fileExchange = ProtocolComponentFactory.getInstance().getFileExchange(settings);
        if (cmdHandler.hasOption(Constants.FILE_ARG)) {
            File f = findTheFile();
            return fileExchange.putFile(f);
        } else {
            String urlArg = cmdHandler.getOptionValue(Constants.URL_ARG);

            try {
                final URL url = new URL(urlArg);

                ProtocolType protocolType = ProtocolType.fromValue(url.getProtocol().toUpperCase(Locale.ROOT));
                if (protocolType != ProtocolType.FILE) {
                    // Test if URL can actually be opened to exit early
                    // - otherwise checksum pillars will still receive and store checksum.
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    int responseCode = connection.getResponseCode();

                    if (responseCode > 399) {
                        throw new Exception("Http URL Connection ResponseCode: " + responseCode);
                    }
                }

                return url;
            } catch (Exception e) {
                throw new IllegalArgumentException("The URL argument is either empty or not a valid URL: " + urlArg, e);
            }
        }
    }

    /**
     * @return The size of the actual file, or 0 if no file argument is given.
     */
    protected long getSizeOfFileOrZero() {
        if (cmdHandler.hasOption(Constants.FILE_ARG)) {
            return findTheFile().length();
        } else {
            return 0L;
        }
    }

    /**
     * Creates the data structure for encapsulating the validation checksums for validation of the PutFile
     * or ReplaceFile operations.
     *
     * @param file The file to have the checksum calculated.
     * @return The ChecksumDataForFileTYPE for the pillars to validate the operations.
     */
    protected ChecksumDataForFileTYPE getValidationChecksumDataForFile(File file) {
        ChecksumSpecTYPE csSpec = ChecksumUtils.getDefault(settings);

        String checksum = ChecksumUtils.generateChecksum(file, csSpec);

        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        res.setCalculationTimestamp(CalendarUtils.getNow());
        res.setChecksumSpec(csSpec);
        try {
            res.setChecksumValue(Base16Utils.encodeBase16(checksum));
        } catch (DecoderException e) {
            output.error(e.getMessage());
            System.exit(1);
        }

        return res;
    }

    /**
     * Creates the data structure for encapsulating the validation checksums for validation of the PutFile
     * or ReplaceFile operations.
     *
     * @param arg The name of the argument to retrieve the checksum from
     *            (must likely Constants.CHECKSUM_ARG or Constants.REPLACE_CHECKSUM_ARG)
     * @return The ChecksumDataForFileTYPE for the pillars to validate the operations.
     */
    protected ChecksumDataForFileTYPE getValidationChecksumDataFromArgument(String arg) {
        if (!cmdHandler.hasOption(arg)) {
            return null;
        }
        ChecksumSpecTYPE csSpec = ChecksumUtils.getDefault(settings);

        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        res.setCalculationTimestamp(CalendarUtils.getNow());
        res.setChecksumSpec(csSpec);
        try {
            res.setChecksumValue(Base16Utils.encodeBase16(cmdHandler.getOptionValue(arg)));
        } catch (DecoderException e) {
            System.exit(1);
        }


        return res;
    }

    /**
     * Extracts the id of the file to be put.
     *
     * @return Returns either the value of the file ID argument, or no such option, then the name of the file.
     */
    protected String retrieveFileID() {
        if (cmdHandler.hasOption(Constants.FILE_ID_ARG)) {
            return cmdHandler.getOptionValue(Constants.FILE_ID_ARG);
        } else {
            return findTheFile().getName();
        }
    }

    /**
     * Closes the connections, e.g. to the message-bus.
     *
     * @throws JMSException If the message-bus cannot be closed.
     */
    public void shutdown() throws JMSException {
        MessageBus bus = MessageBusManager.getMessageBus();
        if (bus != null) {
            bus.close();
        }
    }
}
