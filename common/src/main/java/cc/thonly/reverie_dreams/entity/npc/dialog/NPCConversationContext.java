package cc.thonly.reverie_dreams.entity.npc.dialog;

import cc.thonly.reverie_dreams.api.entity.type.ChatAIEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.ai.npc.NPCPersonas;
import cc.thonly.reverie_dreams.ai.ProviderRegistry;
import cc.thonly.reverie_dreams.server.ChatAIManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the conversation context from an NPC's current server state.
 */
public final class NPCConversationContext {
    private NPCConversationContext() {
    }

    public static String build(NPCSimpleEntity npc, ServerPlayer player) {
        Map<String, String> kw = new LinkedHashMap<>();
        kw.put("physical_condition", describeHealth(npc));
        kw.put("appetite", describeAppetite(npc.getFoodData().getNutrition()));
        kw.put("attitude_towards_player", describeFavorability(npc.getFavorabilityContainer().get(player.getUUID())));
        kw.put("player_can_issue_world_orders", Boolean.toString(npc.isOwnedBy(player)));
        kw.put("skill_task", npc.getSkillTask().context());
        kw.put("behavior_profile", NPCPersonas.behavior(npc.getRoleType().identify()).context().toString());
        kw.put("personal_memory", npc.getMemory().recall(player.getUUID()).toString());
        var provider = ProviderRegistry.get(ChatAIManager.chatType());
        kw.put("world_tools_available", Boolean.toString(provider != null && provider.supportsWorldTools()));
        LivingEntity owner = npc.getOwner();
        if (owner != null) {
            kw.put("owner", owner.getPlainTextName());
        }
        return ChatAIEntity.GSON.toJson(kw);
    }

    private static String describeHealth(NPCSimpleEntity npc) {
        float maxHealth = npc.getMaxHealth();
        if (maxHealth <= 0.0f) {
            return "healthy";
        }
        float ratio = npc.getHealth() / maxHealth;
        if (ratio > 0.75f) {
            return "healthy";
        }
        if (ratio > 0.5f) {
            return "slightly wounded";
        }
        if (ratio > 0.25f) {
            return "badly wounded";
        }
        return "on the verge of collapse";
    }

    private static String describeAppetite(int nutrition) {
        if (nutrition >= 16) {
            return "full";
        }
        if (nutrition >= 10) {
            return "satisfied";
        }
        if (nutrition >= 4) {
            return "hungry";
        }
        return "starving";
    }

    private static String describeFavorability(double favorability) {
        if (favorability >= 150.0) {
            return "very close";
        }
        if (favorability >= 100.0) {
            return "friendly";
        }
        if (favorability >= 60.0) {
            return "neutral";
        }
        if (favorability >= 30.0) {
            return "wary";
        }
        return "hostile";
    }
}
