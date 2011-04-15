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
package org.bitrepository.common.utils;

import java.io.File;

/**
 * Utility method for validating arguments. Throws {@link java.lang.IllegalArgumentException} whenever a validation of
 * an argument fails.
 */
public final class ArgumentValidationUtils {
    
    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ArgumentValidationUtils() { }

    /**
     * Validates that a given object is not null. 
     * Otherwise an {@link java.lang.IllegalArgumentException} is thrown.
     * @param obj The object to validate.
     */
    public static void checkNotNull(Object obj) {
        if(obj == null) {
            throw new IllegalArgumentException("The argument may not be null.");
         }
    }
    
    /**
     * Validates that a given object is not null. 
     * Otherwise an {@link java.lang.IllegalArgumentException} is thrown.
     * @param obj The object to validate.
     * @param description The description of the object. Will be part of the potential exception.
     */
    public static void checkNotNull(Object obj, String description) {
        if(obj == null) {
            throw new IllegalArgumentException("The argument may not be null: " + description);
        }
    }
    
    /**
     * Validates that a given String is neither null or the empty string.  
     * Otherwise an {@link java.lang.IllegalArgumentException} is thrown.
     * @param obj The String to validate.
     */
    public static void checkNotNullOrEmpty(String str) {
        checkNotNull(str);
        if(str.isEmpty()) {
            throw new IllegalArgumentException("The argument may not be en empty string.");
        }
    }
    
    /**
     * Validates that a given String is neither null or the empty string.  
     * Otherwise an {@link java.lang.IllegalArgumentException} is thrown.
     * @param obj The String to validate.
     * @param description The description of the string. Will be part of the potential exception.
     */
    public static void checkNotNullOrEmpty(String str, String description) {
        checkNotNull(str, description);
        if(str.isEmpty()) {
            throw new IllegalArgumentException("The argument may not be en empty string: " + description);
        }
    }
    
    /**
     * Validates that a file is a proper file (both exists and is not a directory).
     * Otherwise an {@link java.lang.IllegalArgumentException} is thrown.
     * @param file The file to validate.
     */
    public static void checkIsFile(File file) {
        checkNotNull(file);
        if(!file.isFile()) {
            throw new IllegalArgumentException("The file '" + file.getAbsolutePath() + "' is not a proper file. "
                    + "Either it does not exist or it is a directory.");
        }
    }
    
    /**
     * Validates that a file is a proper file (both exists and is not a directory).
     * Otherwise an {@link java.lang.IllegalArgumentException} is thrown.
     * @param file The file to validate.
     * @param description The description of the file. Will be part of the potential exception.
     */
    public static void checkIsFile(File file, String description) {
        checkNotNull(file, description);
        if(!file.isFile()) {
            throw new IllegalArgumentException("The file '" + file.getAbsolutePath() + "' is not a proper file. "
                    + "Either it does not exist or it is a directory: " + description);
        }
    }
    
    /**
     * Validates that a file does not already exist.
     * Otherwise an {@link java.lang.IllegalArgumentException} is thrown.
     * @param file The file to validate.
     */
    public static void checkFileDoesNotExist(File file) {
        checkNotNull(file);
        if(file.isFile()) {
            throw new IllegalArgumentException("The file '" + file.getAbsolutePath() + "' already exists as either a "
                    + "a file or a directory.");
        }
    }
    
    /**
     * Validates that a file does not already exist.
     * Otherwise an {@link java.lang.IllegalArgumentException} is thrown.
     * @param file The file to validate.
     * @param description The description of the file. Will be part of the potential exception.
     */
    public static void checkFileDoesNotExist(File file, String description) {
        checkNotNull(file, description);
        if(file.isFile()) {
            throw new IllegalArgumentException("The file '" + file.getAbsolutePath() + "' already exists as either a "
                    + "a file or a directory: " + description);
        }
    }
}
