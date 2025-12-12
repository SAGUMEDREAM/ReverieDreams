package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public interface YoumuSwordUsing {
    default InteractionResult useItem(Level world, Player user, InteractionHand hand) {
        var pThis = (Item) this;
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            var itemStack = user.getItemInHand(hand);
            var server = serverWorld.getServer();
            var registryManager = server.registryAccess();
            var entityTypes = registryManager.lookupOrThrow(Registries.ENTITY_TYPE);

            var center = user.blockPosition();

            var entities = serverWorld.getEntitiesOfClass(
                    LivingEntity.class,
                    new AABB(
                            center.getX() - 16, center.getY() - 16, center.getZ() - 16,
                            center.getX() + 16, center.getY() + 16, center.getZ() + 16
                    ),
                    entity -> entity != user
            );

            entities.removeIf(entity ->
                    !entityTypes.wrapAsHolder(entity.getType()).is(EntityTypeTags.UNDEAD)
            );

            if (!entities.isEmpty()) {
                for (LivingEntity entity : entities) {
                    entity.addEffect(new MobEffectInstance(MobEffects.GLOWING, 15 * 20));
                }
                serverWorld.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, user.getSoundSource(), 1.0f, 1.0f);

                if (!user.hasInfiniteMaterials()) {
                    itemStack.hurtWithoutBreaking(1, user);
                }
                return InteractionResult.SUCCESS_SERVER;
            } else {
                return InteractionResult.PASS;
            }
        }
        return InteractionResult.SUCCESS;
    }
}
