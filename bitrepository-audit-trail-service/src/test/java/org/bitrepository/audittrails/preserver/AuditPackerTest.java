package org.bitrepository.audittrails.preserver;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.settings.referencesettings.AuditTrailPreservation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuditPackerTest {
    private String collectionID;
    private AuditTrailPreservation preservationSettings;
    private AuditTrailStore store;

    @BeforeAll
    void setup() {
        Settings settings = TestSettingsProvider.reloadSettings("LocalAuditPreservationUnderTest");
        preservationSettings =
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailPreservation();
        collectionID = settings.getCollections().get(0).getID();
        SettingsUtils.initialize(settings);
        store = Mockito.mock(AuditTrailStore.class);
    }

    @Test
    void testCreateNewPackage() throws IOException {
        AuditPacker packer = new AuditPacker(store, preservationSettings, collectionID);
        Map<String, Long> seqNumsReached = packer.getSequenceNumbersReached();
        Assertions.assertEquals(3, seqNumsReached.size());
        Assertions.assertEquals(0, packer.getPackedAuditCount());

        // Create a stubbed event iterator for each expected contributor containing only one event.
        List<StubAuditEventIterator> iterators = List.of(
                new StubAuditEventIterator(), new StubAuditEventIterator(), new StubAuditEventIterator());

        Mockito.when(store.getAuditTrailsByIterator(
                any(), eq(collectionID), any(),
                any(), any(), any(),
                any(), (Instant) any(), (Instant) any(),
                any(), any())
        ).thenReturn(iterators.get(0)).thenReturn(iterators.get(1)).thenReturn(iterators.get(2));

        // Do the actual call to createNewPackage - this will fetch first event from the iterators.
        packer.createNewPackage();
        List<Long> expectedSeqNums = List.of(1L, 1L, 1L);
        Assertions.assertEquals(3, packer.getPackedAuditCount());
        Assertions.assertIterableEquals(expectedSeqNums, packer.getSequenceNumbersReached().values());

        // As the iterators have no new audits there should be no newly packed audits on a new call.
        packer.createNewPackage();
        Assertions.assertEquals(0, packer.getPackedAuditCount());
        Assertions.assertIterableEquals(expectedSeqNums, packer.getSequenceNumbersReached().values());
    }
}
