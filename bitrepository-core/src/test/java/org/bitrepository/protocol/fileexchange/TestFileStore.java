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

import org.apache.commons.io.FileUtils;
import org.bitrepository.common.filestore.DefaultFileInfo;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** 
 * Models the storing and retrieval of files as needed in test modeling pillar behavior.
 * Even though it should support different collections, this implementation does not!
 * All collection ids are ignored.
 */
public class TestFileStore implements FileStore {    
    private static final String ROOT_STORE = "target/test-classes/filestorage";
    private final String storageDir;
    /** The file store already contains this file when it created. The fileID corresponds to the file ID found in the 
     * {@LINK ClientTestMessageFactory}
     */
    public static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;
    private final String storeName;
    
    /** The default test file.*/
    public static final File DEFAULT_TEST_FILE = new File("src/test/resources/test-files/", DEFAULT_FILE_ID);

    /**
     * Constructor, where the FileStore is initialized with some files.
     * @param storeName The id for the FileStore.
     * @param initialFiles The files to copy into the FileStore during initialization.
     */
    public TestFileStore(String storeName, File ... initialFiles) {
        this.storeName = storeName;
        storageDir = ROOT_STORE + "/" + storeName;

        for(File initFile : initialFiles) {
            try {
                FileUtils.copyFileToDirectory(initFile, new File(storageDir));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public FileInfo downloadFileForValidation(String fileID, String collectionID, InputStream inputStream) {
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
        try {
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
        } catch (IOException e) {
            throw new RuntimeException("Could not download file for validation.", e);
        }
        
        return new DefaultFileInfo(theFile);
    }
    
    @Override
    public void moveToArchive(String fileID, String collectionID) {
        // This does nothing.
        return;
    }

    @Override
    public void deleteFile(String fileID, String collectionID) {
        (new File(storageDir, fileID)).delete();
    }

    @Override
    public FileInfo getFileInfo(String fileID, String collectionID) {
        return new DefaultFileInfo(new File(storageDir, fileID));
    }

    @Override
    public Collection<String> getAllFileIds(String collectionID) {
        String[] ids = new File(storageDir).list();
        List<String> res = new ArrayList<>();
        for(String id : ids) {
            res.add(id);
        }
        return res;
    }

    @Override
    public boolean hasFile(String fileID, String collectionID) {
        return (new File(storageDir, fileID)).isFile();
    }

    @Override
    public String toString() {
        return "TestFileStore [storageDir=" + storageDir + ", storeName=" + storeName + "]";
    }

    public void storeFile(String fileID, InputStream in) throws IOException {
        downloadFileForValidation(fileID, null, in);
        moveToArchive(fileID, null);
    }

    @Override
    public void replaceFile(String fileID, String collectionID) {
        // TODO Auto-generated method stub
    }

    @Override
    public long sizeLeftInArchive(String collectionID) {
        return new File(storageDir).getFreeSpace();
    }

    @Override
    public FileInfo getFileInTmpDir(String fileID, String collectionID) {
        return getFileInfo(fileID, collectionID);
    }

    @Override
    public void ensureFileNotInTmpDir(String fileID, String collectionID) { }

    @Override
    public void close() { }
}
