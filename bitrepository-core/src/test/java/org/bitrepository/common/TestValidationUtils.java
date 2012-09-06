package org.bitrepository.common;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.testng.Assert;

public class TestValidationUtils {
    /**
     * Validates that only one constructor is declared and it is private and inaccessible.
     * And also validates that all the methods are static.
     * 
     * @param utilityClass The utility class to validate.
     */
    @SuppressWarnings("rawtypes")
    public static void validateUtilityClass(Class utilityClass) {
        Constructor[] constructors = utilityClass.getDeclaredConstructors();
        Assert.assertEquals(constructors.length, 1);
        Assert.assertFalse(constructors[0].isAccessible(), "The constructor should not be accessible.");
        Assert.assertFalse((constructors[0].getModifiers() & Modifier.PRIVATE) == 0, 
                "The constructor should be private: " + constructors[0]);
        
        for(Method m : utilityClass.getDeclaredMethods()) {
            int modifier = m.getModifiers();
            Assert.assertTrue((modifier & Modifier.STATIC) != 0, "The method should be static: " + m);
        }
    }
}
