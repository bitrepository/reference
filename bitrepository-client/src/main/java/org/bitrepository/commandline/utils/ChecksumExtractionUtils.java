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

/**
 * Interface for handling the command line arguments.
 */
public class ChecksumExtractionUtils {
    public static ChecksumType extractChecksumType(CommandLineArgumentsHandler cmdHandler, Settings settings,
            OutputHandler output) {
        String type;
        if(cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_TYPE_ARG)) {
            type = cmdHandler.getOptionValue(Constants.REQUEST_CHECKSUM_TYPE_ARG).toUpperCase();
        } else { 
            type = ChecksumUtils.getDefault(settings).getChecksumType().name();
        }
        
        if(cmdHandler.hasOption(Constants.REQUEST_CHECKSUM_SALT_ARG)) {
            if(!type.startsWith("HMAC_")) {
                type = "HMAC_" + type;
            }
        } else {
            if(type.startsWith("HMAC_")) {
                String[] split = type.split("_");
                type = split[split.length - 1];
                output.error("HMAC checksum spec given, but no salt given. Using '" + type + "' instead");
            }
        }
        
        return ChecksumType.fromValue(type);
    }
}
