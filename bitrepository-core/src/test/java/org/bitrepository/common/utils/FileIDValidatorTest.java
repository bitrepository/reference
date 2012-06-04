/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.common.utils;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.FileIDValidator;
import org.bitrepository.service.exception.InvalidMessageException;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class FileIDValidatorTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
    }
    
    @Test( groups = {"regressiontest"})
    public void validatorTest() throws Exception {
        addDescription("Tests the FileIDValidator class for the input handling based on a given regex.");
        addStep("Setup the validator", "Should be ok.");
        settings.getCollectionSettings().getProtocolSettings().setAllowedFileIDPattern("[a-zA-z0-9\\-_.]{5,250}");
        FileIDValidator validator = new FileIDValidator(settings);
        String validFileID = "abcdefghijklmnopqrstuvwxyz";
        String invalidCharacters = "¾§?+±|´~$½¥½{¥[]{[¡@£";
        String tooLong = validFileID + validFileID + validFileID + validFileID + validFileID + validFileID 
                + validFileID + validFileID + validFileID + validFileID + validFileID + validFileID;
        String tooShort = "";
        
        addStep("Test a null as argument", "The null should be ignored.");
        validator.validateFileID(null);
        
        addStep("Test a valid fileID", "Should be valid");
        validator.validateFileID(validFileID);
        
        addStep("Test invalid characters", "Should be invalid");
        try {
            validator.validateFileID(invalidCharacters);
            Assert.fail("Should fail with bad characters here!");
        } catch (InvalidMessageException e) {
            // expected
        }

        addStep("Test invalid length", "Should be invalid");
        try {
            validator.validateFileID(tooLong);
            Assert.fail("Should fail with invalid length here!");
        } catch (InvalidMessageException e) {
            // expected
        }
        
        addStep("Test too short", "Should be invalid");
        try {
            validator.validateFileID(tooShort);
            Assert.fail("Should fail with invalid length here!");
        } catch (InvalidMessageException e) {
            // expected
        }
    }
    
    @Test( groups = {"regressiontest"})
    public void validatorDefaultTest() throws Exception {
        addDescription("Tests the FileIDValidator class default restrictions. Only the length should fail.");
        addStep("Setup the validator, where all file ids are allowed at default.", "Should be ok.");
        settings.getCollectionSettings().getProtocolSettings().setAllowedFileIDPattern(".+");
        FileIDValidator validator = new FileIDValidator(settings);
        String validFileID = "abcdefghijklmnopqrstuvwxyz";
        String invalidCharacters = "¾§?+±|´~$½¥½{¥[]{[¡@£";
        String tooLong = validFileID + validFileID + validFileID + validFileID + validFileID + validFileID 
                + validFileID + validFileID + validFileID + validFileID + validFileID + validFileID;
        String tooShort = "";
        
        addStep("Test a null as argument", "The null should be ignored.");
        validator.validateFileID(null);
        
        addStep("Test a valid fileID", "Should be valid");
        validator.validateFileID(validFileID);
        
        addStep("Test odd characters", "Should be valid");
        validator.validateFileID(invalidCharacters);

        addStep("Test invalid length", "Should be invalid");
        try {
            validator.validateFileID(tooLong);
            Assert.fail("Should fail with invalid length here -> too long");
        } catch (InvalidMessageException e) {
            // expected
        }
        
        addStep("Test too short", "Should be invalid");
        try {
            validator.validateFileID(tooShort);
            Assert.fail("Should fail with invalid length here -> too short");
        } catch (InvalidMessageException e) {
            // expected
        }
    }
    
    @Test( groups = {"regressiontest"})
    public void badRegexTest() throws Exception {
        addDescription("Tests the FileIDValidator handling of bad file id pattern.");
        addStep("Give the validator a 'null' as allowed file id pattern", "Should be a null stored as regex." );
        settings.getCollectionSettings().getProtocolSettings().setAllowedFileIDPattern(null);
        TestFileIDValidator validator = new TestFileIDValidator(settings);
        Assert.assertNull(validator.getRegex());
        
        addStep("Give the validator an empty string as allowed file id pattern", "Should be a null stored as regex." );
        settings.getCollectionSettings().getProtocolSettings().setAllowedFileIDPattern("");
        validator = new TestFileIDValidator(settings);
        Assert.assertNull(validator.getRegex());
    }
    
    private class TestFileIDValidator extends FileIDValidator {
        
        public TestFileIDValidator(Settings settings) {
            super(settings);
        }
        
        public String getRegex() {
            return regex;
        }
    }
}
