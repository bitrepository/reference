package org.bitrepository.modify.replacefile.pillarselector;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;

public class SpecificPillarSelectorForReplaceFile extends ReplaceFileSelector {

    private final String choosenPillar;
    
    public SpecificPillarSelectorForReplaceFile(Collection<String> pillarsWhichShouldRespond, String choosenPillar) {
        responseStatus = new ContributorResponseStatus(pillarsWhichShouldRespond);
        this.choosenPillar = choosenPillar;
    }
    
    @Override
    protected boolean checkPillarResponseForSelection(IdentifyPillarsForReplaceFileResponse response) {
        if (!ResponseCode.IDENTIFICATION_POSITIVE.equals(
                response.getResponseInfo().getResponseCode())) {
            return false;
        } 
        
        return response.getPillarID().equals(choosenPillar); 
    }

}
