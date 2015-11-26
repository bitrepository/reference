/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.store.checksumdatabase;

import java.util.Date;

import org.bitrepository.common.ArgumentValidator;

/**
 * Container for the information about the checksum of a file.
 */
public class ChecksumEntry {
    /** The id of the file.*/
    private final String fileID;
    /** The checksum of the file.*/
    private String checksum;
    /** The calculation date for the checksum of the file.*/
    private Date calculationDate;
    
    /**
     * Constructor.
     * @param fileID The id of the file.
     * @param checksum The checksum of the file.
     * @param calculationDate The calculation date for the checksum of the file.
     */
    public ChecksumEntry(String fileID, String checksum, Date calculationDate) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        this.fileID = fileID;
        this.checksum = checksum;
        this.calculationDate = calculationDate;
    }
    
    /**
     * Set a new value for the checksum.
     * @param checksum The new checksum value.
     */
    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
    
    /**
     * Set a new timestamp for the calculation date of the checksum.
     * @param date The new date for the calculation of the checksum.
     */
    public void setCalculationDate(Date date) {
        this.calculationDate = date;
    }
    
    /**
     * @return The id of the file.
     */
    public String getFileId() {
        return fileID;
    }
    
    /**
     * @return The checksum of the file.
     */
    public String getChecksum() {
        return checksum;
    }
    
    /**
     * @return The calculation date for the checksum of the file.
     */
    public Date getCalculationDate() {
        return calculationDate;
    }
}
