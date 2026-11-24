package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.api.entity.type.DanmakuShooter;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.server.DelayedTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Sunflower extends Item {
    private static final double MAX_TARGET_RANGE_SQR = 32.0 * 32.0;

    public Sunflower(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        Level level = player.level();
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ItemCooldowns cooldowns = player.getCooldowns();
            ItemStack stack = player.getItemInHand(hand);
            if (cooldowns.isOnCooldown(stack)) {
                return InteractionResult.FAIL;
            }
            this.spawn(player, serverLevel, target);
            DelayedTask.repeat(serverLevel.getServer(), 2, 0.75f, () -> {
                this.spawn(player, serverLevel, target);
            });
            cooldowns.addCooldown(stack, 50);
            damageItem(player, stack, hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ItemStack stack = player.getItemInHand(hand);
            ItemCooldowns cooldowns = player.getCooldowns();
            if (cooldowns.isOnCooldown(stack)) {
                return InteractionResult.FAIL;
            }
            this.spawn(player, serverLevel, null);
            DelayedTask.repeat(serverLevel.getServer(), 2, 0.75f, () -> {
                this.spawn(player, serverLevel, null);
            });
            cooldowns.addCooldown(stack, 50);
            damageItem(player, stack, hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    private static void damageItem(Player player, ItemStack stack, InteractionHand hand) {
        if (player.hasInfiniteMaterials()) {
            return;
        }
        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        stack.hurtAndBreak(1, player, slot);
    }

    private void spawn(LivingEntity entity, ServerLevel serverLevel, LivingEntity target) {
        // Entity interaction supplies an explicit target; ordinary use keeps the player's aim.
        if (target != null && (!target.isAlive() || target.level() != serverLevel
                || entity.distanceToSqr(target) > MAX_TARGET_RANGE_SQR)) {
            return;
        }
        boolean aimed = target != null;
        float[] pitchYaw = aimed
                ? aimAt(entity, target)
                : new float[]{entity.getXRot(), entity.getYRot()};
        // Aimed volleys use a tighter fan; free aim keeps a wider spread.
        float[] yawOffsets = aimed
                ? new float[]{-6.0f, -3.0f, 0.0f, 3.0f, 6.0f}
                : new float[]{-10.0f, -5.0f, 0.0f, 5.0f, 10.0f};
        for (float yawOffset : yawOffsets) {
            ItemStack shot = DanmakuTypes.random(DanmakuTypes.BUBBLE).create();
            shot.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), shot.getOrDefault(
                            RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault())
                    .withDamage(3.5f)
                    .withScale(2.5f)
                    .withHitboxScale(2.5f));
            // DanmakuShooter.spawn overrides speed from this argument; keep the value here
            // in sync rather than setting it on the component above.
            DanmakuShooter.spawn(serverLevel, entity, shot, pitchYaw[0], pitchYaw[1] + yawOffset,
                    1.8f, 1.0f, 0.15f);
        }
        DanmakuShooter.soundDefault(entity);
    }

    private static float[] aimAt(LivingEntity shooter, LivingEntity target) {
        double dx = target.getX() - shooter.getX();
        double dy = target.getY() + target.getBbHeight() * 0.5 - shooter.getEyeY();
        double dz = target.getZ() - shooter.getZ();
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horizontal));
        float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        return new float[]{pitch, yaw};
    }
}
