package com.namelessmc.java_api;

import com.namelessmc.java_api.exception.UnknownNamelessVersionException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NamelessVersionTest {
    @Test void acceptsBothProductionAndPortVersionFamilies() throws Exception {
        assertSame(NamelessVersion.V2_2, NamelessVersion.parse("2.2.5"));
        for (String release : new String[]{"2.3.0", "2.3.1", "2.3.99"}) {
            assertSame(NamelessVersion.V2_3, NamelessVersion.parse(release));
            assertTrue(NamelessVersion.isSupportedByJavaApi(NamelessVersion.parse(release)));
        }
        assertTrue(NamelessVersion.isSupportedByJavaApi(NamelessVersion.V2_2));
        assertTrue(NamelessVersion.supportedVersions().contains(NamelessVersion.V2_3));
    }

    @Test void keepsUnknownFamiliesAndUnreviewedPrereleasesClosed() {
        for (String release : new String[]{"2.4.0", "3.0.0", "2.3.0-pr1", "2.3", "2.3.develop"}) {
            assertThrows(UnknownNamelessVersionException.class, () -> NamelessVersion.parse(release), release);
        }
    }
}
