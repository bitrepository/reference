/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id: ReferencePillarLauncher.java 685 2012-01-06 16:35:17Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/ReferencePillarLauncher.java $
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Method for launching the ReferencePillar. 
 * It just loads the configurations and uses them to create the PillarSettings needed for starting the ReferencePillar.
 */
public final class ReferencePillarLauncher {
    /** The default path to the collection id during development.*/
    private static final String DEFAULT_COLLECTION_ID = "bitrepository-devel";
    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_SETTINGS = "settings/xml";
    /**  Objects for the Security module. */
    private static MessageAuthenticator authenticator;
    private static MessageSigner signer;
    private static OperationAuthorizor authorizer;
    private static PermissionStore permissionStore;
    private static SecurityManager securityManager;

    /**
     * Private constructor. To prevent instantiation of this utility class.
     */
    private ReferencePillarLauncher() { }
    
    /**
     * @param args <ol>
     * <li> The path to the directory containing the settings. See {@link XMLFileSettingsLoader} for details.</li>
     * <li> The collection ID to load the settings for.</li>
     * </ol>
     */
    public static void main(String[] args) {
        String collectionId;
        String pathToSettings;
        String privateKeyFile = "foobar";
        if(args.length >= 2) {
            collectionId = args[0];
            pathToSettings = args[1];
        } else if(args.length == 1) {
            pathToSettings = args[0];
            collectionId = ".";
        } else {
            collectionId = DEFAULT_COLLECTION_ID;
            pathToSettings = DEFAULT_PATH_TO_SETTINGS;
        }
        
        SettingsProvider settingsLoader = new SettingsProvider(
                new XMLFileSettingsLoader(pathToSettings));
        try {
            Settings settings = settingsLoader.getSettings(collectionId);
            permissionStore = new PermissionStore();
            authenticator = new BasicMessageAuthenticator(permissionStore);
            signer = new BasicMessageSigner();
            authorizer = new BasicOperationAuthorizor(permissionStore);
            securityManager = new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile, 
                    authenticator, signer, authorizer, permissionStore);
            
            ReferencePillarComponentFactory.getInstance().getPillar(MessageBusManager.getMessageBus(settings, securityManager), 
                    settings);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }
}
