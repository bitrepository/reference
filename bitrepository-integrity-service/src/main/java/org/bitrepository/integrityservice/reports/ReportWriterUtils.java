package org.bitrepository.integrityservice.reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;


/**
 * Utility class for helper methods for writing report files 
 */
public class ReportWriterUtils {
    
    /**
     * Creates a File object, and makes sure that it's empty. I.e. deletes the old file if present on disk. 
     */
    public static File makeEmptyFile(File dir, String fileName) {
        File file = new File(dir, fileName);
        if(file.exists()) {
            file.delete();
        }
        return file;
    }
    
    /**
     * Helper method to add an entry to a partial report file. 
     * Writes the line, adds a new line, and flushes to disk. 
     */
    public static void addLine(BufferedWriter writer, String line) throws IOException {
        writer.append(line);
        writer.newLine();
        writer.flush(); 
    }
}
