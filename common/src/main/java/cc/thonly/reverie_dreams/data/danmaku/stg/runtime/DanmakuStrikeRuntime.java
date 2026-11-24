package cc.thonly.reverie_dreams.data.danmaku.stg.runtime;

import cc.thonly.reverie_dreams.data.danmaku.stg.DanmakuStrike;
import cc.thonly.reverie_dreams.api.entity.type.FriendlyFaction;
import cc.thonly.reverie_dreams.registry.content.RDDamageTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Timed melee volumes; no extra entities or client collision authority.
 */
final class DanmakuStrikeRuntime {
    private final List<DanmakuStrike> strikes;
    private final Map<Integer, Set<UUID>> hits = new HashMap<>();
    private final Set<Integer> shown = new HashSet<>();

    DanmakuStrikeRuntime(List<DanmakuStrike> strikes) {
        this.strikes = strikes;
    }

    void reset() {
        this.hits.clear();
        this.shown.clear();
    }

    void fire(int tick, DanmakuPatternRuntime runtime, SpellCastOrigin origin) {
        ServerLevel world = runtime.getWorld();
        Entity owner = runtime.getCaster();
        DamageSource damageSource = null;
        for (DanmakuStrike strike : this.strikes) {
            if (!strike.activeAt(tick)) {
                continue;
            }
            var volume = strike.volume(origin.bodyPosition(), origin.pitch(), origin.yaw(), origin.bodyScale());
            if (this.shown.add(strike.hitGroup())) {
                var center = volume.center();
                world.sendParticles(ParticleTypes.SWEEP_ATTACK, center.x, center.y, center.z, 1, 0, 0, 0, 0);
            }
            Set<UUID> groupHits = this.hits.computeIfAbsent(strike.hitGroup(), ignored -> new HashSet<>());
            for (LivingEntity target : world.getEntitiesOfClass(LivingEntity.class, volume.bounds(),
                    entity -> entity.isAlive() && !entity.isSpectator() && entity != owner
                            && !groupHits.contains(entity.getUUID())
                            && !(FriendlyFaction.isFriendLy(entity, owner)
                            && !FriendlyFaction.isDirectTarget(owner, entity)))) {
                if (!volume.intersects(target.getBoundingBox())) {
                    continue;
                }
                // Minecraft terrain blocks a melee attack; the source game's arena has no blocks.
                if (world.clip(new ClipContext(origin.bodyPosition(), target.getBoundingBox().getCenter(),
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                        owner == null ? CollisionContext.empty() : CollisionContext.of(owner))).getType() != HitResult.Type.MISS) {
                    continue;
                }
                groupHits.add(target.getUUID());
                if (damageSource == null) {
                    damageSource = RDDamageTypes.create(world.registryAccess(),
                            owner instanceof Player ? DamageTypes.PLAYER_ATTACK : DamageTypes.MOB_ATTACK, owner);
                }
                target.hurt(damageSource, strike.damage());
            }
        }
    }
}
