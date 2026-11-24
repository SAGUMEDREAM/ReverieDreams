package cc.thonly.reverie_dreams.entity.misc;

import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import cc.thonly.reverie_dreams.world.dream.DreamSleepAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Relative;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * A short-lived rift that sends living entities to a recorded destination.
 *
 * <p>Chunk tickets are deliberately placed here:
 * <ul>
 *     <li>The rift keeps its own chunk loaded while it exists, so it keeps ticking after
 *     the player walks away.</li>
 *     <li>Before a teleport, both the old position and the destination get a portal
 *     ticket. This prevents a far-away mob from landing in an unloaded chunk, and keeps
 *     the player's old chunk alive long enough for the teleport to finish.</li>
 * </ul>
 */
public class GapPortalEntity extends Entity {
    public static final int LIFETIME = 30 * 20;
    /**
     * Ticks this rift stays open, synced to the client: the renderer fades the rift
     * out against its own lifetime, so a longer window (the boundary ofuda asks for
     * three minutes) does not make the rift visually vanish after the default 30s.
     */
    public static final EntityDataAccessor<Integer> DATA_LIFETIME =
            SynchedEntityData.defineId(GapPortalEntity.class, EntityDataSerializers.INT);
    private static final int TELEPORT_GRACE_TICKS = 10;
    private static final int CHUNK_REFRESH_TICKS = 10 * 20;
    /** Travellers land this many blocks away from the rift they came out of (1..3). */
    private static final int LANDING_RADIUS = 3;

    private int age;
    private ResourceKey<Level> targetDimension = Level.OVERWORLD;
    private BlockPos targetPos = BlockPos.ZERO;
    private Component targetName = Component.empty();
    private boolean dreamReturn;

    public GapPortalEntity(EntityType<? extends GapPortalEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public GapPortalEntity(ServerLevel level, ResourceKey<Level> targetDimension, BlockPos targetPos, String targetName) {
        this(level, targetDimension, targetPos, Component.literal(targetName));
    }

    public GapPortalEntity(ServerLevel level, ResourceKey<Level> targetDimension, BlockPos targetPos, Component targetName) {
        this(RDEntityTypes.GAP_PORTAL.value(), level);
        this.targetDimension = targetDimension;
        this.targetPos = targetPos;
        this.targetName = targetName;
    }

    public static GapPortalEntity dreamReturn(ServerLevel level) {
        GapPortalEntity portal = new GapPortalEntity(level, RDBuiltinLevels.DREAM_WORLD, BlockPos.ZERO,
                Component.translatable("message.reverie_dreams.dream_spool.destination"));
        portal.dreamReturn = true;
        portal.setLifetime(8 * 20);
        return portal;
    }

    public void setLifetime(int lifetime) {
        this.entityData.set(DATA_LIFETIME, Math.max(1, lifetime));
    }

    public int getLifetime() {
        return this.entityData.get(DATA_LIFETIME);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_LIFETIME, LIFETIME);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }

        ServerLevel level = (ServerLevel) this.level();
        this.age++;

        if (this.age == 1) {
            keepChunksLoaded(level, this.blockPosition(), 3);
            this.spawnOpenEffects(level);
        }
        if (this.age % CHUNK_REFRESH_TICKS == 0) {
            keepChunksLoaded(level, this.blockPosition(), 3);
        }
        if (this.age % 2 == 0) {
            this.spawnSwirlParticles(level);
        }
        if (this.age % 60 == 0) {
            level.playSound(null, this.blockPosition(), SoundEvents.PORTAL_AMBIENT,
                    SoundSource.BLOCKS, 0.28f, 0.75f + this.random.nextFloat() * 0.1f);
        }

        if (this.age >= this.getLifetime()) {
            this.spawnCloseEffects(level);
            this.discard();
            return;
        }

        if (this.age >= TELEPORT_GRACE_TICKS && (!this.dreamReturn || this.age % 5 == 0)) {
            this.teleportOccupants(level);
        }
    }

    private void teleportOccupants(ServerLevel sourceLevel) {
        List<Entity> entities = sourceLevel.getEntities(
                this,
                this.getBoundingBox().inflate(0.2),
                entity -> entity instanceof LivingEntity
                        && entity != this
                        && entity.isAlive()
                        && (!this.dreamReturn || entity instanceof ServerPlayer)
                        && !entity.isPassenger()
                        && !entity.isOnPortalCooldown()
        );

        for (Entity entity : entities) {
            this.teleportEntity(sourceLevel, entity);
        }
    }

    private void teleportEntity(ServerLevel sourceLevel, Entity entity) {
        if (this.targetDimension == null) {
            return;
        }
        var server = sourceLevel.getServer();
        if (server == null) {
            return;
        }
        ServerLevel targetLevel = server.getLevel(this.targetDimension);
        if (targetLevel == null) {
            if (entity instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("dialog.gap.no_target_world"), false);
            }
            return;
        }

        BlockPos destination = this.targetPos;
        if (this.dreamReturn) {
            destination = ((DreamSleepAnchor) entity).reverie_dreams$getDreamSleepAnchor();
            if (destination == null) {
                if (this.age % 20 == 0 && entity instanceof ServerPlayer player) {
                    player.sendSystemMessage(Component.translatable("message.reverie_dreams.dream_spool.no_bed"), true);
                }
                return;
            }
        }

        // Load both ends before touching the destination blocks. The old chunk is
        // especially important for players, whose source chunk would otherwise be
        // unloaded as soon as they leave.
        keepChunksLoaded(sourceLevel, this.blockPosition(), 3);
        keepChunksLoaded(targetLevel, destination, 2);
        targetLevel.getChunkAt(destination);

        BlockPos safe = findSafePosition(targetLevel, entity, destination, this.dreamReturn);
        if (safe == null) {
            if (this.dreamReturn && this.age % 20 == 0 && entity instanceof ServerPlayer player) {
                player.sendSystemMessage(Component.translatable("message.reverie_dreams.dream_spool.blocked"), true);
            }
            return;
        }

        double x = safe.getX() + 0.5;
        double y = safe.getY();
        double z = safe.getZ() + 0.5;
        boolean teleported;
        if (entity instanceof ServerPlayer player) {
            teleported = player.teleportTo(
                    targetLevel, x, y, z,
                    EnumSet.noneOf(Relative.class),
                    player.getYRot(), player.getXRot(), true
            );
        } else {
            teleported = entity.teleportTo(
                    targetLevel, x, y, z,
                    EnumSet.noneOf(Relative.class),
                    entity.getYRot(), entity.getXRot(), true
            );
        }

        if (!teleported) {
            return;
        }
        if (this.dreamReturn) entity.fallDistance = 0;

        entity.setPortalCooldown(100);
        sourceLevel.sendParticles(ParticleTypes.PORTAL, this.getX(), this.getY() + 1.0, this.getZ(),
                30, 0.55, 0.9, 0.55, 0.08);
        targetLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y + 1.0, z,
                30, 0.55, 0.9, 0.55, 0.08);
        targetLevel.playSound(null, safe, SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.3f, 0.8f);
        sourceLevel.playSound(null, this.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.PLAYERS, 0.255f, 1.2f);

        if (entity instanceof ServerPlayer player) {
            player.sendSystemMessage(Component.translatable("dialog.gap.arrived", this.targetName), false);
        }
    }

    /**
     * Finds a landing spot <em>around</em> the far end instead of on top of it.
     *
     * <p>Landing exactly on the destination rift used to put the traveller inside
     * that rift's own trigger box, so it teleported them straight back and the two
     * ends kept flinging them between dimensions. The search here walks the square
     * rings at 1..3 blocks and rejects any spot that would touch a rift (or a block),
     * then picks one at random so people do not always pop out in the same corner.</p>
     */
    private static BlockPos findSafePosition(ServerLevel level, Entity entity, BlockPos base, boolean needsFloor) {
        double halfWidth = entity.getBbWidth() / 2.0;
        // Rifts within reach: the destination rift itself must be avoided, and any
        // other rift nearby would bounce the traveller back just the same.
        List<GapPortalEntity> rifts = level.getEntitiesOfClass(
                GapPortalEntity.class, new AABB(base).inflate(LANDING_RADIUS + 1.0));

        for (int radius = 1; radius <= LANDING_RADIUS; radius++) {
            List<BlockPos> spots = new ArrayList<>();
            for (BlockPos offset : ringOffsets(radius)) {
                for (int dy = 0; dy <= 2; dy++) {
                    BlockPos pos = base.offset(offset).above(dy);
                    if (needsFloor && (!level.isLoaded(pos)
                            || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)
                            || !level.getFluidState(pos).isEmpty())) continue;
                    if (isClearLanding(level, entity, pos, halfWidth, rifts)) {
                        spots.add(pos);
                        break;
                    }
                }
            }
            if (!spots.isEmpty()) {
                return spots.get(entity.getRandom().nextInt(spots.size()));
            }
        }

        if (needsFloor) return null;
        // Nearly blocked cave: stand on the rift itself, as the absolute last resort.
        for (int dy = 0; dy <= 2; dy++) {
            BlockPos pos = base.above(dy);
            if (level.noCollision(entity, entityBox(entity, pos, halfWidth))) {
                return pos;
            }
        }
        return null;
    }

    private static boolean isClearLanding(ServerLevel level, Entity entity, BlockPos pos,
                                          double halfWidth, List<GapPortalEntity> rifts) {
        AABB box = entityBox(entity, pos, halfWidth);
        if (!level.noCollision(entity, box)) {
            return false;
        }
        for (GapPortalEntity rift : rifts) {
            if (rift.getBoundingBox().inflate(0.2).intersects(box)) {
                return false;
            }
        }
        return true;
    }

    private static AABB entityBox(Entity entity, BlockPos pos, double halfWidth) {
        return new AABB(
                pos.getX() + 0.5 - halfWidth,
                pos.getY(),
                pos.getZ() + 0.5 - halfWidth,
                pos.getX() + 0.5 + halfWidth,
                pos.getY() + entity.getBbHeight(),
                pos.getZ() + 0.5 + halfWidth
        );
    }

    /** Square (Chebyshev) ring of offsets exactly {@code radius} blocks away. */
    private static List<BlockPos> ringOffsets(int radius) {
        List<BlockPos> offsets = new ArrayList<>();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (Math.max(Math.abs(dx), Math.abs(dz)) == radius) {
                    offsets.add(new BlockPos(dx, 0, dz));
                }
            }
        }
        return offsets;
    }

    private static void keepChunksLoaded(ServerLevel level, BlockPos pos, int radius) {
        level.getChunkSource().addTicketWithRadius(TicketType.PORTAL, ChunkPos.containing(pos), radius);
    }

    private void spawnOpenEffects(ServerLevel level) {
        level.sendParticles(ParticleTypes.REVERSE_PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                24, 0.6, 0.9, 0.6, 0.05);
        level.sendParticles(ParticleTypes.END_ROD,
                this.getX(), this.getY() + 1.2, this.getZ(),
                10, 0.4, 0.9, 0.4, 0.02);
        level.playSound(null, this.blockPosition(), SoundEvents.PORTAL_TRIGGER,
                SoundSource.BLOCKS, 0.25f, 0.65f);
    }

    private void spawnCloseEffects(ServerLevel level) {
        level.sendParticles(ParticleTypes.PORTAL,
                this.getX(), this.getY() + 1.0, this.getZ(),
                24, 0.6, 0.9, 0.6, 0.06);
        level.playSound(null, this.blockPosition(), SoundEvents.PORTAL_TRAVEL,
                SoundSource.BLOCKS, 0.25f, 1.35f);
    }

    private void spawnSwirlParticles(ServerLevel level) {
        double baseAngle = this.age * 0.18;
        for (int i = 0; i < 3; i++) {
            double angle = baseAngle + i * (Math.PI * 2.0 / 3.0);
            double x = this.getX() + Math.cos(angle) * 0.55;
            double y = this.getY() + 1.0 + Math.sin(angle * 1.7) * 0.8;
            double z = this.getZ() + Math.sin(angle) * 0.55;
            level.sendParticles(
                    i == 1 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL,
                    x, y, z, 1, 0.0, 0.0, 0.0, 0.01
            );
        }
        if (this.age % 10 == 0) {
            level.sendParticles(ParticleTypes.END_ROD,
                    this.getX(), this.getY() + 1.2, this.getZ(),
                    1, 0.45, 0.8, 0.45, 0.01);
        }
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith(Entity entity) {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 128.0 * 128.0;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }
}
