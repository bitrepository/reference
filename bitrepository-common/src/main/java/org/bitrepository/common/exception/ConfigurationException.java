/*
 * #%L
 * bitrepository-common
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
package org.bitrepository.common.exception;

/**
 * Thrown when problems are encountered with accessing a Bitrepository configuration
 */
@SuppressWarnings("serial")
public class ConfigurationException extends RuntimeException {

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public ConfigurationException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}
}
