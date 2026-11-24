package cc.thonly.reverie_dreams.block.redstone;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RedstoneSettingsTest {
    @Test void cartIdentifiersAreExactAndBlankIsWildcard() {
        assertTrue(RedstoneSettings.matches("", "any train"));
        assertTrue(RedstoneSettings.matches("A", "A"));
        assertFalse(RedstoneSettings.matches("A", "AB"));
        assertFalse(RedstoneSettings.matches("AB", "A"));
        assertFalse(RedstoneSettings.matches("A", ""));
        assertFalse(RedstoneSettings.matches("A", null));
    }
    @Test void channelCanBeDisabledButNotHalfConfigured() {
        assertTrue(RedstoneSettings.validChannel("station", "west"));
        assertTrue(RedstoneSettings.validChannel(" ", ""));
        assertFalse(RedstoneSettings.validChannel("station", " "));
        assertFalse(RedstoneSettings.validChannel("", "key"));
        assertFalse(RedstoneSettings.validChannel("x".repeat(31), "key"));
        assertFalse(RedstoneSettings.validChannel("station", "x".repeat(31)));
    }
    @Test void oldSavedDelayValuesNormalizeWithoutOverflow() {
        assertEquals(0, RedstoneSettings.delay(Integer.MIN_VALUE));
        assertEquals(0, RedstoneSettings.delay(0));
        assertEquals(20, RedstoneSettings.delay(20));
        assertEquals(72000, RedstoneSettings.delay(Integer.MAX_VALUE));
    }
}
