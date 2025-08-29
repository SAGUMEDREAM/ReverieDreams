package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.ItemCooldownManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public class TenguCameraItem extends Item {

    public TenguCameraItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        if (!world.isClient && world instanceof ServerWorld serverWorld && user instanceof ServerPlayerEntity player) {
            var center = user.getBlockPos();
            var entities = serverWorld.getEntitiesByClass(
                    Entity.class,
                    new Box(
                            center.getX() - 16, center.getY() - 16, center.getZ() - 16,
                            center.getX() + 16, center.getY() + 16, center.getZ() + 16
                    ),
                    entity -> entity != user
            );
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 15, 0, false, false));
            player.addStatusEffect(new StatusEffectInstance(StatusEffects.NIGHT_VISION, 3 * 20, 0, false, false));
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 5 * 20, 1, false, false));
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, (int) (1.5 * 2 * 20), 0, false, false));
                    livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, (int) (1.5 * 2 * 20), 0, false, false));
                }
                if (entity instanceof DanmakuEntity danmakuEntity) {
                    serverWorld.spawnEntity(
                            new ItemEntity(serverWorld,
                                    danmakuEntity.getX(),
                                    danmakuEntity.getY(),
                                    danmakuEntity.getZ(),
                                    new ItemStack(ModItems.POINT)
                            )
                    );
                    danmakuEntity.discard();
                }
            }
            world.playSound(null, player.getBlockPos(), SoundEventInit.PHOTO, SoundCategory.PLAYERS);

            ItemCooldownManager itemCooldownManager = user.getItemCooldownManager();
            itemCooldownManager.set(itemStack, 30);

            if (!user.isInCreativeMode()) {
                itemStack.damage(1, user);
            }
            if (itemStack.isDamageable() && itemStack.getDamage() >= itemStack.getMaxDamage()) {
                itemStack.decrement(1);
            }
            return ActionResult.SUCCESS_SERVER;
        }
        return ActionResult.SUCCESS;
    }
}
