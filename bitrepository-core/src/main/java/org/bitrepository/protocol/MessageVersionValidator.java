/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.protocol;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocolversiondefinition.ProtocolVersionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle message version validation for implementation of versioning support.
 */
public class MessageVersionValidator {

    private static final Logger log = LoggerFactory.getLogger(MessageVersionValidator.class);
    private static ProtocolVersionDefinition protocolVersion = new ProtocolVersionDefinition();

    static {
        protocolVersion = ProtocolVersionLoader.loadProtocolVersion();
    }

    /**
     * Private constructor
     */
    private MessageVersionValidator() {
    }

    public static void validateMessageVersion(Message message) throws InvalidMessageVersionException {
        if (message.getMinVersion().compareTo(protocolVersion.getVersion()) > 0) {
            log.info("The Request minimum version is LARGER than this components version = " + protocolVersion.getVersion());
            throw new InvalidMessageVersionException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE,
                    "The requested minimum version is too large, components version: " + protocolVersion.getVersion());
        }
        if (message.getVersion() != null && message.getVersion().compareTo(protocolVersion.getMinVersion()) < 0) {
            log.info("The Request version " + message.getVersion() + " is SMALLER than this components minimum version = " +
                    protocolVersion.getMinVersion());
            throw new InvalidMessageVersionException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE,
                    "The requested version " + message.getVersion() +
                            " is smaller than the components minimum version, pillar minVersion:" + " " + protocolVersion.getMinVersion());
        }
    }

    public static String getProtocolVersion() {
        return protocolVersion.getVersion().toString();
    }
}
