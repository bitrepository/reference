/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: MutableGetFileClientSettings.java 240 2011-07-28 07:55:25Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-access-client/src/main/java/org/bitrepository/access/getfile/MutableGetFileClientSettings.java $
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
package org.bitrepository.access.getchecksums;

import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;

/**
 * Instantiable instance for the GetChecksumClientSettings.
 */
public class MutableGetChecksumsClientSettings extends MutableClientSettings
        implements GetChecksumsClientSettings {

    /** @see GetChecksumsClientSettings#getGetChecksumsDefaultTimeout() */
    private long getChecksumsDefaultTimeout;
    @Override
    public long getGetChecksumsDefaultTimeout() {
        return getChecksumsDefaultTimeout;
    }
    /** @see GetChecksumsClientSettings#getGetChecksumsDefaultTimeout() */
    public void setGetChecksumsDefaultTimeout(long getFileDefaultTimeout) {
        this.getChecksumsDefaultTimeout = getFileDefaultTimeout;
    }
}
