package dev.m00nl1ght.clockwork.test;

import dev.m00nl1ght.clockwork.core.ClockworkCore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("ManifestReaderLoad")
public class ManifestReaderLoadTest extends ClockworkTest {

    @Test
    public void checkLoaded() {
        assertEquals(core().getState(), ClockworkCore.State.INITIALISED);
    }

}
