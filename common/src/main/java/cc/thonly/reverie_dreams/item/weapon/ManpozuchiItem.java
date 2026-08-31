package cc.thonly.reverie_dreams.item.weapon;

import cc.thonly.reverie_dreams.api.entity.LivingEntityDataModifier;
import cc.thonly.reverie_dreams.item.base.PickaxeItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ManpozuchiItem extends PickaxeItem {
    private static final int ATTACK_DAMAGE_MODIFIER_VALUE = 5;
    private static final float ATTACK_SPEED_MODIFIER_VALUE = -3.4f;
    public static final float MINING_SPEED_MULTIPLIER = 1.5f;
    private static final float HEAVY_SMASH_SOUND_FALL_DISTANCE_THRESHOLD = 5.0f;
    public static final float KNOCKBACK_RANGE = 3.5f;
    private static final float KNOCKBACK_POWER = 0.7f;

    public static final ToolMaterial MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 59, 2.0F, 0.0F, 15, ItemTags.GOLD_TOOL_MATERIALS);

    public ManpozuchiItem(float attackDamage, float attackSpeed, Properties settings) {
        super(MATERIAL, attackDamage, attackSpeed, settings);
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level world, BlockPos pos, LivingEntity user) {
        if (!world.isClientSide() && user instanceof ServerPlayer player) {
            boolean b = super.canDestroyBlock(stack, state, world, pos, user);
            return b && !player.hasInfiniteMaterials();
        }
        return super.canDestroyBlock(stack, state, world, pos, user);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity entity, InteractionHand hand) {
        boolean isSneaking = user.isShiftKeyDown();
        Level world = user.level();
        if (!world.isClientSide() && isSneaking && user instanceof ServerPlayer player) {
            ItemStack stackInHand = player.getItemInHand(hand);
            if (stackInHand.getDamageValue() >= stackInHand.getMaxDamage()) {
                return InteractionResult.PASS;
            }
            AttributeMap attributes = entity.getAttributes();
            AttributeInstance attributeInstance = attributes.getInstance(Attributes.SCALE);
            if (attributeInstance == null) {
                return InteractionResult.PASS;
            }
            LivingEntityDataModifier lePlayerImpl = (LivingEntityDataModifier) entity;
            double state = lePlayerImpl.reverie_dreams$getManpozuchiUsingState();
            if (state >= 0.2) {
                attributeInstance.setBaseValue(state);
                lePlayerImpl.reverie_dreams$setManpozuchiUsingState(state - 0.1);
            } else {
                attributeInstance.setBaseValue(1.0);
                lePlayerImpl.reverie_dreams$setManpozuchiUsingState(1.0);
            }
            if (!user.hasInfiniteMaterials()) {
                stackInHand.hurtWithoutBreaking(1, user);
            }
            user.swing(hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        boolean isSneaking = user.isShiftKeyDown();
        if (!world.isClientSide() && isSneaking && user instanceof ServerPlayer player) {
            ItemStack stackInHand = user.getItemInHand(hand);
            if (stackInHand.getDamageValue() >= stackInHand.getMaxDamage()) {
                return InteractionResult.PASS;
            }
            AttributeMap attributes = player.getAttributes();
            AttributeInstance attributeInstance = attributes.getInstance(Attributes.SCALE);
            if (attributeInstance == null) {
                return InteractionResult.PASS;
            }
            LivingEntityDataModifier lePlayerImpl = (LivingEntityDataModifier) player;
            double state = lePlayerImpl.reverie_dreams$getManpozuchiUsingState();
            if (state >= 0.2) {
                attributeInstance.setBaseValue(state);
                lePlayerImpl.reverie_dreams$setManpozuchiUsingState(state - 0.1);
            } else {
                attributeInstance.setBaseValue(1.0);
                lePlayerImpl.reverie_dreams$setManpozuchiUsingState(1.0);
            }
            if (!user.hasInfiniteMaterials()) {
                stackInHand.hurtWithoutBreaking(1, user);
            }
            user.swing(hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        return super.use(world, user, hand);
    }

    public void hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (shouldDealAdditionalDamage(attacker)) {
            ServerLevel serverWorld = (ServerLevel) attacker.level();
            attacker.setDeltaMovement(attacker.getDeltaMovement().with(Direction.Axis.Y, 0.009999999776482582));

            ServerPlayer serverPlayer = null;
            if (attacker instanceof ServerPlayer) {
                serverPlayer = (ServerPlayer) attacker;
                serverPlayer.currentImpulseImpactPos = this.getCurrentExplosionImpactPos(serverPlayer);
                serverPlayer.setIgnoreFallDamageFromCurrentImpulse(true, Vec3.ZERO);
                serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
            }

            if (target.onGround()) {
                if (attacker instanceof ServerPlayer) {
                    serverPlayer.setSpawnExtraParticlesOnFall(true);
                }

                SoundEvent soundEvent = attacker.fallDistance > 5.0 ? SoundEvents.MACE_SMASH_GROUND_HEAVY : SoundEvents.MACE_SMASH_GROUND;
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), soundEvent, attacker.getSoundSource(), 1.0F, 1.0F);
            } else {
                serverWorld.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.MACE_SMASH_AIR, attacker.getSoundSource(), 1.0F, 1.0F);
            }

            knockbackNearbyEntities(serverWorld, attacker, target);
        }

    }

    private Vec3 getCurrentExplosionImpactPos(ServerPlayer player) {
        return player.isIgnoringFallDamageFromCurrentImpulse() && player.currentImpulseImpactPos != null && player.currentImpulseImpactPos.y <= player.position().y ? player.currentImpulseImpactPos : player.position();
    }

    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (shouldDealAdditionalDamage(attacker)) {
            attacker.resetFallDistance();
        }

    }

    public float getAttackDamageBonus(Entity target, float baseAttackDamage, DamageSource damageSource) {
        Entity var5 = damageSource.getDirectEntity();
        if (var5 instanceof LivingEntity livingEntity) {
            if (!shouldDealAdditionalDamage(livingEntity)) {
                return 0.0F;
            } else {
                double d = 3.0;
                double e = 8.0;
                double f = livingEntity.fallDistance;
                double g;
                if (f <= 3.0) {
                    g = 4.0 * f;
                } else if (f <= 8.0) {
                    g = 12.0 + 2.0 * (f - 3.0);
                } else {
                    g = 22.0 + f - 8.0;
                }

                Level var14 = livingEntity.level();
                if (var14 instanceof ServerLevel) {
                    ServerLevel serverWorld = (ServerLevel) var14;
                    return (float) (g + (double) EnchantmentHelper.modifyFallBasedDamage(serverWorld, livingEntity.getWeaponItem(), target, damageSource, 0.0F) * f);
                } else {
                    return (float) g;
                }
            }
        } else {
            return 0.0F;
        }
    }

    private static void knockbackNearbyEntities(Level world, Entity attacker, Entity attacked) {
        world.levelEvent(2013, attacked.getOnPos(), 750);
        world.getEntitiesOfClass(LivingEntity.class, attacked.getBoundingBox().inflate(3.5), getKnockbackPredicate(attacker, attacked)).forEach((entity) -> {
            Vec3 vec3d = entity.position().subtract(attacked.position());
            double d = getKnockback(attacker, entity, vec3d);
            Vec3 vec3d2 = vec3d.normalize().scale(d);
            if (d > 0.0) {
                entity.push(vec3d2.x, 0.699999988079071, vec3d2.z);
                if (entity instanceof ServerPlayer) {
                    ServerPlayer serverPlayerEntity = (ServerPlayer) entity;
                    serverPlayerEntity.connection.send(new ClientboundSetEntityMotionPacket(serverPlayerEntity));
                }
            }

        });
    }

    private static Predicate<LivingEntity> getKnockbackPredicate(Entity attacker, Entity attacked) {
        return (entity) -> {
            boolean bl;
            boolean bl2;
            boolean bl3;
            boolean var10000;
            label64:
            {
                bl = !entity.isSpectator();
                bl2 = entity != attacker && entity != attacked;
                bl3 = !attacker.isAlliedTo(entity);
                if (entity instanceof TamableAnimal tameableEntity) {
                    if (attacked instanceof LivingEntity livingEntity) {
                        if (tameableEntity.isTame() && tameableEntity.isOwnedBy(livingEntity)) {
                            var10000 = true;
                            break label64;
                        }
                    }
                }

                var10000 = false;
            }

            boolean bl4;
            label56:
            {
                bl4 = !var10000;
                if (entity instanceof ArmorStand armorStandEntity) {
                    if (armorStandEntity.isMarker()) {
                        var10000 = false;
                        break label56;
                    }
                }

                var10000 = true;
            }

            boolean bl5 = var10000;
            boolean bl6 = attacked.distanceToSqr(entity) <= Math.pow(3.5, 2.0);
            return bl && bl2 && bl3 && bl4 && bl5 && bl6;
        };
    }

    private static double getKnockback(Entity attacker, LivingEntity attacked, Vec3 distance) {
        return (3.5 - distance.length()) * 0.699999988079071 * (double) (attacker.fallDistance > 5.0 ? 2 : 1) * (1.0 - attacked.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
    }

    public static boolean shouldDealAdditionalDamage(LivingEntity attacker) {
        return attacker.fallDistance > 1.5 && !attacker.isFallFlying();
    }

    @SuppressWarnings("deprecation")
    @Nullable
    public DamageSource getItemDamageSource(LivingEntity user) {
        return shouldDealAdditionalDamage(user) ? user.damageSources().mace(user) : super.getItemDamageSource(user);
    }

}
