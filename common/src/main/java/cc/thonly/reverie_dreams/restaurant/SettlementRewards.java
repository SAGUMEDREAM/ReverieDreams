package cc.thonly.reverie_dreams.restaurant;

import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerFactory;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import cc.thonly.reverie_dreams.util.item.ItemUtils;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Shared payout used by online players and by the NPC settlement ledger. */
public final class SettlementRewards {
    private static final double DIRECT_PAY_DISTANCE_SQR = 32.0 * 32.0;

    private SettlementRewards() {
    }

    public static void grant(Entity source, ServerPlayer player, Settlement settlement) {
        payCoins(source, player, settlement.coins());
        if (settlement.experience() > 0) {
            player.giveExperiencePoints(settlement.experience());
        }
        SimpleTriggerFactory.create(SimpleTriggerKeys.WAITER).trigger(player);
        playOutcome(player, settlement.outcome());
    }

    public static void payCoins(Entity source, ServerPlayer player, int coins) {
        if (coins <= 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        boolean fromSource = source.level() == player.level()
                && source.distanceToSqr(player) <= DIRECT_PAY_DISTANCE_SQR;
        double x = fromSource ? source.getX() : player.getX();
        double y = fromSource ? source.getEyeY() : player.getY() + player.getBbHeight() * 0.5;
        double z = fromSource ? source.getZ() : player.getZ();
        List<ItemStack> stacks = ItemUtils.calculateCoins(coins);
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemEntity entity = new ItemEntity(level, x, y, z, stack);
            if (fromSource) {
                double dx = player.getX() - x;
                double dy = player.getY() + player.getBbHeight() * 0.5 - y;
                double dz = player.getZ() - z;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                if (distance > 0.001) {
                    dx /= distance;
                    dy /= distance;
                    dz /= distance;
                }
                entity.setDeltaMovement(dx * 0.23, dy * 0.23 + 0.04, dz * 0.23);
            }
            entity.setPickUpDelay(40);
            level.addFreshEntity(entity);
        }
    }

    public static void dropCoins(Entity source, int coins) {
        if (coins <= 0 || !(source.level() instanceof ServerLevel level)) {
            return;
        }
        double x = source.getX();
        double y = source.getEyeY();
        double z = source.getZ();
        List<ItemStack> stacks = ItemUtils.calculateCoins(coins);
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            ItemEntity entity = new ItemEntity(level, x, y, z, stack);
            entity.setDeltaMovement(
                    (source.getRandom().nextDouble() - 0.5) * 0.1,
                    0.15,
                    (source.getRandom().nextDouble() - 0.5) * 0.1
            );
            entity.setPickUpDelay(40);
            level.addFreshEntity(entity);
        }
    }

    public static void playOutcome(ServerPlayer player, Settlement.Outcome outcome) {
        switch (outcome) {
            case SUCCESS -> SoundEventPlayUtils.playSound(player, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 1.0f);
            case TOO_EXPENSIVE -> SoundEventPlayUtils.playSound(player, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 1.0f);
            case ANGRY -> SoundEventPlayUtils.playSound(player, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0f, 0.65f);
        }
    }
}
