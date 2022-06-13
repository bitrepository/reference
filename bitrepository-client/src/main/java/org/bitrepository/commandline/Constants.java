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

    public static final Boolean ARGUMENT_IS_REQUIRED = true;
    public static final Boolean ARGUMENT_IS_NOT_REQUIRED = false;
    public static final Boolean HAS_ARGUMENT = true;
    public static final Boolean NO_ARGUMENT = false;
    public static final Boolean NOT_ALLOWING_UNDEFINED_ARGUMENTS = false;
    /**
     * The path to the settings.
     */
    public static final String SETTINGS_ARG = "s";
    /**
     * The path to the private key.
     */
    public static final String PRIVATE_KEY_ARG = "k";
    /**
     *
     */
    public static final String VERBOSITY_ARG = "v";

    /**
     * The collectionID argument
     */
    public static final String COLLECTION_ID_ARG = "c";
    /**
     * The file argument.
     */
    public static final String FILE_ARG = "f";
    /**
     * The argument for the id of the file.
     */
    public static final String FILE_ID_ARG = "i";
    /**
     * The pillar argument.
     */
    public static final String PILLAR_ARG = "p";
    /**
     * The checksum of the file.
     */
    public static final String CHECKSUM_ARG = "C";
    /**
     * The checksum of the file to replace.
     */
    public static final String REPLACE_CHECKSUM_ARG = "r";
    /**
     * The argument for the type of checksum to request.
     */
    public static final String REQUEST_CHECKSUM_TYPE_ARG = "R";
    /**
     * The argument for the salt of the checksum to request.
     */
    public static final String REQUEST_CHECKSUM_SALT_ARG = "S";
    /**
     * The argument for deleting files afterwards.
     */
    public static final String DELETE_FILE_ARG = "d";
    /**
     * The argument for the URL of a file.
     */
    public static final String URL_ARG = "u";

    /**
     * The argument for the location of the results.
     */
    public static final String LOCATION = "l";

    public static final String REQUEST_CHECKSUM_TYPE_DESC =
            "[OPTIONAL] Request the use of a specific checksum algorithm in the response from the pillars. " +
                    "E.g. '-" + REQUEST_CHECKSUM_TYPE_ARG + " SHA1'.";

    public static final String REQUEST_CHECKSUM_SALT_DESC =
            "[OPTIONAL] A salt for the requested checksum specified by the checksum type argument " +
                    "[-" + REQUEST_CHECKSUM_TYPE_ARG + " REQUIRED]. Must be string of even length. " +
                    "E.g. '-" + REQUEST_CHECKSUM_TYPE_ARG + " HMAC_SHA1 -" + REQUEST_CHECKSUM_SALT_ARG + " abcd'";

    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_ARGUMENT_FAILURE = 1;
    public static final int EXIT_OPERATION_FAILURE = -1;

    public static void main(String[] args) {
        System.out.println(REQUEST_CHECKSUM_TYPE_DESC);
        System.out.println(REQUEST_CHECKSUM_SALT_DESC);
    }
}
