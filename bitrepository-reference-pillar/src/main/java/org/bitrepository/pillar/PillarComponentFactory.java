/*
 * #%L
 * Bitrepository Integration
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.lang.reflect.Constructor;

import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.pillar.store.ChecksumStorageModel;
import org.bitrepository.pillar.store.FileStorageModel;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumDAO;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumDatabaseManager;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumStore;
import org.bitrepository.pillar.store.filearchive.CollectionArchiveManager;
import org.bitrepository.protocol.CoordinationLayerException;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.audit.AuditTrailContributerDAOFactory;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.settings.referencesettings.PillarType;

/**
 * Component factory for this module.
 */
public final class PillarComponentFactory {

    /**
     * Private constructor for initialization of the singleton.
     */
    private PillarComponentFactory() {
    }

    /**
     * Creates the pillar from on settings, key-file and pillar-ID.
     * @param pathToSettings The path to the settings file.
     * @param pathToKeyFile The path to the key file (can be empty).
     * @param pillarID The id of the pillar (if null, the pillar-id in settings are used).
     * @return The pillar.
     */
    public static Pillar createPillar(String pathToSettings, String pathToKeyFile, String pillarID) {
        Settings settings = loadSettings(pillarID, pathToSettings);
        SecurityManager securityManager = loadSecurityManager(pathToKeyFile, settings);
        return new Pillar(settings, securityManager);
    }

    /**
     * Instantiates the ChecksumStore.
     * @param settings The settings.
     * @return The ChecksumStore.
     */
    public static ChecksumStore getChecksumStore(Settings settings) {
        DatabaseManager checksumDatabaseManager = new ChecksumDatabaseManager(settings);
        return new ChecksumDAO(checksumDatabaseManager);
    }
    
    /**
     * Instantiates the AuditTrailManager.
     * @param settings The settings.
     * @return The AuditTrailManager.
     */
    public static AuditTrailManager getAuditTrailManager(Settings settings) {
        AuditTrailContributerDAOFactory daoFactory = new AuditTrailContributerDAOFactory();
        return daoFactory.getAuditTrailContributorDAO(
                settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase(),
                settings.getComponentID(), settings);
    }

    
    /**
     * Retrieves the FileStore defined in the settings.
     * @param settings The settings.
     * @return The filestore from settings, or the CollectionArchiveManager, if the setting is missing.
     */
    @SuppressWarnings("unchecked")
    public static FileStore getFileStore(Settings settings) {
        if(settings.getReferenceSettings().getPillarSettings().getFileStoreClass() == null) {
            return new CollectionArchiveManager(settings);
        }
        
        try {
            Class<FileStore> fsClass = (Class<FileStore>) Class.forName(
                    settings.getReferenceSettings().getPillarSettings().getFileStoreClass());
            Constructor<FileStore> fsConstructor = fsClass.getConstructor(Settings.class);
            return fsConstructor.newInstance(settings);
        } catch (Exception e) {
            throw new CoordinationLayerException("Could not instantiate the FileStore", e);
        }
    }
    
    /**
     * Instantiates the PillarModel.
     * @param settings The settings.
     * @param cache The ChecksumCache.
     * @param alarmDispatcher The alarm dispatcher.
     * @return The PillarModel, either for FullReferencePillar or ChecksumPillar.
     */
    public static StorageModel getPillarModel(Settings settings, ChecksumStore cache, AlarmDispatcher alarmDispatcher) {
        PillarType pillarType = settings.getReferenceSettings().getPillarSettings().getPillarType();
        FileExchange fileExchange = ProtocolComponentFactory.createFileExchange(settings);
        if(pillarType == PillarType.CHECKSUM) {
            return new ChecksumStorageModel(cache, alarmDispatcher, settings, fileExchange);
        } else if(pillarType == PillarType.FILE) {
            FileStore archive = getFileStore(settings);
            return new FileStorageModel(archive, cache, alarmDispatcher, settings, fileExchange);
        } else {
            throw new IllegalStateException("Cannot instantiate a pillar of type '" + pillarType + "'.");
        }
    }

    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_SETTINGS = "conf";
    /** The default path for the settings in the development.*/
    private static final String DEFAULT_PATH_TO_KEY_FILE = "conf/pillar.pem";

    /**
     * Method for retrieving the settings for the launcher.
     * @param pathToSettings The path to the settings. If it is null or empty, then the default path is used.
     * @return The settings.
     */
    private static Settings loadSettings(String pillarID, String pathToSettings) {
        String settingsPath;
        if(pathToSettings == null || pathToSettings.isEmpty()) {
            settingsPath = DEFAULT_PATH_TO_SETTINGS;
        } else {
            settingsPath = pathToSettings;
        }

        PillarSettingsProvider settingsLoader =
                new PillarSettingsProvider(new XMLFileSettingsLoader(settingsPath), pillarID);

        return settingsLoader.getSettings();
    }

    /**
     * Instantiates the security manager based on the settings and the path to the key file.
     * @param pathToPrivateKeyFile The path to the key file.
     * @param settings The settings.
     * @return The security manager.
     */
    private static BasicSecurityManager loadSecurityManager(String pathToPrivateKeyFile, Settings settings) {
        String privateKeyFile;
        if(pathToPrivateKeyFile == null || pathToPrivateKeyFile.isEmpty()) {
            privateKeyFile = DEFAULT_PATH_TO_KEY_FILE;
        } else {
            privateKeyFile = pathToPrivateKeyFile;
        }

        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        return new BasicSecurityManager(settings.getRepositorySettings(), privateKeyFile,
                authenticator, signer, authorizer, permissionStore,
                settings.getComponentID());
    }
}
