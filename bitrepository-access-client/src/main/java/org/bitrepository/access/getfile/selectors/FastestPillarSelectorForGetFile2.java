package org.bitrepository.access.getfile.selectors;

import java.util.Collection;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.protocol.utils.TimeMeasurementUtils;

public class FastestPillarSelectorForGetFile2 extends GetFileSelector {
    
    public FastestPillarSelectorForGetFile2(Collection<String> pillarsWhichShouldRespond) {
        responseStatus = new ContributorResponseStatus(pillarsWhichShouldRespond);
    }
    
    protected boolean checkPillarResponseForSelection(IdentifyPillarsForGetFileResponse response) {
        if (!ResponseCode.IDENTIFICATION_POSITIVE.equals(
                response.getResponseInfo().getResponseCode())) {
            return false;
        } 
        if (selectedPillar == null || TimeMeasurementUtils.compare(response.getTimeToDeliver(), 
                selectedPillar.getTimeToDeliver()) < 0) {
            return true;
        } 
        return false;
    }

    

}
