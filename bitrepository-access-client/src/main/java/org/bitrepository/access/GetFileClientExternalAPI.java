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
package org.bitrepository.access;

/**
 * Interface for the external use of a GetFileClient.
 * A GetFileClient needs to inherit both this external interface and the internal interface 'GetFileClientAPI'.
 */
public interface GetFileClientExternalAPI {
    /**
     * Method for retrieving a file as fast as possible, by first identifying which pillars can deliver and how fast, 
     * and then choosing the fastest one for the actual retrieval.
     * 
     * @param fileId The id of the file to retrieve.
     * @param slaId The id for the SLA, where the file belongs.
     * @param number The number of pillars which should contain the file, and therefore needs to be identified.
     */
    void retrieveFastest(String fileId, String slaId, int number);
    
    /**
     * Method for retrieving a file from a given pillar.
     * 
     * @param fileId The id of the file to retrieve.
     * @param slaId The id of the SLA, where the file belongs.
     * @param pillarId The id of pillar, where the file should be retrieved from.
     */
    void getFile(String fileId, String slaId, String pillarId);
}
