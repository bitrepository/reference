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
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.commandline.clients.PagingGetChecksumsClient;
import org.bitrepository.commandline.outputformatter.GetChecksumsDisagreementInfoFormatter;
import org.bitrepository.commandline.outputformatter.GetChecksumsInfoFormatter;
import org.bitrepository.commandline.outputformatter.GetChecksumsOutputFormatter;

/**
 * Perform the GetChecksums operation.
 */
public class GetChecksums extends CommandLineClient {
    /** The client for performing the GetChecksums operation.*/
    private final PagingGetChecksumsClient pagingClient;

    /**
     * @param args The arguments for performing the GetChecksums operation.
     */
    public static void main(String[] args) {
        GetChecksums client = new GetChecksums(args);
        client.runCommand();
    }

    /**
     * @param args The command line arguments for defining the operation.
     */
    private GetChecksums(String ... args) {
        super(args);
        GetChecksumsClient client = AccessComponentFactory.getInstance().createGetChecksumsClient(settings, 
                securityManager, COMPONENT_ID);
        GetChecksumsOutputFormatter outputFormatter = retrieveOutputFormatter();
        pagingClient = new PagingGetChecksumsClient(client, getTimeout(), outputFormatter, output); 
    }

    @Override
    protected String getComponentID() {
        return COMPONENT_ID;
    }

    @Override
    protected boolean isFileIDArgumentRequired() {
        return false;
    }

    /**
     * Creates the options for the command line argument handler.
     */
    protected void createOptionsForCmdArgumentHandler() {
        super.createOptionsForCmdArgumentHandler();

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
    @Override
    protected void performOperation() {
        ChecksumSpecTYPE spec = getRequestChecksumSpec();
        output.startupInfo("Performing the GetChecksums operation.");
        Boolean success = pagingClient.getChecksums(getCollectionID(), getFileIDs(), 
                getPillarIDs(), spec);
        if(success) {
            System.exit(Constants.EXIT_SUCCESS);
        } else {
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }
    
    /**
     * Retrieves the given output formatter depending on whether or not it requests a given file or all files.
     * @return The output formatter.
     */
    private GetChecksumsOutputFormatter retrieveOutputFormatter() {
        if(cmdHandler.hasOption(Constants.FILE_ID_ARG)) {
            return new GetChecksumsDisagreementInfoFormatter(output);
        } else {
            return new GetChecksumsInfoFormatter(output);
        }
    }
    
}
