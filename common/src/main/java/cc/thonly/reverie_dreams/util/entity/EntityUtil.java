package cc.thonly.reverie_dreams.util.entity;

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
import net.minecraft.world.level.gameevent.GameEvent;

public class EntityUtil {
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

    public static void hurt(ServerLevel level, LivingEntity entity, DamageSource source, float dmg) {
        if (!entity.isInvulnerableTo(level, source)) {
            dmg = ((LivingEntityAccessor)entity).reverie_dreams$getDamageAfterArmorAbsorb(source, dmg);
            dmg = ((LivingEntityAccessor)entity).reverie_dreams$getDamageAfterMagicAbsorb(source, dmg);
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
