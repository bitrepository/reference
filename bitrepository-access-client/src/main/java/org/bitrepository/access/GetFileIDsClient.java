package org.bitrepository.access;

import java.io.File;
import java.util.List;

/**
 * Interface for GetFileIDs client.
 */
public interface GetFileIDsClient {
    /**
     * Identify potential pillars for retrieving File IDs from pillar.
     *
     * @param slaID The ID of a collection.
     * @param fileIDs A pattern for file IDs in that collection.
     * @return A list of IDs of pillars that could respond to this request.
     */
    List<String> identifyPillarsForGetFileIDs(String slaID, String fileIDs);

    /**
     * Retrieve a set of File IDs from pillar.
     *
     * @param slaID The ID of a collection.
     * @param fileIDs A pattern for file IDs in that collection.
     * @param fileIDs A pattern for file IDs
     * @return A file containing a set of File IDs. The file is in XML format.
     */
    File getFileIDs(String slaID, String fileIDs, String pillarID);
}
