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
 * Exception class to indicate that no permissions has been registered for the given operation type.
 */
@SuppressWarnings("serial")
public class UnregisteredPermissionException extends Exception {

    /**
     * @param message description of why the exception was created
     */
    public UnregisteredPermissionException(String message) {
        super(message);
    }

    /**
     * Constructor for UnregisteredPermissionException
     *
     * @param message description of why the exception was created
     * @param cause   the throwable that caused the exception to be created.
     */
    public UnregisteredPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
