/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.integrityclient.cache;

import java.math.BigInteger;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityclient.cache.database.DatabaseStoragedCache;
import org.bitrepository.integrityclient.cache.database.DerbyDBConnector;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DatabaseCacheTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
    }
    
//    @Test(groups = {"regressiontest"})
    @Test(groups = {"databasetest"})
    public void connectionTest() throws Exception {
        addDescription("Testing the connection to the integrity database.");
        String url = "jdbc:derby:integritydb";
        String fileId = "TEST-FILE-ID-" + new Date().getTime();
        String pillarId = "MY-TEST-PILLAR";
        
        Connection connection = DerbyDBConnector.getEmbeddedDBConnection(url);
        Assert.assertNotNull(connection);
        
        DatabaseStoragedCache cache = new DatabaseStoragedCache(connection);

        cache.updateFileIDs(getFileIDsData(fileId), pillarId);
        cache.updateChecksumData(getChecksumResults(fileId), getChecksumSpec(), pillarId);
        
//        Long fileGuid = cache.retrieveFileGuid(fileId);
//        Assert.assertNotNull(fileGuid);
//        System.out.println("File: " + fileId + " : " + fileGuid);
//        
//        Long pillarGuid = cache.retrievePillarGuid(pillarId);
//        Assert.assertNotNull(pillarGuid);
//        System.out.println("Pillar: " + pillarId + " : " + pillarGuid);
//        
//        Long checksumGuid = cache.retrieveChecksumSpecGuid(getChecksumSpec());
//        Assert.assertNotNull(checksumGuid);
//        System.out.println("checksumSpec: " + getChecksumSpec() + " : " + checksumGuid);
//        
        List<String> fileIDs = cache.getAllFileIDs();
        Assert.assertNotNull(fileIDs);
        System.out.println(fileIDs);
    }
    
    private List<ChecksumDataForChecksumSpecTYPE> getChecksumResults(String fileId) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        
        ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
        csData.setChecksumValue(new String("checksum").getBytes());
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setFileID(fileId);
        res.add(csData);
        return res;
    }
    
    private ChecksumSpecTYPE getChecksumSpec() {
        ChecksumSpecTYPE res = new ChecksumSpecTYPE();
        res.setChecksumSalt(new byte[0]);
        res.setChecksumType("MD5");
        return res;
    }
    
    private FileIDsData getFileIDsData(String fileId) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        
        FileIDsDataItem item1 = new FileIDsDataItem();
        item1.setFileID(fileId);
        item1.setFileSize(BigInteger.ONE);
        item1.setLastModificationTime(CalendarUtils.getNow());
        items.getFileIDsDataItem().add(item1);
        
//        FileIDsDataItem item2 = new FileIDsDataItem();
//        item2.setFileID("FILEID-2");
//        item2.setFileSize(BigInteger.TEN);
//        item2.setLastModificationTime(CalendarUtils.getEpoch());
//        items.getFileIDsDataItem().add(item2);
        
        res.setFileIDsDataItems(items);
        return res;
    }
}
