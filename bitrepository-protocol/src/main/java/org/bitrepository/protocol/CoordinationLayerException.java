/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.protocol;

/**
 * Throw if something as gone wrong in a attempt to interact with the coordination layer
 */
@SuppressWarnings("serial")
public class CoordinationLayerException extends RuntimeException {

    /**
     * Constructor for this exception based on a causing exception and described by a message.
     * @param message The message for the exception.
     */
    public CoordinationLayerException(String arg0, Throwable arg1) {
        super(arg0, arg1);
    }

    /**
     * Constructor for this exception based on a text message.
     * @param message The message for the exception.
     */
    public CoordinationLayerException(String arg0) {
        super(arg0);
    }
}
