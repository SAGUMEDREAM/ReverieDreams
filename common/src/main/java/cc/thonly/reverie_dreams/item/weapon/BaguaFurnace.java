package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.entity.misc.BaguaFurnaceEntity;
import cc.thonly.reverie_dreams.entity.projectile.BaguaFireball;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class BaguaFurnace extends Item {
    public static final int NORMAL_COOLDOWN_TICKS = 20;
    public static final int DEPLOY_COOLDOWN_TICKS = 20 * 40;
    public static final int DEPLOY_DURABILITY_COST = 15;

    public BaguaFurnace(Properties settings) {
        super(settings);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity user) {
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            ServerLevel serverWorld = (ServerLevel) world;
            BaguaFurnaceEntity baguaFurnaceEntity = new BaguaFurnaceEntity(serverWorld, user);
            if (!serverWorld.addFreshEntity(baguaFurnaceEntity))
                return stack;

            serverWorld.sendParticles(
                    ParticleTypes.FLAME,
                    player.getX(), player.getEyeY(), player.getZ(),
                    20, 0.35D, 0.35D, 0.35D, 0.03D
            );
            serverWorld.playSound(
                    null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                    SoundSource.PLAYERS, 0.8F, 1.0F
            );

            player.getCooldowns().addCooldown(stack, DEPLOY_COOLDOWN_TICKS);
            if (!player.hasInfiniteMaterials()) {
                stack.hurtAndBreak(DEPLOY_DURABILITY_COST, player, user.getUsedItemHand() == InteractionHand.OFF_HAND
                        ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
            }
        }
        return stack;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return this.use(context.getLevel(), context.getPlayer(), context.getHand());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 40;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(world instanceof ServerLevel serverWorld) || !(user instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }

        ItemStack itemStack = player.getItemInHand(hand);
        if (player.getCooldowns().isOnCooldown(itemStack)) {
            return InteractionResult.FAIL;
        }

        if (player.isShiftKeyDown()) {
            player.startUsingItem(hand);
            return InteractionResult.CONSUME;
        }

        BaguaFireball fireball = new BaguaFireball(serverWorld, player);
        fireball.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.5F, 1.0F);
        serverWorld.addFreshEntity(fireball);

        serverWorld.sendParticles(
                ParticleTypes.FLAME,
                player.getX(), player.getEyeY(), player.getZ(),
                8, 0.12D, 0.12D, 0.12D, 0.02D
        );
        serverWorld.playSound(
                null, player.blockPosition(), SoundEvents.BLAZE_SHOOT,
                SoundSource.PLAYERS, 0.6F, 1.0F
        );

        player.getCooldowns().addCooldown(itemStack, NORMAL_COOLDOWN_TICKS);
        if (!player.hasInfiniteMaterials()) {
            itemStack.hurtAndBreak(1, player, hand == InteractionHand.OFF_HAND
                    ? EquipmentSlot.OFFHAND
                    : EquipmentSlot.MAINHAND);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS_SERVER;
    }
}
