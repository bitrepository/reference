/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getchecksums;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.BitRepositoryClient;
import org.bitrepository.client.eventhandler.EventHandler;

import java.net.URL;

/**
 * Used for retrieving checksums for files stored on the collection pillars.
 */
public interface GetChecksumsClient extends BitRepositoryClient {

    /**
     * Retrieves the checksums for a set of files.
     * <p>
     * If the number of fileIDs in a collection is large (10.000's) the fileIDs should  be retrieved in chunks by
     * using the <code>ContributorQuery</code> functionality.
     * <p>
     * Since every pillar cannot upload their checksums to the same URL, it is extended with the pillarID for the given
     * pillar, e.g.: 'http://upload.url/mypath' + '-pillarID'.
     * <p>
     * The results are returned through as a ChecksumsCompletePillarCompete event as the results are returned by the
     * pillars.
     *
     * @param collectionID          Identifies the collection to request checksums for.
     * @param contributorQueries    Defines which fileIDs to retrieve. If null all fileIDs from all contributors are
     *                              returned.
     * @param fileID                The optional fileID to retrieve file information for. If <code>null</code> file information are
     *                              retrieved for all files.
     * @param checksumSpec          Specification of how the type of checksums. If no checksum spec is specified the default
     *                              checksum type will be returned.
     * @param addressForResult      [OPTIONAL] The address to upload the calculated checksums to. If this is null, then the
     *                              results will be retrieved through the message.
     * @param eventHandler          [OPTIONAL] The handler which should receive notifications of the events occurring in
     *                              connection with the pillar communication.
     * @param auditTrailInformation AuditTrail information for the contributors
     */
    void getChecksums(String collectionID, ContributorQuery[] contributorQueries, String fileID,
                      ChecksumSpecTYPE checksumSpec, URL addressForResult, EventHandler eventHandler,
                      String auditTrailInformation);
}
