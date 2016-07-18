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
package org.bitrepository.commandline.utils;

import java.nio.file.Paths;
import java.util.Collection;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.bitrepository.client.CommandLineSettingsProvider;
import org.bitrepository.commandline.Constants;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.security.SecurityManagerUtil;

/**
 * Interface for handling the command line arguments.
 */
public class CommandLineArgumentsHandler {
    /** The parser of the command line arguments.*/
    protected final CommandLineParser parser;
    /** The options for the command line arguments*/
    protected final Options options;
    /** The command line. */
    protected CommandLine cmd = null;
    /** The settings.*/
    protected Settings settings = null;
    
    /**
     * Constructor.
     */
    public CommandLineArgumentsHandler() {
        parser = new DefaultParser();
        options = new Options();
    }
    
    /**
     * Parses the commandline arguments.
     * @param args The command line arguments to pass.
     * @throws ParseException If the arguments does not parse. E.g. arguments missing or too many arguments.
     */
    public void parseArguments(String ... args) throws ParseException {
        cmd = parser.parse(options, args, Constants.NOT_ALLOWING_UNDEFINED_ARGUMENTS);
    }
    
    /**
     * For validating that the command line has been instantiated.
     */
    private void ensureThatCmdHasBeenInitialised() {
        ArgumentValidator.checkNotNull(cmd, "No argument has been parsed from the command line.");
    }
    
    /**
     * Creates the default options for the command line arguments for the clients.
     */
    public void createDefaultOptions() {
        Option settingsOption = new Option(Constants.SETTINGS_ARG, true, "The path to the directory with the settings "
                + "files for the client");
        settingsOption.setRequired(Constants.ARGUMENT_IS_REQUIRED);
        options.addOption(settingsOption);
        
        Option privateKeyOption = new Option(Constants.PRIVATE_KEY_ARG, true, "The path to the file containing "
                + "the private key.");
        privateKeyOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);        
        options.addOption(privateKeyOption);
        
        Option verbosity = new Option(Constants.VERBOSITY_ARG, false, "Makes the client more verbose");
        verbosity.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        options.addOption(verbosity);
    }
    
    /**
     * @param option The option to add to the command line.
     */
    public void addOption(Option option) {
        options.addOption(option);
    }
    
    /**
     * @param optionName The name of the option to extract the value for.
     * @return The value corresponding to the given option name.
     */
    public String getOptionValue(String optionName) {
        ensureThatCmdHasBeenInitialised();
        return cmd.getOptionValue(optionName);
    }
    
    /**
     * @param optionName The name of the option to validate whether exists.
     * @return Whether any arguments for the options have been given.
     */
    public boolean hasOption(String optionName) {
        ensureThatCmdHasBeenInitialised();
        return cmd.hasOption(optionName);
    }
    
    /**
     * @return Lists the possible arguments in a human readable format.
     */
    @SuppressWarnings("unchecked")
    public String listArguments() {
        StringBuilder res = new StringBuilder();
        res.append("Takes the following arguments:\n");
        for(Option option : (Collection<Option>) options.getOptions()) {
            res.append("-" + option.getOpt() + " " + option.getDescription() + "\n");
        }
        return res.toString();
    }    
    
    /**
     * Method for retrieving the settings for the launcher.
     * This will be based on the argument for the path to the settings.
     * @return The settings.
     */
    public Settings loadSettings() {
        ensureThatCmdHasBeenInitialised();
        if(settings == null) {
            SettingsProvider settingsLoader =
                new CommandLineSettingsProvider(new XMLFileSettingsLoader(cmd.getOptionValue(Constants.SETTINGS_ARG)));
            settings = settingsLoader.getSettings();
        }
        SettingsUtils.initialize(settings);
        return settings;
    }
    
    /**
     * Instantiates the security manager based on the settings and argument for the path to the key file.
     * @param settings The settings.
     * @return The security manager.
     */
    public SecurityManager loadSecurityManager(Settings settings) {
        ArgumentValidator.checkNotNull(settings, "Settings settings");
        ensureThatCmdHasBeenInitialised();
        String privateKeyFile = cmd.getOptionValue(Constants.PRIVATE_KEY_ARG, "");
        return SecurityManagerUtil.getSecurityManager(settings, Paths.get(privateKeyFile));
    }
}
