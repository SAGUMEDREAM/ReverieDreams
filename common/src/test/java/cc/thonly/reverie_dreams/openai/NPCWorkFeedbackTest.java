package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.entity.npc.work.NPCWorkFeedback;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NPCWorkFeedbackTest {
    @Test void unavailableFacilitiesSuppliesAndSpaceAreBlockers() {
        for (var code : new String[]{"no_nearby_water", "no_furnace", "no_farmland", "no_beehive", "need_fuel",
                "need_smelt_input", "inventory_full", "need_seeds", "need_matching_seeds", "need_smoke", "work_write_refused"})
            assertTrue(NPCWorkFeedback.classify(code).blocked(), code);
    }
    @Test void naturalWaitingAndActualLogisticsRemainRunning() {
        for (var code : new String[]{"ready", "waiting_for_crops", "waiting_for_fish", "waiting_for_smelting",
                "waiting_for_sheep", "waiting_for_honey", "storage_busy", "collecting_supplies", "unloading_outputs"})
            assertFalse(NPCWorkFeedback.classify(code).blocked(), code);
    }
}
