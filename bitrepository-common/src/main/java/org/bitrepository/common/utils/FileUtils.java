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
            throw new ConfigurationException("");
        }
        
        // instantiate the directory
        File directory = new File(dirPath);
        
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

        return directory;
    }
}
