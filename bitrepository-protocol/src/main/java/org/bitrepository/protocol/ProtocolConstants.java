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

import java.io.IOException;
import java.util.Properties;

/** Defines the constants for this code base. */
public final class ProtocolConstants {
    /** Protocol version used. */
    public static final long PROTOCOL_VERSION = 1L;
    /** Protocol minimum version used. */
    public static final long PROTOCOL_MIN_VERSION = 1L;
    
    public static long getProtocolVersion() throws IOException, NumberFormatException {
    	long l; 
    	Properties properties = new Properties();
    	String version = null;
    	String[] splittedVersion;
    	
        properties.load(Thread.currentThread().getContextClassLoader()
                                    .getResourceAsStream("protocol_version.properties"));
            
        version = properties.getProperty("org.bitrepository.protocol.version");
        splittedVersion = version.split("\\D");
        l = Long.parseLong(splittedVersion[0]);
        
        return l;
    }
    
    public static long getProtocolMinVersion() throws IOException, NumberFormatException {
    	long l; 
    	Properties properties = new Properties();
    	String version = null;
    	String[] splittedVersion;

        properties.load(Thread.currentThread().getContextClassLoader()
                                    .getResourceAsStream("protocol_version.properties"));
            
        version = properties.getProperty("org.bitrepository.protocol.min_version");
        splittedVersion = version.split("\\D");
        l = Long.parseLong(splittedVersion[0]);
        
        return l;
    }
    
    /** Hides the constructor, preventing instantiation */
    private ProtocolConstants() {}
}
