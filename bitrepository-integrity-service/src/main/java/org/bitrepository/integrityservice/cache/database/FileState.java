/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.integrityservice.cache.database;

/**
 * The different states for a file in the integrity cache.
 */
public enum FileState {

    /** The state when the file has been found in the latest file list. */ 
    EXISTING,
    /** The state when the file was not found in the latest file list. */
    MISSING,
    /** The state when it is not known for the given file.*/
    UNKNOWN;
    
    /**
     * Converts between an integer and the actual file state.
     * @param i The integer value for the file state.
     * @return The file state for the given integer.
     */
    public static FileState fromOrdinal(Integer i) {
        switch (i) {
            case 0: return EXISTING;
            case 1: return MISSING;
            default: return UNKNOWN;
        }
    }
}
