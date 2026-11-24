package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.api.registry.tag.ConventionalEntityTypeTags;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 限时诅咒诱饵。
 *
 * <p>只把“正在追击召唤者”的怪物拉向自己，不会永久改写周围所有生物的目标；
 * 15 秒后或被击破时进行一次不破坏地形的退魔爆发。</p>
 */
public class CursedDecoyEntity extends LivingEntity {
    public static final int LIFETIME_TICKS = 15 * 20;
    private static final int TAUNT_INTERVAL_TICKS = 10;
    private static final double TAUNT_RADIUS = 16.0;
    private static final double BURST_RADIUS = 4.0;
    private static final float BURST_DAMAGE = 6.0F;

    @Nullable
    private UUID ownerUUID;
    private int age;
    private boolean detonated;

    public CursedDecoyEntity(EntityType<? extends CursedDecoyEntity> type, Level level) {
        super(type, level);
        this.setCustomName(Component.translatable(this.getType().getDescriptionId()));
        this.setCustomNameVisible(false);
    }

    public CursedDecoyEntity(ServerLevel level, ServerPlayer owner) {
        this(RDEntityTypes.CURSED_DECOY.value(), level);
        this.ownerUUID = owner.getUUID();
    }

    @Override
    public HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }

    public void setOwner(ServerPlayer owner) {
        this.ownerUUID = owner.getUUID();
    }

    public boolean isOwnedBy(Player player) {
        return this.ownerUUID != null && this.ownerUUID.equals(player.getUUID());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        this.age++;
        if (this.age % TAUNT_INTERVAL_TICKS == 0) {
            this.tauntNearbyMobs();
            this.spawnAmbientParticles();
        }
        if (this.age >= LIFETIME_TICKS) {
            this.detonate();
        }
    }

    private void tauntNearbyMobs() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ServerPlayer owner = this.getOwner(serverLevel);
        if (owner == null) {
            return;
        }

        AABB area = this.getBoundingBox().inflate(TAUNT_RADIUS);
        for (Mob mob : serverLevel.getEntitiesOfClass(Mob.class, area, this::canBeTaunted)) {
            if (mob.getTarget() != owner && mob.getLastHurtByMob() != owner) {
                continue;
            }
            if (!mob.getSensing().hasLineOfSight(this) || !mob.canAttack(this)) {
                continue;
            }
            mob.setTarget(this);
            mob.setLastHurtByMob(this);
        }
    }

    private boolean canBeTaunted(Mob mob) {
        if (!mob.isAlive()) {
            return false;
        }
        if (mob instanceof TamableAnimal tameable && tameable.isTame()) {
            return false;
        }
        return !isBoss(mob);
    }

    private void spawnAmbientParticles() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(
                ParticleTypes.SOUL,
                this.getX(), this.getY() + 0.9, this.getZ(),
                3, 0.35, 0.55, 0.35, 0.01
        );
    }

    /**
     * 自动引爆或被击破时调用。只伤害附近敌对目标，不改变方块。
     */
    public void detonate() {
        if (this.detonated) {
            return;
        }
        this.detonated = true;

        if (!(this.level() instanceof ServerLevel serverLevel)) {
            this.discard();
            return;
        }

        ServerPlayer owner = this.getOwner(serverLevel);
        DamageSource source = owner != null
                ? this.damageSources().indirectMagic(owner, owner)
                : this.damageSources().magic();
        AABB area = this.getBoundingBox().inflate(BURST_RADIUS);

        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area, this::isBurstTarget)) {
            target.hurtServer(serverLevel, source, BURST_DAMAGE);
            if (target.isAlive()) {
                target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0));
                target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0));
            }
        }

        serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                this.getX(), this.getY() + 0.9, this.getZ(),
                32, 0.6, 0.7, 0.6, 0.03
        );
        serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                this.getX(), this.getY() + 0.9, this.getZ(),
                24, 0.6, 0.7, 0.6, 0.05
        );
        serverLevel.playSound(
                null, this.blockPosition(), SoundEvents.SOUL_ESCAPE.value(),
                SoundSource.PLAYERS, 1.0F, 0.85F
        );
        this.discard();
    }

    private boolean isBurstTarget(LivingEntity target) {
        if (target == this || !target.isAlive() || target instanceof Player) {
            return false;
        }
        if (target instanceof TamableAnimal tameable && tameable.isTame()) {
            return false;
        }
        if (!(target instanceof Mob mob) || isBoss(mob)) {
            return false;
        }
        return mob instanceof Enemy
                || mob.getTarget() == this
                || mob.getLastHurtByMob() == this;
    }

    private static boolean isBoss(Mob mob) {
        return mob.getType().builtInRegistryHolder().is(ConventionalEntityTypeTags.BOSSES);
    }

    @Nullable
    private ServerPlayer getOwner(ServerLevel level) {
        if (this.ownerUUID == null) {
            return null;
        }
        MinecraftServer server = level.getServer();
        return server == null ? null : server.getPlayerList().getPlayer(this.ownerUUID);
    }

    @Override
    public void die(DamageSource source) {
        this.detonate();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() {
    }
}