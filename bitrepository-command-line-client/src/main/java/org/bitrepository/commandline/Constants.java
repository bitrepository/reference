/*
 * #%L
 * Bitrepository Command Line
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
package org.bitrepository.commandline;

/**
 * Container for the constants for this package.
 */
public class Constants {
    /**
     * Private constructor to prevent instantiation.
     */
    private Constants() {}
    
    /** If an argument is required.*/
    public static final Boolean ARGUMENT_IS_REQUIRED = true;
    /** If an argument is not required.*/
    public static final Boolean ARGUMENT_IS_NOT_REQUIRED = false;
    /** Whether a given option has an argument.*/
    public static final Boolean HAS_ARGUMENT = true;
    /** For not allowing undefined arguments when parsing of arguments.*/
    public static final Boolean NOT_ALLOWING_UNDEFINED_ARGUMENTS = false;
    
    /** The path to the settings.*/ 
    public static final String SETTINGS_ARG = "s";
    /** The path to the private key.*/
    public static final String PRIVATE_KEY_ARG = "k";
    
    /** The file argument.*/
    public static final String FILE_ARG = "f";
    /** The argument for the id of the file.*/
    public static final String FILE_ID_ARG = "i";
    /** The pillar argument.*/
    public static final String PILLAR_ARG = "p";
    /** The checksum of the file.*/
    public static final String CHECKSUM_ARG = "c";
    /** The argument for the type of checksum to request.*/
    public static final String REQUEST_CHECKSUM_TYPE_ARG = "R";
    /** The argument for the salt of the checksum to request.*/
    public static final String REQUEST_CHECKSUM_SALT_ARG = "S";
    
    /** The argument for the location of the results.*/
    public static final String LOCATION = "l";
    
    
    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_ARGUMENT_FAILURE = 1;
    public static final int EXIT_OPERATION_FAILURE = -1;
    
}
