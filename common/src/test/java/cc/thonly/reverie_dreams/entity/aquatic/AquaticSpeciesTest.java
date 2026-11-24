package cc.thonly.reverie_dreams.entity.aquatic;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class AquaticSpeciesTest {
    @Test void nineCanonicalSpeciesRemainDistinctAndRegistryIndependent() {
        assertEquals(Set.of("trout", "lamprey", "salmon", "tuna", "supreme_tuna", "shrimp", "crab", "octopus", "sea_urchin"),
                Arrays.stream(AquaticSpecies.values()).map(AquaticSpecies::key).collect(Collectors.toSet()));
        assertEquals(9, AquaticSpecies.values().length);
        assertNotEquals(AquaticSpecies.TUNA, AquaticSpecies.SUPREME_TUNA);
        assertTrue(AquaticSpecies.SUPREME_TUNA.width() > AquaticSpecies.TUNA.width());
        assertTrue(AquaticSpecies.LAMPREY.bottomDweller());
    }

    @Test void dimensionsAndAttributesAreFiniteAndTheUrchinHasNoWanderingSpeed() {
        for (var species : AquaticSpecies.values()) {
            assertTrue(Float.isFinite(species.width()) && species.width() > 0 && species.width() <= 1);
            assertTrue(Float.isFinite(species.height()) && species.height() > 0 && species.height() < 1);
            assertTrue(Double.isFinite(species.health()) && species.health() > 0);
            assertTrue(Double.isFinite(species.speed()) && species.speed() >= 0);
            assertEquals(species.stationary(), species.speed() == 0);
        }
        assertTrue(AquaticSpecies.SEA_URCHIN.stationary());
    }

    @Test void swimmingFloppingAndBottomWalkingUseTheSharedExactClipNames() {
        for (var species : new AquaticSpecies[]{AquaticSpecies.TROUT, AquaticSpecies.SALMON, AquaticSpecies.TUNA, AquaticSpecies.SUPREME_TUNA}) {
            assertEquals("animation.swim", species.animationName(true, true));
            assertEquals("animation.idle", species.animationName(true, false));
            assertEquals("animation.flop", species.animationName(false, false));
        }
        assertEquals("animation.swim", AquaticSpecies.LAMPREY.animationName(true, true));
        assertEquals("animation.swim", AquaticSpecies.OCTOPUS.animationName(true, true));
        for (var species : new AquaticSpecies[]{AquaticSpecies.SHRIMP, AquaticSpecies.CRAB}) {
            assertEquals("animation.walk", species.animationName(true, true));
            assertEquals("animation.walk", species.animationName(false, true));
            assertEquals("animation.idle", species.animationName(false, false));
            assertFalse(species.flops());
        }
        for (boolean water : new boolean[]{false, true}) for (boolean moving : new boolean[]{false, true})
            assertEquals("animation.idle", AquaticSpecies.SEA_URCHIN.animationName(water, moving));
    }
}
