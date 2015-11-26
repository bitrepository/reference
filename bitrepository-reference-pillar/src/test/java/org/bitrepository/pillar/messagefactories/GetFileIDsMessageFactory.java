/*
 * #%L
 * Bitrepository Integration
 * 
 * $Id: PillarTestMessageFactory.java 659 2011-12-22 15:56:07Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/PillarTestMessageFactory.java $
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
package org.bitrepository.pillar.messagefactories;

import java.math.BigInteger;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.common.settings.Settings;

public class GetFileIDsMessageFactory extends PillarTestMessageFactory {
    public GetFileIDsMessageFactory(String collectionID, Settings clientSettings, String pillarID, 
            String pillarDestination) {
        super(collectionID, clientSettings, pillarID, pillarDestination);
    }
    
    public IdentifyPillarsForGetFileIDsRequest createIdentifyPillarsForGetFileIDsRequest(FileIDs fileID) {
        IdentifyPillarsForGetFileIDsRequest res = new IdentifyPillarsForGetFileIDsRequest();
        initializeIdentifyRequest(res);
        res.setFileIDs(fileID);
        return res;
    }
    
    public GetFileIDsRequest createGetFileIDsRequest(FileIDs fileID, String url) {
        GetFileIDsRequest res = new GetFileIDsRequest();
        initializeOperationRequest(res);
        res.setFileIDs(fileID);
        res.setResultAddress(url);
        res.setPillarID(pillarID);
        return res;
    }
    
    public GetFileIDsRequest createGetFileIDsRequest(FileIDs fileID, String url, Long maxResults, XMLGregorianCalendar maxDate, XMLGregorianCalendar minDate) {
        GetFileIDsRequest res = createGetFileIDsRequest(fileID, url);
        if(maxResults != null) { 
            res.setMaxNumberOfResults(BigInteger.valueOf(maxResults));
        }
        if(maxDate != null) {
            res.setMaxTimestamp(maxDate);
        }
        if(minDate != null) {
            res.setMinTimestamp(minDate);
        }
        return res;
    }

}
