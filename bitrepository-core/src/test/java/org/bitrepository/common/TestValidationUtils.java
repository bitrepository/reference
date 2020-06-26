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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.testng.Assert;

public class TestValidationUtils {
    /**
     * Validates that only one constructor is declared and it is private and inaccessible.
     * This constructor is then used for instantiating the given class.
     * And also validates that all the methods are static.
     * 
     * @param utilityClass The utility class to validate.
     */
    @SuppressWarnings("rawtypes")
    public static void validateUtilityClass(Class utilityClass) {
        Constructor[] constructors = utilityClass.getDeclaredConstructors();
        Assert.assertEquals(constructors.length, 1);
        Assert.assertFalse(constructors[0].isAccessible(), "The constructor should not be accessible.");
        Assert.assertNotEquals((constructors[0].getModifiers() & Modifier.PRIVATE), 0,
                "The constructor should be private: " + constructors[0]);
        
        // instantiate the class
        constructors[0].setAccessible(true);
        try {
            constructors[0].newInstance();
        } catch (Exception e) {
            Assert.fail("Could not instantiate the constructor.", e);
        }

        // validate the methods
        for(Method m : utilityClass.getDeclaredMethods()) {
            int modifier = m.getModifiers();
            Assert.assertTrue((modifier & Modifier.STATIC) != 0, "The method should be static: " + m);
        }
    }
}
