package cc.thonly.reverie_dreams.paper.plants;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PlantRulesTest {
    @Test void sourceFarmlandIdentityAndSpacing() {
        int[] below = new int[9]; Arrays.fill(below, -1);
        assertEquals(1.0f, PlantRules.moisture(below, false, false, false));
        assertEquals(26, PlantRules.growthDenominator(PlantRules.moisture(below, false, false, false)));
        below[4] = 0; assertEquals(2.0f, PlantRules.moisture(below, false, false, false));
        below[4] = 7; assertEquals(4.0f, PlantRules.moisture(below, false, false, false));
        Arrays.fill(below, 7);
        assertEquals(10.0f, PlantRules.moisture(below, false, false, false));
        assertEquals(5.0f, PlantRules.moisture(below, true, true, false));
        assertEquals(5.0f, PlantRules.moisture(below, false, false, true));
        assertEquals(10.0f, PlantRules.moisture(below, true, false, false));
        assertEquals(3, PlantRules.growthDenominator(10.0f));
        assertEquals(6, PlantRules.growthDenominator(5.0f));
    }
    @Test void brightFruitGrowsOneOutOfExactSourceDenominator() {
        for (int denominator : new int[] {3, 6, 7, 13, 26, 51}) {
            for (int age = 0; age < 3; age++) {
                int successes = 0;
                for (int draw = 0; draw < denominator; draw++) {
                    assertFalse(PlantRules.grows(age, 8, draw, denominator));
                    if (PlantRules.grows(age, 9, draw, denominator)) successes++;
                    assertFalse(PlantRules.grows(3, 15, draw, denominator));
                }
                assertEquals(1, successes);
            }
        }
    }
    @Test void boneMealAndHarvestPreserveDistinctSourceAges() {
        assertEquals(1, PlantRules.HARVEST_AGE);
        assertEquals(1, PlantRules.boneMealAge(0));
        assertEquals(2, PlantRules.boneMealAge(1));
        assertEquals(3, PlantRules.boneMealAge(2));
        assertEquals(3, PlantRules.boneMealAge(3));
    }
    @Test void invalidSourceValuesReject() {
        assertThrows(IllegalArgumentException.class, () -> PlantRules.moisture(new int[8], false, false, false));
        int[] invalid = new int[9]; invalid[0] = 8;
        assertThrows(IllegalArgumentException.class, () -> PlantRules.moisture(invalid, false, false, false));
        assertThrows(IllegalArgumentException.class, () -> PlantRules.boneMealAge(-1));
        assertThrows(IllegalArgumentException.class, () -> PlantRules.boneMealAge(4));
        assertThrows(IllegalArgumentException.class, () -> PlantRules.grows(0, 10, 3, 3));
        assertThrows(IllegalArgumentException.class, () -> PlantRules.growthDenominator(Float.NaN));
        assertThrows(IllegalArgumentException.class, () -> PlantRules.growthDenominator(0));
    }
}
