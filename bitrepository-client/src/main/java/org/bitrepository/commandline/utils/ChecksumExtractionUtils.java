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
package org.bitrepository.commandline.utils;

import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.commandline.Constants;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.ChecksumUtils;

import java.util.Locale;

/**
 * Utility class for extraction of checksum parameters from the commandline arguments.
 */
public class ChecksumExtractionUtils {
    /**
     * Extracts the checksum type from the commandline arguments.
     * Ensures, that the HMAC checksum types is used, when a salt is given, 
     * and also that it is not the HMAC checksum type, which is used, when no salt is given.
     * Also, ignores cases for the checksum types.  
     * @param cmdHandler Contains the arguments from the commansline.
     * @param settings The settings, containing the default checksum type (if no checksum type is given).
     * @param output The OutputHandler, where output and logging information is delivered.
     * @return The checksum type.
     */
    public static ChecksumType extractChecksumType(CommandLineArgumentsHandler cmdHandler, Settings settings,
            OutputHandler output) {
        String type;
        if(cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG)) {
            type = cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG).toUpperCase(Locale.ROOT);
        } else { 
            type = ChecksumUtils.getDefault(settings).getChecksumType().name();
        }
        
        if(cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_SALT_ARG)) {
            if(!type.startsWith("HMAC_")) {
                type = "HMAC_" + type;
                output.debug("Non-HMAC checksum spec given, but also salt. Thus using '" + type + "' instead.");
            }
        } else {
            if(type.startsWith("HMAC_")) {
                type = type.replace("HMAC_", "");
                output.warn("HMAC checksum spec given, but no salt given. Using '" + type + "' instead.");
            }
        }
        
        return ChecksumType.fromValue(type);
    }
}
