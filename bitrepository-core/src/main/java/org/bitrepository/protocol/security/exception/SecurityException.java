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
 * Exception to indicate a serious problem in the security package.
 */
@SuppressWarnings("serial")
public class SecurityException extends RuntimeException {

    /**
     * @param message the textual description of the cause.
     * @param cause   the reason for throwing the exception.
     */
    public SecurityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message the textual description of the cause.
     */
    public SecurityException(String message) {
        super(message);
    }

}
