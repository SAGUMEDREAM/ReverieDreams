package cc.thonly.reverie_dreams.item.prop;

import cc.thonly.reverie_dreams.entity.misc.DanmakuEntity;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.sound.SoundEventInit;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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

public class BombItem extends Item {
    public BombItem(Properties settings) {
        super(settings);
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        if (!world.isClientSide && world instanceof ServerLevel serverWorld) {
            ItemStack itemStack = user.getItemInHand(hand);
            world.playSound(null, user.getX(), user.getEyeY(), user.getZ(), SoundEventInit.SPELL_CARD, user.getSoundSource(), 1.0f, 1.0f);
            List<Entity> nearbyEntities = serverWorld.getEntitiesOfClass(Entity.class, user.getBoundingBox().inflate(20), entity -> true);
            List<Entity> nearbyDanmaku = nearbyEntities.stream().filter((entity -> entity instanceof DanmakuEntity danmakuEntity && danmakuEntity.getOwner() != user)).toList();
            List<Entity> sign = new ArrayList<>();
            ItemParticleOption ispe = new ItemParticleOption(ParticleTypes.ITEM, ModItems.BOMB_FRAGMENT.getDefaultInstance());
            serverWorld.sendParticles(
                    ispe,
                    user.getX(),
                    user.getY(),
                    user.getZ(),
                    1,
                    0.25,
                    0.5,
                    0.25,
                    0.25
            );
            nearbyDanmaku.forEach((entity) -> {
                DanmakuEntity danmakuEntity = (DanmakuEntity) entity;
                Vec3 pos = danmakuEntity.position();
                int particleCount = (int) 8;
                double radius = danmakuEntity.getBbWidth() / 2 + 0.5;
                double heightOffset = danmakuEntity.getBbHeight();
                RandomSource random = world.getRandom();
                int count = random.nextIntBetweenInclusive(1, 4);
                for (int i = 0; i < count; i++) {
                    serverWorld.addFreshEntity(
                            new ItemEntity(serverWorld,
                                    danmakuEntity.getX(),
                                    danmakuEntity.getY(),
                                    danmakuEntity.getZ(),
                                    new ItemStack(ModItems.POINT)
                            )
                    );
                }
                for (int i = 0; i < particleCount; i++) {
                    double angle = (2 * Math.PI / particleCount) * i;
                    double xOffset = radius * Math.cos(angle);
                    double zOffset = radius * Math.sin(angle);

                    ItemParticleOption itemStackParticleEffect = new ItemParticleOption(ParticleTypes.ITEM, ModItems.POINT.getDefaultInstance());
                    serverWorld.sendParticles(
                            itemStackParticleEffect,
                            pos.x,
                            pos.y,
                            pos.z,
                            1,
                            xOffset,
                            (heightOffset / particleCount) * i,
                            zOffset,
                            0.25
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
                                new ItemStack(ModItems.POINT)
                        )
                );
            }
            user.awardStat(Stats.ITEM_USED.get(this));
            itemStack.consume(1, user);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }
}
