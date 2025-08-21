package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.sound.SoundEventInit;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

public interface YoumuSwordUsing {
    default ActionResult useItem(World world, PlayerEntity user, Hand hand) {
        var pThis = (Item) this;
        if (!world.isClient && world instanceof ServerWorld serverWorld) {
            var itemStack = user.getStackInHand(hand);
            var server = serverWorld.getServer();
            var registryManager = server.getRegistryManager();
            var entityTypes = registryManager.getOrThrow(RegistryKeys.ENTITY_TYPE);

            var center = user.getBlockPos();

            var entities = serverWorld.getEntitiesByClass(
                    LivingEntity.class,
                    new Box(
                            center.getX() - 16, center.getY() - 16, center.getZ() - 16,
                            center.getX() + 16, center.getY() + 16, center.getZ() + 16
                    ),
                    entity -> entity != user
            );

            entities.removeIf(entity ->
                    !entityTypes.getEntry(entity.getType()).isIn(EntityTypeTags.UNDEAD)
            );

            if (!entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    entity.addStatusEffect(new StatusEffectInstance(StatusEffects.GLOWING, 15 * 20));
                }
                serverWorld.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, user.getSoundCategory(), 1.0f, 1.0f);

                if (!user.isInCreativeMode()) {
                    itemStack.damage(1, user);
                }
                return ActionResult.SUCCESS_SERVER;
            } else {
                return ActionResult.PASS;
            }
        }
        return ActionResult.SUCCESS;
    }
}
