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

/**
 * Container for the constants for the checksum database.
 */
public final class DatabaseConstants {
    /** Private constructor to prevent instantiation of this constant container.*/
    private DatabaseConstants() {}
    
    /** The table with the checksum entries.*/
    public static final String CHECKSUM_TABLE = "checksums";
    /** The column for the id of file.*/
    public static final String CS_FILE_ID = "fileid";
    /** The column for the checksum of the file.*/
    public static final String CS_CHECKSUM = "checksum";
    /** The column for the date for the calculation of the checksum.*/
    public static final String CS_DATE = "calculationdate";
    /** The column for the collection id for the file.*/
    public static final String CS_COLLECTION_ID = "collectionid";
    
    /** The column for the guid of the entry.*/
    public static final String CS_GUID = "guid";
    
}
