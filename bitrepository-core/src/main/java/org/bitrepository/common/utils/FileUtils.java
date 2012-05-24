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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for method involving the handling of files.
 */
public final class FileUtils {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(FileUtils.class);
    /** The maximal size of the byte array for digest.*/
    private static final int BYTE_ARRAY_SIZE = 4096;
    
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
        ArgumentValidator.checkNotNullOrEmpty(dirPath, "String dirPath");
        
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
        ArgumentValidator.checkNotNullOrEmpty(dirName, "String dirName");
        ArgumentValidator.checkNotNull(parentDir, "File parentDir");
        
        // validate the argument
        if(!parentDir.isDirectory()) {
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
            if(!directory.mkdirs()) {
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
        ArgumentValidator.checkNotNull(f, "File f");
        
        if(!f.exists()) {
            throw new IllegalArgumentException("The file '" + f + "' does not exist.");
        }
        if(f.isDirectory()) {
            for(File sub : f.listFiles()) {
                delete(sub);
            }
        }
        if(!f.delete()) {
            log.warn("Could not delete '" + f.getAbsolutePath() + "'");
        }
    }
    
    /**
     * Method for deprecating a file by moving it to '*.old'. If a deprecated file already exists, then it is performed
     * recursively, thus creating '*.old.old', '*.old.old.old*... 
     * @param f The file to deprecate.
     */
    public static void deprecateFile(File f) {
        ArgumentValidator.checkNotNull(f, "File f");
        
        if(!f.exists()) {
            throw new IllegalArgumentException("The file '" + f + "' does not exist.");
        }
        File deprecatedLocation = new File(f.getAbsolutePath() + ".old");
        if(deprecatedLocation.exists()) {
            deprecateFile(deprecatedLocation);
        }
        
        if(!f.renameTo(deprecatedLocation)) {
            log.warn("Could not deprecate the file '" + f.getAbsolutePath() + "'.");
        }
    }
    
    /**
     * Method for moving a file from one position to another.
     * @param from The file to move from.
     * @param to The file to move to.
     */
    public static void moveFile(File from, File to) {
        ArgumentValidator.checkNotNull(from, "File from");
        ArgumentValidator.checkNotNull(to, "File to");
        
        if(!from.isFile()) {
            throw new IllegalArgumentException("No downloaded file to archive '" + from.getName() + "'");
        }
        if(to.exists()) {
            throw new IllegalArgumentException("The file already exists within the archive. Cannot archive again!");
        }
        
        if(!from.renameTo(to)) {
            log.warn("Could move the file '" + from.getAbsolutePath() + "' to the location '" + to.getAbsolutePath() 
                    + "'");
        }
    }
    
    /**
     * Reads a file into a byte array.
     * TODO handle the case, when the file is longer than can be expressed with an Integer.
     * 
     * @param file 
     * @return
     * @throws IOException
     */
    public static String readFile(File file) throws IOException {
        ArgumentValidator.checkNotNull(file, "file");
        
        DataInputStream fis = null;
        StringBuffer res = new StringBuffer();
        
        try {
            fis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            
            byte[] bytes = new byte[BYTE_ARRAY_SIZE];
            while (fis.read(bytes) > 0) {
                res.append(new String(bytes));
            }
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
        
        return res.toString();
    }
    
    /**
     * Reads a file and puts the lines into a list.
     * 
     * @param file The file to read from.
     * @return The list of lines from the file.
     * @throws IOException If some input/output error occurs.
     */
    public static List<String> readLinesFromFile(File file) throws IOException {
        ArgumentValidator.checkNotNull(file, "file");
        
        FileInputStream fis = null;
        BufferedReader reader = null;
        List<String> res = new ArrayList<String>();
        
        try {
            fis = new FileInputStream(file);
            reader = new BufferedReader(new InputStreamReader(fis));
            
            String line;
            while ((line = reader.readLine()) != null) {
                res.add(line);
            }
        } finally {
            if(reader != null) {
                reader.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
        
        return res;
    }
    
    /**
     * Copies a file from one place to another place.
     * @param source The source file to copy from.
     * @param target The target file to copy to.
     */
    public static void copyFile(File source, File target) {
        FileInputStream is = null;
        FileOutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(target);
            
            byte[] bytes = new byte[BYTE_ARRAY_SIZE];
            
            int size;
            while((size = is.read(bytes)) > 0) {
                os.write(bytes, 0, size);
            }
            
            os.flush();
        } catch (IOException e) {
            throw new IllegalStateException("Could not copy the file '" + source + "' to the destination '"
                    + target + "'.", e);
        } finally {
            // Remember to close the streams.
            try {
                if(is != null) {
                    is.close();
                } 
                if(os != null) {
                    os.close();
                }
            } catch (IOException e) {
                // TODO
            }
        }
    }
    
    /** Write the contents of a stream into a file.
     *
     * @param in A stream to read from.  This stream is not closed by this
     * method.
     * @param f The file to write the stream contents into.
     * @throws IOException If any error occurs while writing the stream to a file
     */
    public static void writeStreamToFile(InputStream in, File f) throws IOException {
        ArgumentValidator.checkNotNull(f, "File f");
        ArgumentValidator.checkNotNull(in, "InputStream in");
        
        byte[] buffer = new byte[BYTE_ARRAY_SIZE];
        FileOutputStream out = new FileOutputStream(f);
        try {
            int bytesRead;
            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }
        } finally {
            out.close();
        }
    }
    
    
    /** Unzip a zipFile into a directory.  This will create subdirectories
     * as needed.
     *
     * @param zipFile The file to unzip
     * @param toDir The directory to create the files under.  This directory
     * will be created if necessary.  Files in it will be overwritten if the
     * filenames match.
     * @throws IOException If any error occurs while writing the stream to a file
     */
    public static void unzip(File zipFile, File toDir) throws IOException {
        ArgumentValidator.checkNotNull(zipFile, "File zipFile");
        ArgumentValidator.checkNotNull(toDir, "File toDir");
        ArgumentValidator.checkTrue(
                toDir.getAbsoluteFile().getParentFile().canWrite(),
                "can't write to '" + toDir + "'");
        ArgumentValidator.checkTrue(zipFile.canRead(),
                "can't read '" + zipFile + "'");
        InputStream inputStream = null;
        ZipFile unzipper = null;
        try {
            unzipper = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> entries = unzipper.entries();
            while (entries.hasMoreElements()) {
                ZipEntry ze = entries.nextElement();
                File target = new File(toDir, ze.getName());
                // Ensure that its dir exists
                retrieveDirectory(target.getCanonicalFile().getParent());
                if (ze.isDirectory()) {
                    target.mkdir();
                } else {
                    inputStream = unzipper.getInputStream(ze);
                    FileUtils.writeStreamToFile(inputStream, target);
                    inputStream.close();
                }
            }
        } finally {
            if (unzipper != null) {
                unzipper.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
