package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.entity.npc.work.NPCPlan;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NPCPlanTest {
    @Test void smeltingAndBreedingHaveBoundedVerifiableIntermediateObjectives() {
        var plan = new NPCPlan("Produce and deliver iron", steps("""
            [{"skill":"smelt","x":0,"y":64,"z":0,"target_item":"minecraft:iron_ingot","target_count":32},
             {"skill":"deposit_items","x":4,"y":64,"z":0,"item":"minecraft:iron_ingot","count":32}]
            """));
        assertTrue(plan.advance());
        assertEquals("deposit_items", plan.current().get("skill").getAsString());
        assertDoesNotThrow(() -> new NPCPlan("Breed two cows then wait", steps("""
            [{"skill":"breed_animals","x":0,"y":64,"z":0,"target_entity":"minecraft:cow","target_count":2},
             {"skill":"wait_here","x":0,"y":64,"z":0}]
            """)));
        assertThrows(IllegalArgumentException.class, () -> new NPCPlan("bad", steps("""
            [{"skill":"breed_animals","x":0,"y":64,"z":0,"target_item":"minecraft:wheat","target_count":2}]
            """)));
    }
    private JsonArray steps(String text) { return JsonParser.parseString(text).getAsJsonArray(); }

    @Test void harvestThenDeliveryResumesAtNextStep() {
        var plan = new NPCPlan("Collect wheat and deliver it", steps("""
            [{"skill":"farm","x":0,"y":64,"z":0,"target_item":"minecraft:wheat","target_count":32},
             {"skill":"deposit_items","x":4,"y":64,"z":0,"item":"minecraft:wheat","count":32}]
            """));
        assertTrue(plan.advance());
        var restored = NPCPlan.load(plan.save());
        assertEquals("deposit_items", restored.current().get("skill").getAsString());
        assertFalse(restored.advance());
        assertFalse(restored.hasCurrent());
    }

    @Test void rejectsUnboundedIntermediateWorkAndUnreachableStepDistances() {
        assertThrows(IllegalArgumentException.class, () -> new NPCPlan("test", steps("""
            [{"skill":"farm","x":0,"y":64,"z":0},{"skill":"wait_here","x":1,"y":64,"z":0}]
            """)));
        assertThrows(IllegalArgumentException.class, () -> new NPCPlan("test", steps("""
            [{"skill":"go_to","x":0,"y":64,"z":0},{"skill":"go_to","x":100,"y":64,"z":0}]
            """)));
    }

    @Test void rejectsFractionalQuantitiesAndExcessivePlansBeforeExecution() {
        assertThrows(IllegalArgumentException.class, () -> new NPCPlan("test", steps("""
            [{"skill":"deposit_items","x":0,"y":64,"z":0,"item":"minecraft:wheat","count":1.5}]
            """)));
        var entries = new JsonArray();
        for (int i=0; i<9; i++) entries.add(JsonParser.parseString("{\"skill\":\"go_to\",\"x\":0,\"y\":64,\"z\":0}"));
        assertThrows(IllegalArgumentException.class, () -> new NPCPlan("test", entries));
    }
}
