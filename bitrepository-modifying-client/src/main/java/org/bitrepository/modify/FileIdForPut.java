/*
 * #%L
 * Bitmagasin integrationstest
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
package org.bitrepository.modify;

import java.io.File;
import java.net.URL;

import org.bitrepository.protocol.http.HTTPFileExchange;

/**
 * Class for keeping track of the data involved in putting a specific file.
 */
public class FileIdForPut {
    /** The id of the file.*/
    private String fileId;
    /** The id of the SLA, which the file belongs to.*/
    private String slaId;
    /** The actual file.*/
    private File file;
    /** The URL where the file is uploaded. The file is being uploaded, when 
     * this field is requested the first time (and first time only).*/
    private URL url;
    
    /**
     * Constructor.
     * @param fileId The id of the file.
     * @param file The actual file.
     * @param slaId The id of the SLA, which the file belongs to.
     */
    public FileIdForPut(String fileId, File file, String slaId) {
        this.fileId = fileId;
        this.file = file;
        this.slaId = slaId;
    }
    
    /**
     * Retrieval of the id of the SLA.
     * @return The id of the SLA.
     */
    public String getSlaId() {
        return slaId;
    }
    
    /**
     * Retrieval of the id of the file.
     * @return The id of the file.
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * Retrieval of the actual file.
     * @return The actual file.
     */
    public File getFile() {
        return file;
    }
    
    /**
     * Retrieval of the URL for the file. If no URL is known yet, then the
     * file has not yet been uploaded; it is therefore uploaded, and the URL 
     * for the location where it has been uploaded is saved and returned.
     * 
     * @param fileId The id for the file.
     * @return The URL for the location where the file has been uploaded.
     */
    public URL getUrl() {
        // check whether it already has been uploaded to a server.
        if(url == null) {
            try {
                // upload the file and store the URL.
                url = HTTPFileExchange.uploadToServer(file);
            } catch (Exception e) {
                throw new ModifyException("Could not upload the file '"
                        + fileId + "'.", e);
            }
        }
        return url;
    }
}
