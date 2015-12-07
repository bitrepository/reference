/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Utility class for helper methods for writing report files 
 */
public class ReportWriterUtils {
    
    /**
     * Creates a File object, and makes sure that it's empty. I.e. deletes the old file if present on disk.
     * <p>
     * Please remember that there is nothing stopping some other thread from deleting your file
     * @param dir the dir in which to create the file
     * @param fileName the filename
     * @return a brand new File object, ready for use
     * @throws IOException if the file already existed and the delete failed
     */
    public static File makeEmptyFile(File dir, String fileName) throws IOException {
        Path file = dir.toPath().resolve(fileName);
        Files.deleteIfExists(file);
        return file.toFile();
    }
    
    /**
     * Helper method to add an entry to a partial report file. 
     * Writes the line, adds a new line, and flushes to disk.
     * @param writer the writer to write to
     * @param line the line to write
     * @throws IOException if the writer cannot write
     */
    public static void addLine(BufferedWriter writer, String line) throws IOException {
        writer.append(line);
        writer.newLine();
        writer.flush(); 
    }
}
