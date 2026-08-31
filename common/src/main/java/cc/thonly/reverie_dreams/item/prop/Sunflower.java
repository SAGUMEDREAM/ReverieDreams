package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.api.entity.type.DanmakuShooter;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.server.DelayedTask;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class Sunflower extends Item {
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
            this.spawn(player, serverLevel);
            DelayedTask.repeat(serverLevel.getServer(), 2, 0.8f, () -> {
                this.spawn(player, serverLevel);
            });
            cooldowns.addCooldown(stack, 60);
            Integer cur = itemStack.get(DataComponents.DAMAGE);
            Integer max = itemStack.get(DataComponents.MAX_DAMAGE);
            if (cur != null && max != null && cur >= max) {
                itemStack.setCount(0);
            }
            itemStack.hurtWithoutBreaking(1, player);
            if (itemStack.isDamageableItem() && itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide() && level instanceof ServerLevel serverLevel) {
            ItemStack stack = player.getItemInHand(hand);
            this.spawn(player, serverLevel);
            DelayedTask.repeat(serverLevel.getServer(), 2, 0.8f, () -> {
                this.spawn(player, serverLevel);
            });
            ItemCooldowns cooldowns = player.getCooldowns();
            cooldowns.addCooldown(stack, 60);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    private void spawn(LivingEntity entity, ServerLevel serverLevel) {
        float[] pitchYaw = new float[]{entity.getXRot(), entity.getYRot()};
        ItemStack a = DanmakuTypes.random(DanmakuTypes.BUBBLE).create();
        ItemStack b = DanmakuTypes.random(DanmakuTypes.BUBBLE).create();
        ItemStack c = DanmakuTypes.random(DanmakuTypes.BUBBLE).create();
        a.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), a.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault())
                                                                .withSpeed(2.3f));
        b.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), b.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault())
                                                                .withSpeed(2.3f));
        c.set(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), c.getOrDefault(RDDataComponentTypes.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault())
                                                                .withSpeed(2.3f));
        DanmakuShooter.spawn(serverLevel, entity, a, pitchYaw[0], pitchYaw[1] - 15.0f, 0.5f, 5.0f, 0.2f);
        DanmakuShooter.spawn(serverLevel, entity, b, pitchYaw[0], pitchYaw[1], 0.5f, 5.0f, 0.2f);
        DanmakuShooter.spawn(serverLevel, entity, c, pitchYaw[0], pitchYaw[1] + 15.0f, 0.5f, 5.0f, 0.2f);
        DanmakuShooter.soundDefault(entity);
    }
}
