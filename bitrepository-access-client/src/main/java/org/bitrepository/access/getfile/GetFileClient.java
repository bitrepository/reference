/*
 * #%L
 * bitrepository-access-client
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
package org.bitrepository.access.getfile;

/**
 * Interface for the external use of a GetFileClient.
 * A GetFileClient needs to inherit both this external interface and the internal interface 'GetFileClientAPI'.
 */
public interface GetFileClient {
    /**
     * Method for retrieving a file as fast as possible, by first identifying which pillars can deliver and how fast, 
     * and then choosing the fastest one for the actual retrieval.
     *
     * @param fileId The id of the file to retrieve.
     */
    void retrieveFastest(String fileId);
    
    /**
     * Method for retrieving a file from a given pillar.
     *
     * @param fileId The id of the file to retrieve.
     * @param pillarId The id of pillar, where the file should be retrieved from.
     */
    void getFile(String fileId, String pillarId);
}
