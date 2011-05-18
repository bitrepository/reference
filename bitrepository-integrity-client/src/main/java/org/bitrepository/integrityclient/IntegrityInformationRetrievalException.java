/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityclient;

/**
 * Exception thrown on trouble retrieving integrity information.
 */
public class IntegrityInformationRetrievalException extends RuntimeException {
    /**
     * Create exception for integrity information retrieval error.
     * @param message What went wrong.
     */
    public IntegrityInformationRetrievalException(String message) {
        super(message);
    }

    /**
     * Create exception for integrity information retrieval error.
     * @param message What went wrong.
     * @param cause Exception that caused this exception.
     */
    public IntegrityInformationRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
