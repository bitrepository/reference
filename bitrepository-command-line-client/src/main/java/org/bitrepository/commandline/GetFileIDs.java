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

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.commandline.clients.PagingGetFileIDsClient;
import org.bitrepository.commandline.outputformatter.GetFileIDsInfoFormatter;
import org.bitrepository.commandline.outputformatter.GetFileIDsOutputFormatter;

/**
 * Perform the GetFileIDs operation.
 */
public class GetFileIDs extends CommandLineClient {
    /** The client for performing the actual operation.*/
    private final PagingGetFileIDsClient pagingClient;

    /**
     * @param args The arguments for performing the GetFileIDs operation.
     */
    public static void main(String[] args) {
        GetFileIDs client = new GetFileIDs(args);
        client.runCommand();
    }

    /**
     * @param args The command line arguments for defining the operation.
     */
    private GetFileIDs(String ... args) {
        super(args);
        output.debug("Instantiation GetFileIDClient.");
        GetFileIDsClient client = AccessComponentFactory.getInstance().createGetFileIDsClient(settings, 
                securityManager, COMPONENT_ID);
        output.debug("Instantiation GetFileID outputFormatter.");
        GetFileIDsOutputFormatter outputFormatter = new GetFileIDsInfoFormatter(output);

        output.debug("Instantiation GetFileID paging client.");
        pagingClient = new PagingGetFileIDsClient(client, getTimeout(), outputFormatter, output); 
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
     * Perform the GetFileIDs operation.
     */
    public void performOperation() {
        output.startupInfo("Performing the GetFileIDs operation.");
        Boolean success = pagingClient.getFileIDs(getCollectionID(), getFileIDs(), getPillarIDs());
        if(success) {
            System.exit(Constants.EXIT_SUCCESS);
        } else {
            System.exit(Constants.EXIT_OPERATION_FAILURE);
        }
    }
}
