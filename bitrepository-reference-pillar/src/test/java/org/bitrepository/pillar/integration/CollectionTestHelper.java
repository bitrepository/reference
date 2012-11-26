package org.bitrepository.pillar.integration;
/*
 * #%L
 * Bitrepository Reference Pillar
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

import java.util.Collection;
import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.deletefile.DeleteFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.fileexchange.HttpServerConnector;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;

public class CollectionTestHelper {
    private final Settings settings;
    private final HttpServerConnector httpServer;
    private final SecurityManager securityManager;

    private final GetFileIDsClient getFileIDsClient;
    private final DeleteFileClient deleteFileClient;
    protected PutFileClient putClient;

    public CollectionTestHelper(
            Settings settings,
            HttpServerConnector httpServer) {
        this.settings = settings;
        this.securityManager = new DummySecurityManager();
        this.httpServer = httpServer;

        putClient = ModifyComponentFactory.getInstance().retrievePutClient(
                settings, new DummySecurityManager(), settings.getComponentID()
        );
        getFileIDsClient = AccessComponentFactory.getInstance().createGetFileIDsClient(
                settings, securityManager, settings.getComponentID()
        );
        deleteFileClient = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(
                settings, securityManager, settings.getComponentID()
        );
    }

    public void cleanCollection(Collection<String> pillarIDs) {
    }
}
