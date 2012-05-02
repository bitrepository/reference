package org.bitrepository.modify.replacefile.pillarselector;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;

public class AllPillarsSelectorForReplaceFile extends ReplaceFileSelector {
    
    public AllPillarsSelectorForReplaceFile(Collection<String> pillarsWhichShouldRespond) {
        responseStatus = new ContributorResponseStatus(pillarsWhichShouldRespond);
    }
    
    protected boolean checkPillarResponseForSelection(IdentifyPillarsForReplaceFileResponse response) {
        return response.getResponseInfo().getResponseCode().equals(ResponseCode.IDENTIFICATION_POSITIVE);
    }


    

}
