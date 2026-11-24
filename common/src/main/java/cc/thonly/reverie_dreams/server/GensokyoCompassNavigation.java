package cc.thonly.reverie_dreams.server;

import cc.thonly.reverie_dreams.data.gensokyo.GensokyoLandmark;
import cc.thonly.reverie_dreams.item.prop.GensokyoCompassItem;
import cc.thonly.reverie_dreams.registry.content.GensokyoLandmarks;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Server-driven boss bar guidance for the Gensokyo compass.
 *
 * <p>The bar exists only while the player is in Gensokyo and holds a compass
 * with a valid target, so switching dimensions or dropping the item hides it
 * without any extra packets from the client.</p>
 */
public final class GensokyoCompassNavigation {
    private static final Map<UUID, ServerBossEvent> BARS = new Object2ObjectOpenHashMap<>();
    private static final String[] ARROWS = {"↑", "↗", "→", "↘", "↓", "↙", "←", "↖"};
    private static final double FULL_PROGRESS_DISTANCE = 4096.0D;

    private GensokyoCompassNavigation() {
    }

    public static void tick(Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!player.level().dimension().equals(RDBuiltinLevels.GENSOKYO)) {
            hide(player.getUUID());
            return;
        }

        ItemStack stack = findCompass(serverPlayer);
        Identifier targetId = stack.isEmpty()
                ? null
                : stack.get(RDDataComponentTypes.GENSOKYO_COMPASS_TARGET.value());
        Optional<GensokyoLandmark> target = GensokyoLandmarks.get(targetId);
        if (target.isEmpty()) {
            hide(player.getUUID());
            return;
        }

        GensokyoLandmark landmark = target.get();
        double distance = landmark.horizontalDistance(player.getX(), player.getZ());
        float progress = (float) Math.max(0.0D, Math.min(1.0D, 1.0D - distance / FULL_PROGRESS_DISTANCE));
        progress = Math.round(progress * 100.0F) / 100.0F;

        ServerBossEvent bar = BARS.get(player.getUUID());
        if (bar == null) {
            bar = new ServerBossEvent(player.getUUID(), Component.empty(),
                    BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
            BARS.put(player.getUUID(), bar);
            bar.setVisible(true);
        } else if (!bar.getPlayers().contains(serverPlayer)) {
            // Respawn creates a new ServerPlayer instance for the same UUID.
            bar.removeAllPlayers();
        }
        bar.addPlayer(serverPlayer);
        bar.setName(Component.translatable(
                "bossbar.reverie_dreams.gensokyo_compass",
                landmark.displayName(),
                arrowTo(serverPlayer, landmark),
                Math.round(distance)
        ));
        bar.setProgress(progress);
    }

    public static void remove(Player player) {
        if (player != null) {
            hide(player.getUUID());
        }
    }

    public static void clear(MinecraftServer server) {
        for (ServerBossEvent bar : BARS.values()) {
            bar.setVisible(false);
            bar.removeAllPlayers();
        }
        BARS.clear();
    }

    private static void hide(UUID id) {
        ServerBossEvent bar = BARS.remove(id);
        if (bar == null) {
            return;
        }
        bar.setVisible(false);
        bar.removeAllPlayers();
    }

    private static ItemStack findCompass(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof GensokyoCompassItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    private static String arrowTo(ServerPlayer player, GensokyoLandmark landmark) {
        double dx = landmark.x() + 0.5D - player.getX();
        double dz = landmark.z() + 0.5D - player.getZ();
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        float relative = Mth.wrapDegrees((float) (targetYaw - player.getYRot()));
        int index = (Math.round(relative / 45.0F) & 7);
        return ARROWS[index];
    }
}
