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
package org.bitrepository.integrityservice;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.IntegrityDatabaseCreator;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.math.BigInteger;
import java.util.List;

public abstract class IntegrityDatabaseTestCase extends ExtendedTestCase {
    protected Settings settings;
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        customizeSettings();
        SettingsUtils.initialize(settings);
        DerbyDatabaseDestroyer.deleteDatabase(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());

        IntegrityDatabaseCreator integrityDatabaseCreator = new IntegrityDatabaseCreator();
        integrityDatabaseCreator.createIntegrityDatabase(settings, null);
    }
    
    @AfterMethod (alwaysRun = true)
    public void clearDatabase() throws Exception {
        DBConnector connector = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        DatabaseUtils.executeStatement(connector, "DELETE FROM fileinfo", new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM collection_progress", new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM pillarstats", new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM collectionstats", new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM stats", new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM pillar", new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM collections", new Object[0]);
    }
    
    /**
     * Inserts the checksumdata, but ensures that the data can be inserted, by inserting the file-id-data before. 
     * @param cache The integrity cache.
     * @param csData The checksum data.
     * @param pillarID The id of the pillar.
     * @param collectionID The id of the collection.
     */
    protected void insertChecksumDataForModel(IntegrityModel cache, List<ChecksumDataForChecksumSpecTYPE> csData, 
            String pillarID, String collectionID) {
        insertMissingFilesInChecksumDataForModel(cache, csData, pillarID, collectionID);
        cache.addChecksums(csData, pillarID, collectionID);
    }
    
    /**
     * Converts a piece of checksum data into file id data.
     * @param csData The checksum data to convert.
     */
    protected void insertMissingFilesInChecksumDataForModel(IntegrityModel cache, List<ChecksumDataForChecksumSpecTYPE> csData, 
            String pillarID, String collectionID) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        
        for(ChecksumDataForChecksumSpecTYPE entry : csData) {
            FileIDsDataItem dataItem = new FileIDsDataItem();
            dataItem.setFileID(entry.getFileID());
            dataItem.setFileSize(BigInteger.ZERO);
            dataItem.setLastModificationTime(entry.getCalculationTimestamp());
            items.getFileIDsDataItem().add(dataItem);
        } 
        
        res.setFileIDsDataItems(items);
        cache.addFileIDs(res, pillarID, collectionID);
    }
    
    /**
     * Method to modify the by constructor loaded settings. 
     * Default implementation does nothing, so override to change behavior. 
     */
    protected void customizeSettings() { }
}
