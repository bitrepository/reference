package org.bitrepository.common.filestore;

import java.io.IOException;
import java.io.InputStream;

/**
 * Interface for the information attached to a given file id.
 */
public interface FileInfo {
    /**
     * @return The ID of the file.
     */
    String getFileID();

    /**
     * @return The inputstream for the data.
     * @exception IOException If any issues regarding retrieving the inputstream occurs.
     */
    InputStream getInputstream() throws IOException;
    
    /**
     * @return The last modified timestamp.
     */
    Long getMdate();
    
    /**
     * @return The size of the file.
     */
    long getSize();
}
