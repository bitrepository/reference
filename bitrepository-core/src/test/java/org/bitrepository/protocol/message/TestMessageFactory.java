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
package org.bitrepository.protocol.message;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.ProtocolVersionLoader;

import java.math.BigInteger;

/**
 *
 */
public abstract class TestMessageFactory {
    protected static final String CORRELATION_ID_DEFAULT = "CorrelationID";
    protected static final BigInteger VERSION_DEFAULT = ProtocolVersionLoader.loadProtocolVersion().getVersion();
    protected static final BigInteger MIN_VERSION_DEFAULT = ProtocolVersionLoader.loadProtocolVersion().getMinVersion();

    protected void initializeMessageDetails(Message msg) {
        msg.setVersion(VERSION_DEFAULT);
        msg.setMinVersion(MIN_VERSION_DEFAULT);
    }
}
