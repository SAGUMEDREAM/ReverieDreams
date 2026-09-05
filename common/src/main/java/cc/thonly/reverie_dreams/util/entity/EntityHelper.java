package cc.thonly.reverie_dreams.util.entity;

import cc.thonly.reverie_dreams.api.entity.LivingEntityDataModifier;
import cc.thonly.reverie_dreams.entity.MaidYousei;
import cc.thonly.reverie_dreams.entity.RabbitUnit;
import cc.thonly.reverie_dreams.entity.SunflowerYousei;
import cc.thonly.reverie_dreams.entity.Yousei;
import cc.thonly.reverie_dreams.mixin.accessor.LivingEntityAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class EntityHelper {

    public static void registerHostilityAllYousei(Mob mob, GoalSelector targetSelector) {
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, Yousei.class, true));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, SunflowerYousei.class, true));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, MaidYousei.class, true));
    }

    public static void registerHostilityAllRabbit(Mob mob, GoalSelector targetSelector) {
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(mob, RabbitUnit.class, true));
    }

    public static void removeDeathLevel(LivingEntity livingEntity, int level) {
        LivingEntityDataModifier modifier = LivingEntityDataModifier.getMixin(livingEntity);
        int deathLevel = modifier.reverie_dreams$getDeathLevel();
        if (deathLevel >= 1) {
            modifier.reverie_dreams$setDeathLevel(deathLevel - level);
        }
    }

    @Deprecated
    public static boolean isInTag(RegistryAccess registryManager, Entity entity, TagKey<EntityType<?>> tagKey) {
        Registry<EntityType<?>> registry = registryManager.lookupOrThrow(Registries.ENTITY_TYPE);
        Iterable<Holder<EntityType<?>>> registryEntries = registry.getTagOrEmpty(tagKey);
        for (Holder<EntityType<?>> registryEntry : registryEntries) {
            if (registryEntry.value() == entity.getType()) {
                return true;
            }
        }
        return false;
    }

    public static boolean is(Entity entity, TagKey<EntityType<?>> tagKey) {
        return entity.getType().is(tagKey);
    }

    public static void hurt(ServerLevel level, LivingEntity entity, DamageSource source, float dmg) {
        if (!entity.isInvulnerableTo(level, source)) {
            dmg = ((LivingEntityAccessor) entity).reverie_dreams$getDamageAfterArmorAbsorb(source, dmg);
            dmg = ((LivingEntityAccessor) entity).reverie_dreams$getDamageAfterMagicAbsorb(source, dmg);
            float var10 = Math.max(dmg - entity.getAbsorptionAmount(), 0.0F);
            entity.setAbsorptionAmount(entity.getAbsorptionAmount() - (dmg - var10));
            float absorbedDamage = dmg - var10;
            if (absorbedDamage > 0.0F && absorbedDamage < 3.4028235E37F && source.getEntity() instanceof ServerPlayer serverPlayer) {
                serverPlayer.awardStat(Stats.DAMAGE_DEALT_ABSORBED, Math.round(absorbedDamage * 10.0F));
            }

            if (var10 != 0.0F) {
                entity.getCombatTracker().recordDamage(source, var10);
                entity.setHealth(entity.getHealth() - var10);
                entity.setAbsorptionAmount(entity.getAbsorptionAmount() - var10);
                entity.gameEvent(GameEvent.ENTITY_DAMAGE);
            }
        }
    }
}
