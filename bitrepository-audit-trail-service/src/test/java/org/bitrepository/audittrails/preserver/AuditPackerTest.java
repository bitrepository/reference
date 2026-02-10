package org.bitrepository.audittrails.preserver;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.settings.referencesettings.AuditTrailPreservation;
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class AuditPackerTest extends ExtendedTestCase {
    private String collectionID;
    private AuditTrailPreservation preservationSettings;
    private AuditTrailStore store;

    @BeforeAll
    public void setup() {
        Settings settings = TestSettingsProvider.reloadSettings("LocalAuditPreservationUnderTest");
        preservationSettings = settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailPreservation();
        collectionID = settings.getCollections().get(0).getID();
        SettingsUtils.initialize(settings);
        store = mock(AuditTrailStore.class);
    }

    @Test
    public void testCreateNewPackage() throws IOException {
        AuditPacker packer = new AuditPacker(store, preservationSettings, collectionID);
        Map<String, Long> seqNumsReached = packer.getSequenceNumbersReached();
        assertEquals(3, seqNumsReached.size());
        assertEquals(0, packer.getPackedAuditCount());

        // Create a stubbed event iterator for each expected contributor containing only one event.
        List<StubAuditEventIterator> iterators = of(
                new StubAuditEventIterator(), new StubAuditEventIterator(), new StubAuditEventIterator());

        when(store.getAuditTrailsByIterator(
                any(), eq(collectionID), anyString(), any(Long.class), any(), any(), any(), any(), any(), any(), any())
        ).thenReturn(iterators.get(0)).thenReturn(iterators.get(1)).thenReturn(iterators.get(2));

        // Do the actual call to createNewPackage - this will fetch first event from the iterators.
        packer.createNewPackage();
        Long[] expectedSeqNums = {1L, 1L, 1L};
        assertEquals(3, packer.getPackedAuditCount());
        assertArrayEquals(expectedSeqNums, packer.getSequenceNumbersReached().values().toArray(new Long[0]));

        // As the iterators have no new audits there should be no newly packed audits on a new call.
        packer.createNewPackage();
        assertEquals(0, packer.getPackedAuditCount());
        assertArrayEquals(expectedSeqNums, packer.getSequenceNumbersReached().values().toArray(new Long[0]));
    }
}
