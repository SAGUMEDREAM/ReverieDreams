package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.misc.BaseDanmakuEntity;
import cc.thonly.reverie_dreams.registry.content.advancements.RDCriteriaTriggers;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.sound.RDSoundEvents;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class BombItem extends Item {
    public BombItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide() && world instanceof ServerLevel serverWorld) {
            ItemStack itemStack = user.getItemInHand(hand);
            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), RDSoundEvents.SPELL_CARD, user.getSoundSource(), 1.0f, 1.0f);
            List<Entity> nearbyEntities = serverWorld.getEntitiesOfClass(Entity.class, user.getBoundingBox().inflate(20), entity -> true);
            List<Entity> nearbyDanmaku = nearbyEntities.stream().filter((entity -> entity instanceof BaseDanmakuEntity danmakuEntity && danmakuEntity.getOwner() != user)).toList();
            List<Entity> sign = new ArrayList<>();
            nearbyDanmaku.forEach((entity) -> {
                BaseDanmakuEntity danmakuEntity = (BaseDanmakuEntity) entity;
                RandomSource random = world.getRandom();
                int count = random.nextIntBetweenInclusive(1, 4);
                danmakuEntity.cancelParticle(serverWorld);
                for (int i = 0; i < count; i++) {
                    serverWorld.addFreshEntity(
                            new ItemEntity(serverWorld,
                                    danmakuEntity.getX(),
                                    danmakuEntity.getY(),
                                    danmakuEntity.getZ(),
                                    new ItemStack(RDItems.POINT.value())
                            )
                    );
                }
                danmakuEntity.discard();
                sign.add(entity);
            });
            for (Entity entity : nearbyEntities) {
                if (sign.contains(entity)) {
                    continue;
                }
                if (entity.equals(user)) {
                    continue;
                }
                serverWorld.addFreshEntity(
                        new ItemEntity(serverWorld,
                                user.getX(),
                                user.getY(),
                                user.getZ(),
                                new ItemStack(RDItems.POINT.asItem())
                        )
                );
            }
            user.awardStat(Stats.ITEM_USED.get(this));
            RDCriteriaTriggers.USE_ITEM.value().trigger((ServerPlayer) user, itemStack);
            itemStack.consume(1, user);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
