package org.bitrepository.access.getfile.selectors;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

public class SpecificPillarSelectorForGetFile2 extends GetFileSelector {

    private final String choosenPillar;
    
    public SpecificPillarSelectorForGetFile2(Collection<String> pillarsWhichShouldRespond, String choosenPillar) {
        responseStatus = new ContributorResponseStatus(pillarsWhichShouldRespond);
        this.choosenPillar = choosenPillar;
    }
    
    @Override
    protected boolean checkPillarResponseForSelection(IdentifyPillarsForGetFileResponse response) {
        if (!ResponseCode.IDENTIFICATION_POSITIVE.equals(
                response.getResponseInfo().getResponseCode())) {
            return false;
        } 
        if (response.getPillarID().equals(choosenPillar)) {
            return true;
        } 
        return false;
    }

}
