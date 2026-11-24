package cc.thonly.reverie_dreams.ai.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ai.AIUsageBudget;
import cc.thonly.reverie_dreams.ai.AIUsageSavedData;
import net.minecraft.server.level.ServerPlayer;
import com.google.gson.JsonObject;

public final class NPCUsageView {
    private NPCUsageView() {
    }

    public static JsonObject of(ServerPlayer player) {
        var config = ReverieDreams.config();
        var limits = new AIUsageBudget.Limits(Math.clamp(config.aiDailyRequests, 0, 10000),
                Math.clamp(config.aiPlayerDailyRequests, 0, 10000), Math.clamp(config.aiBackgroundDailyRequests, 0, 10000),
                Math.clamp(config.aiDailyTokenBudget, 0, 100_000_000), Math.clamp(config.aiPlayerDailyTokenBudget, 0, 100_000_000));
        return player.level().getServer().overworld().getDataStorage().computeIfAbsent(AIUsageSavedData.TYPE)
                .budget().snapshot(AIUsageBudget.today(), player.getUUID(), limits);
    }
}
