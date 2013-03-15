/*
 * #%L
 * Bitrepository Protocol
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Interface for the file stores and the reference archive.
 */
public interface FileStore {
    /** 
     * Retrieves the wanted file as a FileInputStream.
     * @param fileID The id of the wanted file.
     * @return A FileInputStream to the requested file.
     * @throws Exception If a problem occurs during the retrieval of the file.
     */
    public FileInputStream getFileAsInputstream(String fileID) throws IOException;
    
    /**
     * Retrieves the wanted file.
     * @param fileID The id of the wanted file.
     * @return The requested file.
     */
    public File getFile(String fileID);
    
    /**
     * Method to check whether a file already exists.
     * @param fileID The id of the file.
     * @return Whether it already exists within the archive.
     */
    public boolean hasFile(String fileID);

    /**
     * Retrieves id of all the files within the storage.
     * @return The collection of file ids in the storage.
     * @throws Exception If anything unexpected occurs.
     */
    public Collection<String> getAllFileIds();

    /**
     * Stores a file given through an InputStream. The file is only intended to be stored in a temporary zone until it 
     * has been validated. Then it should be archived through the 'moveToArchive' method.
     * @param fileID The id of the file to store.
     * @param inputStream The InputStream with the content of the file.
     * @return The downloaded file, which should be validated before it is moved to the archive.
     * @throws IOException If anything unexpected occurs (e.g. file already exists, not enough space, etc.)
     * @see #moveToArchive(String)
     */
    public File downloadFileForValidation(String fileID, InputStream inputStream) throws IOException;
    
    /**
     * Moves a file from the temporary file zone to the archive.
     * @param fileID The id of the file to move to archive.
     * @throws Exception If anything unexpected occurs (e.g. file already exists, not enough space, etc.)
     * @see #downloadFileForValidation(String, InputStream)
     */
    public void moveToArchive(String fileID);

    /**
     * Removes a file from the storage area.
     * @param fileID The id of the file to remove.
     * @throws Exception If anything unexpected occurs (e.g. no such file).
     */
    public void deleteFile(String fileID);
}
