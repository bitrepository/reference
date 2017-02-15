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
package org.bitrepository.common.filestore;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

/**
 * Interface for the file stores and the reference archive.
 */
public interface FileStore extends AutoCloseable {
    /**
     * Retrieves the wanted file.
     * @param fileID The id of the wanted file.
     * @param collectionID the collection id
     * @return The requested file.
     */
    FileInfo getFileInfo(String fileID, String collectionID);
    
    /**
     * Method to check whether a file already exists.
     * @param fileID The id of the file.
     * @param collectionID the collection id
     * @return Whether it already exists within the archive.
     */
    boolean hasFile(String fileID, String collectionID);

    /**
     * Retrieves id of all the files within the storage.
     * @param collectionID the collection id
     * @return The collection of file ids in the storage.
     * @throws RuntimeException If anything unexpected occurs.
     */
    Collection<String> getAllFileIds(String collectionID);

    /**
     * Stores a file given through an InputStream. The file is only intended to be stored in a temporary zone until it 
     * has been validated. Then it should be archived through the 'moveToArchive' method.
     * @param fileID The id of the file to store.
     * @param collectionID the collection id
     * @param inputStream The InputStream with the content of the file.
     * @return The downloaded file, which should be validated before it is moved to the archive.
     * @throws IOException If anything unexpected occurs (e.g. file already exists, not enough space, etc.)
     * @see #moveToArchive(String,String)
     */
    FileInfo downloadFileForValidation(String fileID, String collectionID, InputStream inputStream) throws IOException;
    
    /**
     * Moves a file from the temporary file zone to the archive.
     * @param fileID The id of the file to move to archive.
     * @param collectionID the collection id
     * @throws RuntimeException If anything unexpected occurs (e.g. file already exists, not enough space, etc.)
     * @see #downloadFileForValidation(String, String, InputStream)
     */
    void moveToArchive(String fileID, String collectionID);

    /**
     * Removes a file from the storage area.
     * @param fileID The id of the file to remove.
     * @param collectionID the collection id
     * @throws RuntimeException If anything unexpected occurs (e.g. no such file).
     */
    void deleteFile(String fileID, String collectionID);
    
    /**
     * The replace operation atomically.
     * Removes the archived file from its directory, and moves the tmpFile into the archive dir.
     * 
     * @param fileID The id of the file to perform the replace function upon.
     * @param collectionID the collection id
     */
    void replaceFile(String fileID, String collectionID);
    
    /**
     * For retrieval of the size left in this archive.
     * @param collectionID the collection id
     * @return The number of bytes left in the archive.
     */
    long sizeLeftInArchive(String collectionID);
    
    /**
     * Retrieves the file within a tmpDir.
     * @param fileID The id of the file to locate within the tmpDir.
     * @param collectionID the collection id
     * @return The file in the tmpDir.
     */
    FileInfo getFileInTmpDir(String fileID, String collectionID);
    
    /**
     * Ensures that no such file exists within the tmp directory.
     * 
     * @param fileID The id of the file to clean up after.
     * @param collectionID the collection for the fileID
     */
    void ensureFileNotInTmpDir(String fileID, String collectionID);
    
    /**
     * Closes all the archives.
     */
    @Override
    void close();
}
