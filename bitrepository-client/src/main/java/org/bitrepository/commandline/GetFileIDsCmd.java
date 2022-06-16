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
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.commandline.clients.PagingGetFileIDsClient;
import org.bitrepository.commandline.outputformatter.GetFileIDsInfoFormatter;
import org.bitrepository.commandline.outputformatter.GetFileIDsOutputFormatter;
import org.bitrepository.common.utils.SettingsUtils;

/**
 * Perform the GetFileIDs operation.
 */
public class GetFileIDsCmd extends CommandLineClient {
    private final PagingGetFileIDsClient pagingClient;

    /**
     * @param args The arguments for performing the GetFileIDs operation.
     */
    public static void main(String[] args) {
        try {
            GetFileIDsCmd client = new GetFileIDsCmd(args);
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
    protected GetFileIDsCmd(String... args) {
        super(args);
        output.debug("Instantiation GetFileIDClient.");
        GetFileIDsClient client = AccessComponentFactory.getInstance().createGetFileIDsClient(settings,
                securityManager, getComponentID());
        output.debug("Instantiation GetFileID outputFormatter.");
        GetFileIDsOutputFormatter outputFormatter = new GetFileIDsInfoFormatter(output);

        output.debug("Instantiation GetFileID paging client.");
        int pageSize = SettingsUtils.getMaxClientPageSize();
        pagingClient = new PagingGetFileIDsClient(client, getTimeout(), pageSize, outputFormatter, output);
    }

    @Override
    protected void createOptionsForCmdArgumentHandler() {
        super.createOptionsForCmdArgumentHandler();

        Option pillarOption = new Option(Constants.PILLAR_ARG, Constants.HAS_ARGUMENT,
                "[OPTIONAL] " + Constants.PILLAR_DESC);
        pillarOption.setRequired(Constants.ARGUMENT_IS_NOT_REQUIRED);
        cmdHandler.addOption(pillarOption);
    }

    @Override
    protected boolean isFileIDArgumentRequired() {
        return false;
    }

    /**
     * Perform the GetFileIDs operation.
     */
    public void performOperation() {
        output.startupInfo("Performing the GetFileIDs operation.");
        boolean success = pagingClient.getFileIDs(getCollectionID(), getFileIDs(), getPillarIDs());
        if (success) {
            System.exit(Constants.EXIT_SUCCESS);
        } else {
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }
}
