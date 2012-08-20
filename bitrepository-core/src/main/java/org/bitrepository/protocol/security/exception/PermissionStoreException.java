/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.protocol.security.exception;


/**
 * Execption class to indicate a problem with a call to permission store.  
 */
@SuppressWarnings("serial")
public class PermissionStoreException extends Exception {

    /**
     * @param message the reason for creating the exception.
     */
    public PermissionStoreException(String message) {
        super(message);
    }
    
    /**
     * Constructor for PermissionStoreException
     * @param message the message describing the reason for the exception
     * @param e the exception that caused the creation of PermissionStoreException
     */
    public PermissionStoreException(String message, Throwable e) {
        super(message, e);
    }
}
