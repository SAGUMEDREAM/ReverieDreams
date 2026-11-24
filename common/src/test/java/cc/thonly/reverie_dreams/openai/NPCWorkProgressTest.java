package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.entity.npc.work.NPCWorkProgress;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NPCWorkProgressTest {
    @Test void inventoryGiftsCannotSatisfyAVerifiedProductionOrder() {
        var progress = NPCWorkProgress.production("minecraft:iron_ingot", 32);
        progress.sample(512);
        assertEquals(0, progress.acquired());
        assertFalse(progress.complete());
        progress.record("minecraft:gold_ingot", 32);
        assertFalse(progress.complete());
        progress.record("minecraft:iron_ingot", 32);
        assertTrue(progress.complete());
    }

    @Test void actualOutputMustAlsoBeAvailableBeforeDeliveryStepStarts() {
        var progress = NPCWorkProgress.production("minecraft:wheat", 32);
        progress.record("minecraft:wheat", 32);
        progress.sample(4);
        assertEquals(32, progress.acquired());
        assertFalse(progress.complete());
        progress.sample(32);
        assertTrue(progress.complete());
        progress.sample(0);
        assertFalse(progress.complete());
    }

    @Test void verifiedCountersSurviveReloadWithoutTurningInventorySamplesIntoProduction() {
        var progress = NPCWorkProgress.production("minecraft:iron_ingot", 32);
        progress.record("minecraft:iron_ingot", 12); progress.sample(64);
        var restored = NPCWorkProgress.load(progress.save());
        restored.sample(100);
        assertEquals(12, restored.acquired()); assertFalse(restored.complete());
        restored.record("minecraft:iron_ingot", 20);
        assertTrue(restored.complete());
    }

    @Test void offspringObjectivesUseSpawnCountsAndNeverInventory() {
        var progress = NPCWorkProgress.offspring("minecraft:cow", 2);
        progress.sample(200);
        assertFalse(progress.complete());
        progress.record("minecraft:sheep", 2);
        assertFalse(progress.complete());
        progress.record("minecraft:cow", 1);
        var restored = NPCWorkProgress.load(progress.save());
        assertFalse(restored.complete());
        restored.record("minecraft:cow", 1);
        assertTrue(restored.complete());
    }
    @Test void existingInventoryDoesNotCountAndRepeatedSamplesDoNotAccumulate() {
        var progress = new NPCWorkProgress("minecraft:wheat", 32, 64);
        progress.sample(80);
        progress.sample(80);
        assertEquals(16, progress.acquired());
        assertFalse(progress.complete());
        progress.sample(96);
        assertTrue(progress.complete());
    }

    @Test void saveReloadKeepsOriginalBaseline() {
        var progress = new NPCWorkProgress("minecraft:wheat", 32, 8);
        progress.sample(24);
        var restored = NPCWorkProgress.load(progress.save());
        restored.sample(24);
        assertEquals(16, restored.acquired());
        restored.sample(40);
        assertTrue(restored.complete());
    }

    @Test void RemovedItemsCannotCountAsStillHeld() {
        var progress = new NPCWorkProgress("minecraft:wheat", 32, 8);
        progress.sample(30);
        progress.sample(4);
        assertEquals(0, progress.acquired());
        assertFalse(progress.complete());
    }

    @Test void rejectsInvalidObjectives() {
        assertThrows(IllegalArgumentException.class, () -> new NPCWorkProgress("", 1, 0));
        assertThrows(IllegalArgumentException.class, () -> new NPCWorkProgress("minecraft:wheat", 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new NPCWorkProgress("minecraft:wheat", 1025, 0));
        assertThrows(IllegalArgumentException.class, () -> new NPCWorkProgress("minecraft:wheat", 32, -1));
    }
}
