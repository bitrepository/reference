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
package org.bitrepository.protocol.fileexchange;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.FileUtils;
import org.bitrepository.protocol.TestMessageFactory;

public class TestFileStore {    
    private static final String ROOT_STORE = "target/test-classes/filestorage";
    private final String storageDir;
    /** The file store already contains this file when it created. The fileID corresponds to the file ID found in the 
     * {@LINK TestMessageFactory}
     */
    private static final String DEFAULT_EXISTING_FILE = TestMessageFactory.FILE_ID_DEFAULT;
    private final String storeName;

    public TestFileStore(String storeName) {
        this.storeName = storeName;
        storageDir = ROOT_STORE + "/" + storeName;
        File defaultFile = new File("src/test/resources/test-files/", DEFAULT_EXISTING_FILE);
        try {
            FileUtils.copyFileToDirectory(defaultFile, new File(storageDir));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns an inputstream to the default file.
     * @param fileID
     * @param outputStream
     * @return
     */
    public InputStream readDefaultFile() {
            return getInputstream(DEFAULT_EXISTING_FILE);
    }
    
    /**
     * Returns a input file to the indicated file
     * @param fileID
     * @param outputStream
     * @return
     */
    public InputStream getInputstream(String fileID) {
        try {
            return new FileInputStream(new File(storageDir, fileID));
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }

    /**
     * Stored the indicated file from the supplied input stream
     * @param fileID
     * @param inputStream
     */
    public void storeFile(String fileID, InputStream inputStream)  throws IOException  {
        File theFile = new File(fileID);

        // Check if a file exists.
        if (theFile.exists()) {
            throw new RuntimeException("Unable to store the indicated file, it already exists");
        }

        // Create directory for the file, if requested.
        if (theFile.getParentFile() != null) {
          theFile.getParentFile().mkdirs();
        }

        // Save InputStream to the file.
        BufferedOutputStream bufferedOutputstream = null;
        try {
            bufferedOutputstream = new BufferedOutputStream(new FileOutputStream(theFile));
          byte[] buffer = new byte[32 * 1024];
          int bytesRead = 0;
          while ((bytesRead = inputStream.read(buffer)) != -1) {
              bufferedOutputstream.write(buffer, 0, bytesRead);
          }
        }
        finally {
            bufferedOutputstream.close();
        }
    }
    
    /**
     * Replaces the indicated file
     * @param fileID
     * @param inputStream
     */
    public void replaceFile(String fileID, InputStream inputStream)  throws IOException  {
        deleteFile(fileID);
        storeFile(fileID, inputStream);
    }
    
    /**
     * Deletes the indicated file
     * @param fileID
     * @param inputStream
     */
    public void deleteFile(String fileID)  throws IOException  {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String toString() {
        return "TestFileStore [storageDir=" + storageDir + ", storeName=" + storeName
                + "]";
    }

    public File getFile(String fileID) {
        return new File(storageDir, fileID);
    }

}
