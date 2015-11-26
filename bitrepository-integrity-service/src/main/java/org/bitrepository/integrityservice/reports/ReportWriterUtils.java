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
