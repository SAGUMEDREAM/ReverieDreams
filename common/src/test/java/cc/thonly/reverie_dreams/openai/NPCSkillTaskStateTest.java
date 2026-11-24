package cc.thonly.reverie_dreams.openai;

import cc.thonly.reverie_dreams.entity.npc.work.NPCPlan;

import com.google.gson.JsonParser;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NPCSkillTaskStateTest {
    @Test void offlineTaskAndInjuryRecoveryPolicySurviveSaveReloadWithoutChangingLegacyReflection() {
        var task = restored("smelt", "paused", ",\"model_reflection_enabled\":false,\"recovering\":true");
        var out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        task.save(out);
        var again = new NPCSkillTask();
        again.load(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, out.buildResult()));
        var data = JsonParser.parseString(again.context()).getAsJsonObject();
        assertFalse(data.get("model_reflection_enabled").getAsBoolean());
        assertTrue(data.get("recovering").getAsBoolean());
        assertFalse(data.get("manually_paused").getAsBoolean());
        assertTrue(JsonParser.parseString(restored("").context()).getAsJsonObject().get("model_reflection_enabled").getAsBoolean());
    }
    @Test void ownerPausedProductionKeepsItsPauseAndVerifiedCountersAcrossReload() {
        var task = restored("smelt", "paused", ",\"manually_paused\":true,\"progress\":{\"item\":\"minecraft:iron_ingot\",\"goal\":32,\"baseline\":0,\"held\":12,\"produced\":12,\"basis\":\"verified_production_and_available_inventory\"}");
        var out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        task.save(out);
        var again = new NPCSkillTask();
        again.load(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, out.buildResult()));
        var data = JsonParser.parseString(again.context()).getAsJsonObject();
        assertTrue(data.get("manually_paused").getAsBoolean());
        assertEquals("paused", data.get("phase").getAsString());
        assertEquals(12, data.getAsJsonObject("progress").get("produced").getAsInt());
        assertEquals(20, again.quantityRemaining());
    }
    @BeforeAll static void bootstrap() { SharedConstants.tryDetectVersion(); Bootstrap.bootStrap(); }

    private NPCSkillTask restored(String fields) {
        return restored("farm", "running", fields);
    }

    private NPCSkillTask restored(String skill, String phase, String fields) {
        var tag = new CompoundTag();
        tag.putString("ChatSkillTask", """
                {"skill":"%s","owner":"00000000-0000-0000-0000-000000000001",
                 "x":1,"y":64,"z":2,"phase":"%s","detail":"legacy","retries":0%s}
                """.formatted(skill, phase, fields));
        var task = new NPCSkillTask();
        task.load(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, tag));
        return task;
    }

    @Test void acceptsLegacySavesAndCreatesStableReceipt() {
        var task = restored("");
        assertTrue(task.active());
        var out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        task.save(out);
        var again = new NPCSkillTask();
        again.load(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, out.buildResult()));
        assertEquals(JsonParser.parseString(task.context()).getAsJsonObject().get("task_id"),
                JsonParser.parseString(again.context()).getAsJsonObject().get("task_id"));
    }

    @Test void taskLocationCannotBeReinterpretedInAnotherDimensionAndLegacyLocationIsUnverified() {
        assertFalse(restored("").matchesDimension("minecraft:overworld"));
        var task = restored(",\"dimension\":\"minecraft:overworld\"");
        assertTrue(task.matchesDimension("minecraft:overworld"));
        assertFalse(task.matchesDimension("minecraft:the_nether"));
        var output = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        task.save(output);
        var loaded = new NPCSkillTask();
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, output.buildResult()));
        assertTrue(loaded.matchesDimension("minecraft:overworld"));
        assertFalse(loaded.matchesDimension("minecraft:the_nether"));
    }

    @Test void preservesQuantityBaselineAndBlockedState() {
        var task = restored("farm", "blocked", ",\"progress\":{\"item\":\"minecraft:wheat\",\"goal\":32,\"baseline\":64,\"held\":80}");
        var data = JsonParser.parseString(task.context()).getAsJsonObject();
        assertEquals(16, data.getAsJsonObject("progress").get("acquired").getAsInt());
        assertEquals("blocked", data.get("phase").getAsString());
        assertTrue(task.active());
    }

    @Test void invalidSavedObjectiveStopsInsteadOfExecuting() {
        var task = restored(",\"progress\":{\"item\":\"unknown:missing\",\"goal\":32,\"baseline\":0,\"held\":0}");
        assertFalse(task.active());
        assertEquals("failed", JsonParser.parseString(task.context()).getAsJsonObject().get("phase").getAsString());
    }

    @Test void preservesPartialDeliveryAcrossEntitySaveReload() {
        var task = restored("deposit_items", "blocked", ",\"transfer_item\":\"minecraft:wheat\",\"transfer_goal\":32,\"transferred\":12");
        var out = TagValueOutput.createWithoutContext(ProblemReporter.DISCARDING);
        task.save(out);
        var again = new NPCSkillTask();
        again.load(TagValueInput.create(ProblemReporter.DISCARDING, RegistryAccess.EMPTY, out.buildResult()));
        var data = JsonParser.parseString(again.context()).getAsJsonObject();
        assertTrue(again.active());
        assertEquals(12, data.get("transferred").getAsInt());
        assertEquals(32, data.get("transfer_goal").getAsInt());
    }

    @Test void preservesPendingNextStepInsteadOfRewindingPlan() {
        var steps = JsonParser.parseString("""
                [{"skill":"go_to","x":1,"y":64,"z":2},
                 {"skill":"deposit_items","x":4,"y":64,"z":2,"item":"minecraft:wheat","count":32}]
                """).getAsJsonArray();
        var plan = new NPCPlan("Deliver wheat", steps);
        assertTrue(plan.advance());
        var task = restored("go_to", "completed", ",\"pending_step\":true,\"plan\":" + plan.save());
        var data = JsonParser.parseString(task.context()).getAsJsonObject();
        assertTrue(task.active());
        assertEquals(1, data.getAsJsonObject("plan").get("cursor").getAsInt());
        assertTrue(data.get("pending_step").getAsBoolean());
    }
}
