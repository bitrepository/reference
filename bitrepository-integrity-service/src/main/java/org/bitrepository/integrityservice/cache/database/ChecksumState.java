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
 * The different states for integrity check of the checksum of a given file.
 */
public enum ChecksumState {
    /** When the checksum of the given file is correct (e.g. won the vote).*/
    VALID,
    /** When the checksum of the given file is incorrect (e.g. lost the vote).*/
    ERROR,
    /** When no vote for the checksum has been performed.*/
    UNKNOWN;
    
    /**
     * Converts between an integer and the corresponding checksum state.
     * @param i The integer value for the checksum state.
     * @return The checksum state for the given integer.
     */
    public static ChecksumState fromOrdinal(Integer i) {
        switch (i) {
            case 0: return VALID;
            case 1: return ERROR;
            default: return UNKNOWN;
        }
    }
}
