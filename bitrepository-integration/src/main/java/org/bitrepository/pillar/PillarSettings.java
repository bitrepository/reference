package org.bitrepository.pillar;

import org.bitrepository.protocol.bitrepositorycollection.BitRepositoryCollectionSettings;

public interface PillarSettings extends BitRepositoryCollectionSettings {

    public String getPillarId();
    
    public String getFileDirName();
    
    public String getLocalQueue();
    
    public Long getTimeToUploadValue();
    
    public String getTimeToUploadMeasure();
    
    public Long getTimeToDownloadValue();
    
    public String getTimeToDownloadMeasure();
}
