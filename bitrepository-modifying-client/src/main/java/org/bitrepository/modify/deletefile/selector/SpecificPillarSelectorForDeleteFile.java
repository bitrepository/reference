package org.bitrepository.modify.deletefile.selector;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;

public class SpecificPillarSelectorForDeleteFile extends DeleteFileSelector {

    private final String choosenPillar;
    
    public SpecificPillarSelectorForDeleteFile(Collection<String> pillarsWhichShouldRespond, String choosenPillar) {
        responseStatus = new ContributorResponseStatus(pillarsWhichShouldRespond);
        this.choosenPillar = choosenPillar;
    }
    
    @Override
    protected boolean checkPillarResponseForSelection(IdentifyPillarsForDeleteFileResponse response) {
        if (!ResponseCode.IDENTIFICATION_POSITIVE.equals(
                response.getResponseInfo().getResponseCode())) {
            return false;
        } 
        
        return response.getPillarID().equals(choosenPillar); 
    }

}
