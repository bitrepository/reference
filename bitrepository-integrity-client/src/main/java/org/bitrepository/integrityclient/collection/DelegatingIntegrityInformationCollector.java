package org.bitrepository.integrityclient.collection;

import org.bitrepository.access.GetFileIDsClient;
import org.bitrepository.access.GetFileIDsClientImpl;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.integrityclient.IntegrityInformationRetrievalException;
import org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.List;

/**
 * Integrity information collector that delegates collecting information to the clients.
 */
public class DelegatingIntegrityInformationCollector implements IntegrityInformationCollector {
    /** The storage to store data in. */
    private final CachedIntegrityInformationStorage storage;
    /** The client for retrieving file IDs. */
    private final GetFileIDsClient getFileIDsClient;
    //TODO Real client
    /** The client for retrieving checksums. */
    private final Object getChecksumsClient;

    /**
     * Initialise a delegating integrity information collector.
     *
     * @param storage The storage to store data in.
     * @param getFileIDsClient The client for retrieving file IDs
     * @param getChecksumsClient The client for retrieving checksums
     */
    public DelegatingIntegrityInformationCollector(CachedIntegrityInformationStorage storage,
                                                   GetFileIDsClientImpl getFileIDsClient,
                                                   Object getChecksumsClient) {
        this.storage = storage;
        this.getFileIDsClient = getFileIDsClient;
        this.getChecksumsClient = getChecksumsClient;
    }

    @Override
    public void getFileIDs(String slaID, String pillarID, Collection<String> fileIDs) {
        List<IdentifyPillarsForGetFileIDsResponse> responses = getFileIDsClient.identifyPillarsForGetFileIDs(slaID);

        for (IdentifyPillarsForGetFileIDsResponse response: responses) {
            if (pillarID == null || pillarID.equals(response.getPillarID())) {
                File file = getFileIDsClient.getFileIDs(slaID, response.getReplyTo(), response.getPillarID());
                FileIDsData data = null;
                try {
                    data = JaxbHelper.loadXml(FileIDsData.class, new FileInputStream(file));
                } catch (JAXBException e) {
                    throw new IntegrityInformationRetrievalException("Unable to retrieve file IDs", e);
                } catch (FileNotFoundException e) {
                    throw new IntegrityInformationRetrievalException("Unable to retrieve file IDs", e);
                }
                storage.addFileIDs(data);
            }
        }
    }

    @Override
    public void getChecksums(String slaID, String pillarID, Collection<String> fileIDs, String checksumType, byte[] salt) {
        // TODO Implement real thing
        throw new IntegrityInformationRetrievalException("Not implemented");
    }
}
