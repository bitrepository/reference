/*
 * #%L
 * Bitrepository Common
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
package org.bitrepository.common;

import java.util.Collection;

/**
 * Indicates that one or more arguments are invalid.
 */
@SuppressWarnings("serial")
public final class ArgumentValidator {
    
    /** Utility class, should never be instantiated */
    private ArgumentValidator() {}
    
    /**
     * Check if a String argument is null or the empty string.
     *
     * @param val  the value to check
     * @param name the name and type of the value being checked
     * @throws IllegalArgumentException if validation fails
     */
    public static void checkNotNullOrEmpty(String val, String name) {
        checkNotNull(val, name);

        if (val.isEmpty()) {
            throw new IllegalArgumentException("The value of the argument '" + name + "' must not be an empty string.");
        }
    }

    /**
     * Check if an Object argument is null.
     *
     * @param val  the value to check
     * @param name the name and type of the value being checked.
     * @throws IllegalArgumentException if validation fails
     */
    public static void checkNotNull(Object val, String name) {
        if (val == null) {
            throw new IllegalArgumentException("The value of the argument '" + name + "' must not be null.");
        }
    }

    /**
     * Check if an int argument is less than 0.
     *
     * @param num  argument to check
     * @param name the name and type of the value being checked.
     * @throws IllegalArgumentException if validation fails
     */
    public static void checkNotNegative(int num, String name) {
        if (num < 0) {
            throw new IllegalArgumentException("The value of the argument '" + name
                    + "' must be non-negative, but is " + num + ".");
        }
    }

    /**
     * Check if a long argument is less than 0.
     *
     * @param num argument to check
     * @param name the name and type of the value being checked.
     * @throws IllegalArgumentException if validation fails
     */
    public static void checkNotNegative(long num, String name) {
        if (num < 0) {
            throw new IllegalArgumentException("The value of the argument '" + name
                    + "' must be non-negative, but is " + num + ".");
        }
    }

    /**
     * Check if an int argument is less than or equal to 0.
     *
     * @param num  argument to check
     * @param name the name and type of the value being checked.
     * @throws IllegalArgumentException if validation fails
     */
    public static void checkPositive(int num, String name) {
        if (num <= 0) {
            throw new IllegalArgumentException("The value of the argument '" + name
                    + "' must be positive, but is " + num + ".");
        }
    }

    /**
     * Check if a long argument is less than 0.
     *
     * @param num argument to check
     * @param name the name and type of the value being checked.
     * @throws IllegalArgumentException if validation fails
     */
    public static void checkPositive(long num, String name) {
        if (num <= 0) {
            throw new IllegalArgumentException("The value of the argument '" + name
                    + "' must be positive, but is " + num + ".");
        }
    }

    /**
     * Check if a List argument is not null and the list is not empty.
     *
     * @param c argument to check
     * @param name the name and type of the value being checked.
     * @throws IllegalArgumentException if validation fails
     */
    public static void checkNotNullOrEmpty(Collection<?> c, String name) {
        checkNotNull(c, name);

        if (c.isEmpty()) {
            throw new IllegalArgumentException("The contents of the argument '" + name + "' must not be empty.");
        }
    }
    
    /**
     * Check if a array argument is not null and the list is not empty.
     *
     * @param c argument to check
     * @param name the name and type of the value being checked.
     * @throws IllegalArgumentException if validation fails
     */
    public static void checkNotNullOrEmpty(Object[] array, String name) {
        checkNotNull(array, name);

        if (array.length == 0) {
            throw new IllegalArgumentException("The contents of the argument '" + name + "' must not be empty.");
        }
    }

    /**
     * Check that some condition on input parameters is true and throw an
     * ArgumentNotValid if it is false.
     * @param b the condition to check
     * @param s the error message to be reported
     * @throws IllegalArgumentException if validation fails
     */
    public static void checkTrue(boolean b, String s) {
        if (!b) {
            throw new IllegalArgumentException(s);
        }
    }
}
