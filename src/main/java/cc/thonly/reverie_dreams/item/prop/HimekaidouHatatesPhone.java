package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.polymer.item.IBasicPolymerItem;
import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public class HimekaidouHatatesPhone extends Item implements IBasicPolymerItem {
    public HimekaidouHatatesPhone(Properties properties) {
        super(properties);
    }


    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemStack = user.getItemInHand(hand);
        if (!world.isClientSide && world instanceof ServerLevel serverWorld && user instanceof ServerPlayer player) {
            var center = user.blockPosition();
            var entities = serverWorld.getEntitiesOfClass(
                    Entity.class,
                    new AABB(
                            center.getX() - 16, center.getY() - 16, center.getZ() - 16,
                            center.getX() + 16, center.getY() + 16, center.getZ() + 16
                    ),
                    entity -> entity != user
            );
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 15, 0, false, false));
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 3 * 20, 0, false, false));
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 5 * 20, 1, false, false));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) (1.5 * 2 * 20), 0, false, false));
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, (int) (1.5 * 2 * 20), 0, false, false));
                }
                if (entity instanceof DanmakuEntity danmakuEntity) {
                    serverWorld.addFreshEntity(
                            new ItemEntity(serverWorld,
                                    danmakuEntity.getX(),
                                    danmakuEntity.getY(),
                                    danmakuEntity.getZ(),
                                    new ItemStack(RDItems.POINT)
                            )
                    );
                    danmakuEntity.discard();
                }
            }
            world.playSound(null, player.blockPosition(), SoundEventInit.PHOTO, SoundSource.PLAYERS);

            ItemCooldowns itemCooldownManager = user.getCooldowns();
            itemCooldownManager.addCooldown(itemStack, 30);

            if (!user.hasInfiniteMaterials()) {
                itemStack.hurtWithoutBreaking(1, user);
            }
            if (itemStack.isDamageableItem() && itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                itemStack.shrink(1);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
