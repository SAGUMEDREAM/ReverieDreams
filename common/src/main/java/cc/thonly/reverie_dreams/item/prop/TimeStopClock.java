package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.server.DelayedTask;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerTickRateManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TimeStopClock extends Item {
    private static final int FREEZE_TICKS = 20 * 5;
    private static final int COOLDOWN_TICKS = 20 * 30;

    @Nullable
    public static DelayedTask TASK = null;
    @Nullable
    public static UUID ACTIVE_OWNER = null;

    public TimeStopClock(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        MinecraftServer server = world.getServer();
        if (world.isClientSide() || server == null) {
            return InteractionResult.PASS;
        }

        ServerTickRateManager tickManager = server.tickRateManager();
        if (tickManager.isFrozen()) {
            // 只允许开启者提前解除，避免多人服务器互相抢时停。
            if (ACTIVE_OWNER != null && ACTIVE_OWNER.equals(user.getUUID())) {
                release(server);
                return InteractionResult.SUCCESS_SERVER;
            }
            user.sendSystemMessage(Component.translatable("message.reverie_dreams.time_stop.already_frozen"));
            return InteractionResult.FAIL;
        }

        // 全局时停会影响所有玩家；仅单人单人游玩或管理员可用，避免 LAN 访客绕过权限。
        boolean singleplayerHost = server.isSingleplayer() && server.getPlayerList().getPlayerCount() <= 1;
        boolean hasPermission = singleplayerHost
                || (user instanceof ServerPlayer serverPlayer
                && serverPlayer.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER));
        if (!hasPermission && !ReverieDreams.config().enableTimeStoppingMultiplayer) {
            user.sendSystemMessage(Component.translatable("message.reverie_dreams.time_stop.no_permission"));
            return InteractionResult.FAIL;
        }

        ItemStack itemStack = user.getItemInHand(hand);
        ItemCooldowns itemCooldownManager = user.getCooldowns();
        if (itemCooldownManager.isOnCooldown(itemStack)) {
            return InteractionResult.FAIL;
        }
        itemCooldownManager.addCooldown(itemStack, COOLDOWN_TICKS);

        tickManager.setFrozen(true);
        ACTIVE_OWNER = user.getUUID();
        world.playSound(null, user.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.5f, 1.0f);

        if (!user.hasInfiniteMaterials()) {
            itemStack.hurtWithoutBreaking(1, user);
            if (itemStack.isDamageableItem() && itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                itemStack.shrink(1);
            }
        }

        if (TASK != null) {
            TASK.stop();
        }
        TASK = DelayedTask.create(server, FREEZE_TICKS, () -> release(server));
        return InteractionResult.SUCCESS_SERVER;
    }

    /**
     * 解除本物品造成的时停；服务器停止、玩家退出或计时结束时调用。
     */
    public static void release(@Nullable MinecraftServer server) {
        reset();
        if (server != null && server.tickRateManager().isFrozen()) {
            server.tickRateManager().setFrozen(false);
        }
    }

    /**
     * 只清理跨服务器/跨存档的静态任务，不改变当前 tick 状态。
     */
    public static void reset() {
        ACTIVE_OWNER = null;
        if (TASK != null) {
            TASK.stop();
            TASK = null;
        }
    }

    public static void releaseIfOwner(Player player) {
        if (ACTIVE_OWNER == null || !ACTIVE_OWNER.equals(player.getUUID())) {
            return;
        }
        MinecraftServer server = player.level().getServer();
        release(server);
    }
}
