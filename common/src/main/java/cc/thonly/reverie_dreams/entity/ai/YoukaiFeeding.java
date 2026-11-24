package cc.thonly.reverie_dreams.entity.ai;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.YoukaiFeedingSource;
import cc.thonly.reverie_dreams.api.player.PlayerComponentManager;
import cc.thonly.reverie_dreams.component.BasePlayerComponentManager;
import cc.thonly.reverie_dreams.registry.tag.RDItemTags;
import cc.thonly.reverie_dreams.server.player.FactionReputationComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public final class YoukaiFeeding {
    private YoukaiFeeding() {
    }

    public static boolean isMeat(ItemStack stack) {
        return stack.is(RDItemTags.YOUKAI_MEAT);
    }

    public static @Nullable UUID feeder(ItemEntity item) {
        return item instanceof YoukaiFeedingSource source ? source.reverie_dreams$getYoukaiFeeder() : null;
    }

    public static boolean mayMerge(@Nullable UUID first, @Nullable UUID second) {
        return Objects.equals(first, second);
    }

    public static long day(ServerLevel level) {
        return Math.floorDiv(level.getOverworldClockTime(), 24000L);
    }

    public static boolean canAccept(ItemEntity item, ServerLevel level) {
        UUID id = feeder(item);
        if (id == null)
            return true;
        FactionReputationComponent data = reputation(id, level);
        return data != null && data.canFeedYoukai(day(level));
    }

    /**
     * Called only on the server thread, immediately before consuming exactly one item.
     */
    public static boolean accept(ItemEntity item, ServerLevel level) {
        UUID id = feeder(item);
        if (id == null)
            return true;
        FactionReputationComponent data = reputation(id, level);
        if (data == null || !data.acceptYoukaiFeeding(day(level)))
            return false;
        PlayerComponentManager.serverAccess().markDirty(id);
        YoukaiRelations.invalidatePeacePlayers();
        return true;
    }

    private static @Nullable FactionReputationComponent reputation(UUID id, ServerLevel level) {
        ServerPlayer player = level.getServer().getPlayerList().getPlayer(id);
        if (player != null)
            return YoukaiRelations.reputation(player);
        // Player components remain keyed by UUID after logout; never award a nearby player instead.
        BasePlayerComponentManager manager = (BasePlayerComponentManager) PlayerComponentManager.serverAccess();
        var component = manager.<FactionReputationComponent>getComponent(id, ReverieDreams.id("faction_reputation"));
        return component == null ? null : component.get();
    }
}
