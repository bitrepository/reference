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
import org.bitrepository.access.getfileinfos.GetFileInfosClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.commandline.clients.PagingGetFileInfosClient;
import org.bitrepository.commandline.outputformatter.GetFileInfosInfoFormatter;
import org.bitrepository.commandline.outputformatter.GetFileInfosOutputFormatter;
import org.bitrepository.common.utils.SettingsUtils;

/**
 * Perform the GetChecksums operation.
 */
public class GetFileInfosCmd extends CommandLineClient {
    /** The client for performing the GetFileInfos operation.*/
    private final PagingGetFileInfosClient pagingClient;

    /**
     * @param args The arguments for performing the GetChecksums operation.
     */
    public static void main(String[] args) {
        try {
            GetFileInfosCmd client = new GetFileInfosCmd(args);
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
    protected GetFileInfosCmd(String ... args) {
        super(args);
        GetFileInfosClient client = AccessComponentFactory.getInstance().createGetFileInfosClient(settings, 
                securityManager, getComponentID());
        GetFileInfosOutputFormatter outputFormatter = retrieveOutputFormatter();
        int pageSize = SettingsUtils.getMaxClientPageSize();
        pagingClient = new PagingGetFileInfosClient(client, getTimeout(), pageSize, outputFormatter, output); 
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
        ChecksumSpecTYPE spec = getRequestChecksumSpecOrDefault();
        output.startupInfo("Performing the GetChecksums operation.");
        Boolean success = pagingClient.getFileInfos(getCollectionID(), getFileIDs(), 
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
    private GetFileInfosOutputFormatter retrieveOutputFormatter() {
        return new GetFileInfosInfoFormatter(output);
        /*if(cmdHandler.hasOption(Constants.FILE_ID_ARG)) {
            return new GetFileInfosDistributionFormatter(output);
        } else {
            return new GetFileInfosInfoFormatter(output);
        }*/
    }

}