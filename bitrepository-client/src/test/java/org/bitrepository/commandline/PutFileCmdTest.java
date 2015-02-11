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

import static org.testng.Assert.fail;

import java.util.Date;

import org.bitrepository.client.DefaultFixtureClientTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class PutFileCmdTest extends DefaultFixtureClientTest {
    private static final String SETTINGS_DIR = "settings/xml/bitrepository-devel";
    private static final String KEY_FILE = "KeyFile";
    private static final String DEFAULT_CHECKSUM = "0123456789";

    private String DEFAULT_COLLECTION_ID;
    
    @BeforeMethod(alwaysRun = true)
    public void setupClient() throws Exception {
    	DEFAULT_COLLECTION_ID = settingsForTestClient.getCollections().get(0).getID();
    }
    
    @Test(groups = { "regressiontest" })
    public void successScenarioTest() throws Exception {
    	addDescription("Tests the scenario, where the arguments are OK.");
    	String[] args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-u" + DEFAULT_UPLOAD_FILE_ADDRESS, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-c" + DEFAULT_COLLECTION_ID, 
    			"-i" + DEFAULT_FILE_ID};
    	new PutFile(args);
    }

    @Test(groups = { "regressiontest" })
    public void missingCollectionScenarioTest() throws Exception {
    	addDescription("Tests the scenario, where the collection arguments is missing.");
    	String[] args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-u" + DEFAULT_UPLOAD_FILE_ADDRESS, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-i" + DEFAULT_FILE_ID};
    	try {
    		new PutFile(args);
            fail("Should fail.");
    	} catch (IllegalArgumentException e) {
    		// expected.
    	}
    }

    @Test(groups = { "regressiontest" })
    public void pillarScenarioTest() throws Exception {
    	addDescription("Tests the different scenarios, with the pillar argument.");
    	addStep("Testing against the first pillar id", "Should not fail");
    	String[] args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-u" + DEFAULT_UPLOAD_FILE_ADDRESS, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-c" + DEFAULT_COLLECTION_ID,
    			"-p" + PILLAR1_ID,
    			"-i" + DEFAULT_FILE_ID};
		new PutFile(args);
    	
    	addStep("Testing against a non-existing pillar id", "Should fail");
    	args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-u" + DEFAULT_UPLOAD_FILE_ADDRESS, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-c" + DEFAULT_COLLECTION_ID,
    			"-p" + "Random" + (new Date()).getTime() + "pillar",
    			"-i" + DEFAULT_FILE_ID};
    	try {
    		new PutFile(args);
            fail("Should fail.");
    	} catch (IllegalArgumentException e) {
    		// expected.
    	}
    }
    
    @Test(groups = { "regressiontest" })
    public void missingFileScenarioTest() throws Exception {
    	addDescription("Tests the scenario, where no arguments for file or url is given.");
    	String[] args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-c" + DEFAULT_COLLECTION_ID, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-i" + DEFAULT_FILE_ID};
    	try {
    		new PutFile(args);
            fail("Should fail.");
    	} catch (IllegalArgumentException e) {
    		// expected.
    	}
    }

    @Test(groups = { "regressiontest" })
    public void checksumSpecArgumentTest() throws Exception {
    	addDescription("Tests the different checksum scenarios.");
    	addStep("Test non existing checksum algorithm", "Should fail");
    	String[] args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-u" + DEFAULT_UPLOAD_FILE_ADDRESS, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-c" + DEFAULT_COLLECTION_ID, 
    			"-i" + DEFAULT_FILE_ID,
    			"-R" + "NonExistingChecksumType"};
    	try {
    		new PutFile(args);
            fail("Should fail.");
    	} catch (IllegalArgumentException e) {
    		// expected.
    	}
    	
    	addStep("Test MD5 checksum without salt", "Should not fail");
    	args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-u" + DEFAULT_UPLOAD_FILE_ADDRESS, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-c" + DEFAULT_COLLECTION_ID, 
    			"-i" + DEFAULT_FILE_ID,
    			"-R" + "MD5"};
		new PutFile(args);

    	addStep("Test SHA1 checksum with salt", "Should fail");
    	args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-u" + DEFAULT_UPLOAD_FILE_ADDRESS, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-c" + DEFAULT_COLLECTION_ID, 
    			"-i" + DEFAULT_FILE_ID,
    			"-R" + "SHA1",
    			"-S" + "SALT"};
    	try {
    		new PutFile(args);
            fail("Should fail.");
    	} catch (IllegalArgumentException e) {
    		// expected.
    	}
    	
    	addStep("Test HMAC_SHA256 checksum with salt", "Should not fail");
    	args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-u" + DEFAULT_UPLOAD_FILE_ADDRESS, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-c" + DEFAULT_COLLECTION_ID, 
    			"-i" + DEFAULT_FILE_ID,
    			"-R" + "HMAC_SHA256",
    			"-S" + "SALT"};
    	new PutFile(args);

    	addStep("Test HMAC_SHA512 checksum with salt", "Should fail");
    	args = new String[]{"-s" + SETTINGS_DIR, 
    			"-k" + KEY_FILE,
    			"-u" + DEFAULT_UPLOAD_FILE_ADDRESS, 
    			"-C" + DEFAULT_CHECKSUM,
    			"-c" + DEFAULT_COLLECTION_ID, 
    			"-i" + DEFAULT_FILE_ID,
    			"-R" + "HMAC_SHA512"};
    	try {
    		new PutFile(args);
            fail("Should fail.");
    	} catch (IllegalArgumentException e) {
    		// expected.
    	}
    }
}
