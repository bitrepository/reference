/*
 * #%L
 * Bitmagasin modify client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.modify;

import java.io.File;

/**
 * This is the external interface for the put client.
 * A put client must have both the internal (PutClientAPI) and the external (PutClientExternalAPI) APIs.
 */
interface PutClientExternalAPI {
    /**
     * Method for putting a file with a given ID.
     * 
     * @param file The file to put.
     * @param fileId The unique identification for the file (unique within 
     * the SLA).
     * @param slaId The ID for the SLA which the file belongs to.
     */
    abstract void putFileWithId(File file, String fileId, String slaId);
}
