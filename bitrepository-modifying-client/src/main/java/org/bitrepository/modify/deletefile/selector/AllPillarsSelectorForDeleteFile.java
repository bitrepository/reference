package org.bitrepository.modify.deletefile.selector;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;

public class AllPillarsSelectorForDeleteFile extends DeleteFileSelector {
    
    public AllPillarsSelectorForDeleteFile(Collection<String> pillarsWhichShouldRespond) {
        responseStatus = new ContributorResponseStatus(pillarsWhichShouldRespond);
    }
    
    protected boolean checkPillarResponseForSelection(IdentifyPillarsForDeleteFileResponse response) {
        return response.getResponseInfo().getResponseCode().equals(ResponseCode.IDENTIFICATION_POSITIVE);
    }


    

}
