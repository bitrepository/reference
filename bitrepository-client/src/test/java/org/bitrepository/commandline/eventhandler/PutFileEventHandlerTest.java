/*
 * #%L
 * Bitrepository Client
 * %%
 * Copyright (C) 2010 - 2026 The Royal Danish Library and The State Archives, Denmark
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
package org.bitrepository.commandline.eventhandler;

import org.apache.commons.codec.DecoderException;
import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.commandline.output.OutputHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.modify.putfile.conversation.PutFileCompletePillarEvent;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PutFileEventHandlerTest {
    private static final String COLLECTION_ID = "collection";

    private final Settings settings = TestSettingsProvider.getSettings(getClass().getSimpleName());
    private final OutputHandler output = mock(OutputHandler.class);

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void printsLineForPillarWithoutChecksumWhenSaltedChecksumIsRequested() throws DecoderException {
        PutFileEventHandler handler = new PutFileEventHandler(settings, output, hmacSha1Spec());

        handler.handleComponentComplete(new PutFileCompletePillarEvent("checksum-pillar", COLLECTION_ID, null,
                completed(), null));

        verify(output).resultLine("checksum-pillar \t HMAC_SHA1 \t - \t(no checksum returned, e.g. a checksum pillar)");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void printsReturnedSaltedChecksum() throws DecoderException {
        PutFileEventHandler handler = new PutFileEventHandler(settings, output, hmacSha1Spec());
        ChecksumDataForFileTYPE checksum = new ChecksumDataForFileTYPE();
        checksum.setChecksumValue(Base16Utils.encodeBase16("cae4d2161fb551e70a4176c1ae463ef37ade11f3"));

        handler.handleComponentComplete(new PutFileCompletePillarEvent("pillar", COLLECTION_ID, checksum,
                completed(), null));

        verify(output).resultLine("pillar \t HMAC_SHA1 \t cae4d2161fb551e70a4176c1ae463ef37ade11f3 \t*");
    }

    private static ChecksumSpecTYPE hmacSha1Spec() throws DecoderException {
        ChecksumSpecTYPE spec = new ChecksumSpecTYPE();
        spec.setChecksumType(ChecksumType.HMAC_SHA1);
        spec.setChecksumSalt(Base16Utils.encodeBase16("abab"));
        return spec;
    }

    private static ResponseInfo completed() {
        ResponseInfo info = new ResponseInfo();
        info.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        return info;
    }
}
