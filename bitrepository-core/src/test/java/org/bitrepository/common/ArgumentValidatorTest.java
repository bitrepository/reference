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
package org.bitrepository.common;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ArgumentValidatorTest extends ExtendedTestCase {
    @Test(groups = { "regressiontest" })
    public void utilityTester() throws Exception {
        addDescription("Test that the utility class is a proper utility class.");
        TestValidationUtils.validateUtilityClass(ArgumentValidator.class);
    }
    
    @Test(groups = { "regressiontest" })
    public void testArgumentValidatorObject() throws Exception {
        addDescription("Test the argument validator for arguments not null");
        addStep("Test not null", "Should only throw an exception when a null is given.");
        ArgumentValidator.checkNotNull(new Object(), "No exception expected.");
        try {
            ArgumentValidator.checkNotNull(null, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
        
    @Test(groups = { "regressiontest" })
    public void testArgumentValidatorString() throws Exception {
        addDescription("Test the argument validator for arguments for strings");
        addStep("Test empty string", "Should only throw an exception when the string is null or empty");
        ArgumentValidator.checkNotNullOrEmpty("NO EXCEPTION", "No exception expected.");
        try {
            ArgumentValidator.checkNotNullOrEmpty((String) null, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            ArgumentValidator.checkNotNullOrEmpty("", "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(groups = { "regressiontest" })
    public void testArgumentValidatorInteger() throws Exception {
        addDescription("Test the argument validator for arguments for integers");
        addStep("Test not negative", "Should only throw an exception if the integer is negative");
        ArgumentValidator.checkNotNegative(1, "No exception expected.");
        try {
            ArgumentValidator.checkNotNegative(-1, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        addStep("Test positive", "Should only throw an exception if the integer is not positive");
        ArgumentValidator.checkPositive(1, "No exception expected.");
        try {
            ArgumentValidator.checkPositive(-1, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            ArgumentValidator.checkPositive(0, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }

    @Test(groups = { "regressiontest" })
    public void testArgumentValidatorLong() throws Exception {
        addDescription("Test the argument validator for arguments for longs");
        addStep("Test not negative", "Should only throw an exception if the long is negative");
        ArgumentValidator.checkNotNegative(1L, "No exception expected.");
        try {
            ArgumentValidator.checkNotNegative(-1L, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        addStep("Test positive", "Should only throw an exception if the long is not positive");
        ArgumentValidator.checkPositive(1L, "No exception expected.");
        try {
            ArgumentValidator.checkPositive(-1L, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            ArgumentValidator.checkPositive(0L, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
    
    @Test(groups = { "regressiontest" })
    public void testArgumentValidatorCollection() throws Exception {
        addDescription("Test the argument validator for arguments for collections");
        addStep("Check against null or empty collection", "Should throw exception exception when non-empty collection");
        try {
            ArgumentValidator.checkNotNullOrEmpty((Collection<String>) null, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            ArgumentValidator.checkNotNullOrEmpty(new HashSet<>(), "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        ArgumentValidator.checkNotNullOrEmpty(Arrays.asList("NO FAILURE"), "No exception expected.");
    }
    
    @Test(groups = { "regressiontest" })
    public void testArgumentValidatorArrays() throws Exception {
        addDescription("Test the argument validator for arguments for arrays");
        addStep("Check against null or empty arrays", "Should throw exception exception when non-empty array");
        try {
            ArgumentValidator.checkNotNullOrEmpty((Object[]) null, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        try {
            ArgumentValidator.checkNotNullOrEmpty(new Object[0], "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
        ArgumentValidator.checkNotNullOrEmpty(new Object[]{"NO FAILURE"}, "No exception expected.");
    }

    @Test(groups = { "regressiontest" })
    public void testArgumentValidatorBoolean() throws Exception {
        addDescription("Test the argument validator for arguments for booleans");
        addStep("validate checkTrue", "Should fail when false.");
        ArgumentValidator.checkTrue(true, "No exception expected");
        try {
            ArgumentValidator.checkTrue(false, "Exception expected.");
            Assert.fail("Should throw an exception here.");
        } catch (IllegalArgumentException e) {
            // expected
        }
    }
}
