package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.misc.CursedDecoyEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

import java.util.List;

public class CursedDecoyDollItem extends Item {
    public static final int COOLDOWN_TICKS = 20 * 20;
    private static final int OWNER_RANGE = 64;

    public CursedDecoyDollItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(context.getLevel() instanceof ServerLevel serverWorld)
                || !(context.getPlayer() instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }

        InteractionHand hand = context.getHand();
        ItemStack stack = serverPlayer.getItemInHand(hand);
        if (serverPlayer.getCooldowns().isOnCooldown(stack)) {
            return InteractionResult.FAIL;
        }

        List<CursedDecoyEntity> existing = serverWorld.getEntitiesOfClass(
                CursedDecoyEntity.class,
                serverPlayer.getBoundingBox().inflate(OWNER_RANGE),
                decoy -> decoy.isOwnedBy(serverPlayer)
        );
        if (!existing.isEmpty()) {
            return InteractionResult.FAIL;
        }

        BlockPos pos = context.getClickedPos().above();
        if (!serverWorld.getBlockState(pos).canBeReplaced()
                || !serverWorld.getBlockState(pos.above()).canBeReplaced()) {
            serverPlayer.sendSystemMessage(
                    Component.translatable("message.reverie_dreams.cursed_decoy.no_space"), false);
            return InteractionResult.FAIL;
        }

        CursedDecoyEntity decoy = new CursedDecoyEntity(serverWorld, serverPlayer);
        decoy.setPos(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
        decoy.setYRot(serverPlayer.getYRot());
        decoy.setXRot(0.0F);
        serverWorld.addFreshEntity(decoy);

        // 短暂脱离仇恨：隐身先结束，速度稍长一点，方便重新选位。
        serverPlayer.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, 8 * 20, 0, false, false, true));
        serverPlayer.addEffect(new MobEffectInstance(
                MobEffects.SPEED, 10 * 20, 0, false, true, true));

        serverPlayer.getCooldowns().addCooldown(stack, COOLDOWN_TICKS);
        stack.consume(1, serverPlayer);
        serverPlayer.swing(hand);

        serverWorld.sendParticles(
                ParticleTypes.SOUL,
                pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D,
                16, 0.4D, 0.5D, 0.4D, 0.02D
        );
        serverWorld.playSound(
                null, pos, SoundEvents.SOUL_ESCAPE.value(),
                SoundSource.PLAYERS, 0.8F, 0.9F
        );
        return InteractionResult.SUCCESS_SERVER;
    }
}