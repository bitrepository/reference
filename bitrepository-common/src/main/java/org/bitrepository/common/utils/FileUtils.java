/*
 * #%L
 * bitrepository-access-client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.common.ConfigurationException;

public final class FileUtils {
    
    /**
     * Private constructor. To prevent instantiation of this utility class.
     */
    private FileUtils() {}
    
    /**
     * Function to retrieve a directory on a given path. If the directory does not already exists, then it is 
     * created.
     * 
     * @param dirPath The path to the directory.
     * @return The directory at the end of the path.
     */
    public static File retrieveDirectory(String dirPath) {
        // validate the argument
        if(dirPath == null || dirPath.isEmpty()) {
            throw new ConfigurationException("Bad path for the directory: " + dirPath);
        }
        
        // instantiate the directory
        File directory = new File(dirPath);
        instantiateAsDirectory(directory);
        
        return directory;
    }
    
    /**
     * Method for instantiating a subdirectory with a given name to a given directory.
     * 
     * @param parentDir The directory to be parent to the new directory.
     * @param dirName The name of the directory to be instantiated.
     * @return The instantiated subdirectory.
     */
    public static File retrieveSubDirectory(File parentDir, String dirName) {
        // validate the argument
        if(dirName == null || dirName.isEmpty()) {
            throw new ConfigurationException("Invalid name for the directory: " + dirName);
        }
        // validate the argument
        if(parentDir == null || !parentDir.isDirectory()) {
            throw new ConfigurationException("The parent directory, " + parentDir + ", is invalid");
        }
        
        // instantiate the directory
        File directory = new File(parentDir, dirName);
        instantiateAsDirectory(directory);
        
        return directory;
    }
    
    /**
     * Instantiates the given file as a directory. 
     * Throws exception if invalid or it somehow cannot be instantiated.
     * 
     * @param directory The file to instantiate as a directory.
     */
    private static void instantiateAsDirectory(File directory) {
        // validate that it is not a file.
        if(directory.isFile()) {
            throw new ConfigurationException("The file directory '" + directory.getAbsolutePath() 
                    + "' already exists as a file, and not as a directory, which is required.");
        }
        
        // Create the directory if it does not exist, and validate that it is a directory afterwards.
        if(!directory.exists() || !directory.isDirectory()) {
            directory.mkdirs();
            if(!directory.isDirectory()) {
                throw new ConfigurationException("The file directory '" + directory.getAbsolutePath() + "' cannot be "
                        + "instantiated as a directory.");
            }
        }
    }
    
    /**
     * Method for recursingly removing a file or a directory (with everything within the directory).
     * TODO verify, that it has successfully been removed.
     * @param f The file to remove.
     */
    public static void delete(File f) {
        if(!f.exists()) {
            throw new IllegalArgumentException("The file '" + f + "' does not exist.");
        }
        if(f.isDirectory()) {
            for(File sub : f.listFiles()) {
                delete(sub);
            }
        }
        f.delete();
    }
    
    /**
     * Method for deprecating a file by moving it to '*.old'. If a deprecated file already exists, then it is performed
     * recursively, thus creating '*.old.old', '*.old.old.old*... 
     * @param f The file to deprecate.
     */
    public static void deprecateFile(File f) {
        if(!f.exists()) {
            throw new IllegalArgumentException("The file '" + f + "' does not exist.");
        }
        File deprecatedLocation = new File(f.getAbsolutePath() + ".old");
        if(deprecatedLocation.exists()) {
            deprecateFile(deprecatedLocation);
        }
        
        f.renameTo(deprecatedLocation);
    }
}
