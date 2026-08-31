package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

import java.util.List;

@SuppressWarnings({"SpellCheckingInspection", "resource", "DataFlowIssue", "SuspiciousNameCombination", "LombokGetterMayBeUsed"})
public class NPCFishingHook extends Projectile {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final RandomSource syncronizedRandom = RandomSource.create();
    private boolean biting;
    private int outOfWaterTime;
    private static final int MAX_OUT_OF_WATER_TIME = 10;
    private static final EntityDataAccessor<Integer> DATA_HOOKED_ENTITY = SynchedEntityData.defineId(NPCFishingHook.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_BITING = SynchedEntityData.defineId(NPCFishingHook.class, EntityDataSerializers.BOOLEAN);
    private int life;
    private int nibble;
    private int timeUntilLured;
    private int timeUntilHooked;
    private float fishAngle;
    private boolean openWater = true;
    private @Nullable Entity hookedIn;
    private NPCFishingHook.FishHookState currentState = NPCFishingHook.FishHookState.FLYING;
    private final int luck;
    private final int lureSpeed;
    private final InterpolationHandler interpolationHandler = new InterpolationHandler(this);

    private NPCFishingHook(EntityType<? extends NPCFishingHook> type, Level level, int luck, int lureSpeed) {
        super(type, level);
        this.luck = Math.max(0, luck);
        this.lureSpeed = Math.max(0, lureSpeed);
    }

    public NPCFishingHook(EntityType<? extends NPCFishingHook> type, Level level) {
        this(type, level, 0, 0);
    }

    @SuppressWarnings("unchecked")
    public NPCFishingHook(BaseNPCLikeEntity npc, Level level, int luck, int lureSpeed) {
        this((EntityType<? extends NPCFishingHook>) (Object) RDEntityTypes.FISHING_BOBBER.get(), level, luck, lureSpeed);
        this.setOwner(npc);
        float xRot1 = npc.getXRot();
        float yRot1 = npc.getYRot();
        float yCos = Mth.cos(-yRot1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float ySin = Mth.sin(-yRot1 * (float) (Math.PI / 180.0) - (float) Math.PI);
        float xCos = -Mth.cos(-xRot1 * (float) (Math.PI / 180.0));
        float xSin = Mth.sin(-xRot1 * (float) (Math.PI / 180.0));
        double x1 = npc.getX() - ySin * 0.3;
        double y1 = npc.getEyeY();
        double z1 = npc.getZ() - yCos * 0.3;
        this.snapTo(x1, y1, z1, yRot1, xRot1);
        Vec3 newMovement = new Vec3(-ySin, Mth.clamp(-(xSin / xCos), -5.0F, 5.0F), -yCos);
        double dist = newMovement.length();
        newMovement = newMovement.multiply(
                0.6 / dist + this.random.triangle(0.5, 0.0103365),
                0.6 / dist + this.random.triangle(0.5, 0.0103365),
                0.6 / dist + this.random.triangle(0.5, 0.0103365)
        );
        this.setDeltaMovement(newMovement);
        this.setYRot((float) (Mth.atan2(newMovement.x, newMovement.z) * 180.0F / (float) Math.PI));
        this.setXRot((float) (Mth.atan2(newMovement.y, newMovement.horizontalDistance()) * 180.0F / (float) Math.PI));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public boolean isBiting() {
        return biting;
    }

    @Override
    public InterpolationHandler getInterpolation() {
        return this.interpolationHandler;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        entityData.define(DATA_HOOKED_ENTITY, 0);
        entityData.define(DATA_BITING, false);
    }

    @Override
    protected boolean shouldBounceOnWorldBorder() {
        return true;
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        if (DATA_HOOKED_ENTITY.equals(accessor)) {
            int id = this.getEntityData().get(DATA_HOOKED_ENTITY);
            this.hookedIn = id > 0 ? this.level().getEntity(id - 1) : null;
        }

        if (DATA_BITING.equals(accessor)) {
            this.biting = this.getEntityData().get(DATA_BITING);
            if (this.biting) {
                this.setDeltaMovement(this.getDeltaMovement().x, -0.4F * Mth.nextFloat(this.syncronizedRandom, 0.6F, 1.0F), this.getDeltaMovement().z);
            }
        }

        super.onSyncedDataUpdated(accessor);
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double size = 64.0;
        return distance < 4096.0;
    }

    @Override
    public void tick() {
        this.syncronizedRandom.setSeed(this.getUUID().getLeastSignificantBits() ^ this.level().getGameTime());
        this.getInterpolation().interpolate();
        super.tick();
        BaseNPCLikeEntity owner = this.getEntityOwner();
        if (owner == null) {
            this.discard();
        } else if (this.level().isClientSide() || !this.shouldStopFishing(owner)) {
            if (this.onGround()) {
                this.life++;
                if (this.life >= 1200) {
                    this.discard();
                    return;
                }
            } else {
                this.life = 0;
            }

            float liquidHeight = 0.0F;
            BlockPos blockPos = this.blockPosition();
            FluidState fluidState = this.level().getFluidState(blockPos);
            if (fluidState.is(FluidTags.WATER)) {
                liquidHeight = fluidState.getHeight(this.level(), blockPos);
            }

            boolean isInWater = liquidHeight > 0.0F;
            if (this.currentState == FishHookState.FLYING) {
                if (this.hookedIn != null) {
                    this.setDeltaMovement(Vec3.ZERO);
                    this.currentState = FishHookState.HOOKED_IN_ENTITY;
                    return;
                }

                if (isInWater) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(0.3, 0.2, 0.3));
                    this.currentState = FishHookState.BOBBING;
                    return;
                }

                this.checkCollision();
            } else {
                if (this.currentState == FishHookState.HOOKED_IN_ENTITY) {
                    if (this.hookedIn != null) {
                        if (!this.hookedIn.isRemoved() && this.hookedIn.canInteractWithLevel() && this.hookedIn.level().dimension() == this.level().dimension()
                        ) {
                            this.setPos(this.hookedIn.getX(), this.hookedIn.getY(0.8), this.hookedIn.getZ());
                        } else {
                            this.setHookedEntity(null);
                            this.currentState = FishHookState.FLYING;
                        }
                    }

                    return;
                }

                if (this.currentState == FishHookState.BOBBING) {
                    Vec3 movement = this.getDeltaMovement();
                    double force = this.getY() + movement.y - blockPos.getY() - liquidHeight;
                    if (Math.abs(force) < 0.01) {
                        force += Math.signum(force) * 0.1;
                    }

                    this.setDeltaMovement(movement.x * 0.9, movement.y - force * this.random.nextFloat() * 0.2, movement.z * 0.9);
                    if (this.nibble <= 0 && this.timeUntilHooked <= 0) {
                        this.openWater = true;
                    } else {
                        this.openWater = this.openWater && this.outOfWaterTime < 10 && this.calculateOpenWater(blockPos);
                    }

                    if (isInWater) {
                        this.outOfWaterTime = Math.max(0, this.outOfWaterTime - 1);
                        if (this.biting) {
                            this.setDeltaMovement(
                                    this.getDeltaMovement().add(0.0, -0.1 * this.syncronizedRandom.nextFloat() * this.syncronizedRandom.nextFloat(), 0.0)
                            );
                        }

                        if (!this.level().isClientSide()) {
                            this.catchingFish(blockPos);
                        }
                    } else {
                        this.outOfWaterTime = Math.min(10, this.outOfWaterTime + 1);
                    }
                }
            }

            if (!fluidState.is(FluidTags.WATER) && !this.onGround() && this.hookedIn == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.03, 0.0));
            }

            this.move(MoverType.SELF, this.getDeltaMovement());
            this.applyEffectsFromBlocks();
            this.updateRotation();
            if (this.currentState == FishHookState.FLYING && (this.onGround() || this.horizontalCollision)) {
                this.setDeltaMovement(Vec3.ZERO);
            }

            double inertia = 0.92;
            this.setDeltaMovement(this.getDeltaMovement().scale(0.92));
            this.reapplyPosition();
        }
    }

    private boolean shouldStopFishing(BaseNPCLikeEntity owner) {
        if (owner.canInteractWithLevel()) {
            ItemStack selectedItem = owner.getMainHandItem();
            ItemStack selectedItemOffHand = owner.getOffhandItem();
            boolean mainHandIsFishing = selectedItem.is(item -> item.value() instanceof FishingRodItem);
            boolean offHandIsFishing = selectedItemOffHand.is(item -> item.value() instanceof FishingRodItem);
            if ((mainHandIsFishing || offHandIsFishing) && this.distanceToSqr(owner) <= 1024.0) {
                return false;
            }
        }

        this.discard();
        return true;
    }

    private void checkCollision() {
        HitResult hitResult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        this.hitTargetOrDeflectSelf(hitResult);
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        if (entity instanceof BaseNPCLikeEntity npc) {
            return false;
        }
        return super.canHitEntity(entity) || entity.isAlive() && entity instanceof ItemEntity;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (!this.level().isClientSide()) {
            this.setHookedEntity(hitResult.getEntity());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        this.setDeltaMovement(this.getDeltaMovement().normalize().scale(hitResult.distanceTo(this)));
    }

    private void setHookedEntity(@Nullable Entity hookedIn) {
        if (hookedIn instanceof BaseNPCLikeEntity) {
            return;
        }
        this.hookedIn = hookedIn;
        this.getEntityData().set(DATA_HOOKED_ENTITY, hookedIn == null ? 0 : hookedIn.getId() + 1);
    }

    private void catchingFish(BlockPos blockPos) {
        ServerLevel serverLevel = (ServerLevel) this.level();
        int fishingSpeed = 1;
        BlockPos above = blockPos.above();
        if (this.random.nextFloat() < 0.25F && this.level().isRainingAt(above)) {
            fishingSpeed++;
        }

        if (this.random.nextFloat() < 0.5F && !this.level().canSeeSky(above)) {
            fishingSpeed--;
        }

        if (this.nibble > 0) {
            this.nibble--;
            if (this.nibble <= 0) {
                this.timeUntilLured = 0;
                this.timeUntilHooked = 0;
                this.getEntityData().set(DATA_BITING, false);
            }
        } else if (this.timeUntilHooked > 0) {
            this.timeUntilHooked -= fishingSpeed;
            if (this.timeUntilHooked > 0) {
                this.fishAngle = this.fishAngle + (float) this.random.triangle(0.0, 9.188);
                float angle = this.fishAngle * (float) (Math.PI / 180.0);
                float angleSin = Mth.sin(angle);
                float angleCos = Mth.cos(angle);
                double fishX = this.getX() + angleSin * this.timeUntilHooked * 0.1F;
                double fishY = Mth.floor(this.getY()) + 1.0F;
                double fishZ = this.getZ() + angleCos * this.timeUntilHooked * 0.1F;
                BlockState splashBlockState = serverLevel.getBlockState(BlockPos.containing(fishX, fishY - 1.0, fishZ));
                if (splashBlockState.is(Blocks.WATER)) {
                    if (this.random.nextFloat() < 0.15F) {
                        serverLevel.sendParticles(ParticleTypes.BUBBLE, fishX, fishY - 0.1F, fishZ, 1, angleSin, 0.1, angleCos, 0.0);
                    }

                    float particleXMovement = angleSin * 0.04F;
                    float particleZMovement = angleCos * 0.04F;
                    serverLevel.sendParticles(ParticleTypes.FISHING, fishX, fishY, fishZ, 0, particleZMovement, 0.01, -particleXMovement, 1.0);
                    serverLevel.sendParticles(ParticleTypes.FISHING, fishX, fishY, fishZ, 0, -particleZMovement, 0.01, particleXMovement, 1.0);
                }
            } else {
                this.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (this.random.nextFloat() - this.random.nextFloat()) * 0.4F);
                double y = this.getY() + 0.5;
                serverLevel.sendParticles(
                        ParticleTypes.BUBBLE, this.getX(), y, this.getZ(), (int) (1.0F + this.getBbWidth() * 20.0F), this.getBbWidth(), 0.0, this.getBbWidth(), 0.2F
                );
                serverLevel.sendParticles(
                        ParticleTypes.FISHING,
                        this.getX(),
                        y,
                        this.getZ(),
                        (int) (1.0F + this.getBbWidth() * 20.0F),
                        this.getBbWidth(),
                        0.0,
                        this.getBbWidth(),
                        0.2F
                );
                this.nibble = Mth.nextInt(this.random, 20, 40);
                this.getEntityData().set(DATA_BITING, true);
            }
        } else if (this.timeUntilLured > 0) {
            this.timeUntilLured -= fishingSpeed;
            float teaseChance = 0.15F;
            if (this.timeUntilLured < 20) {
                teaseChance += (20 - this.timeUntilLured) * 0.05F;
            } else if (this.timeUntilLured < 40) {
                teaseChance += (40 - this.timeUntilLured) * 0.02F;
            } else if (this.timeUntilLured < 60) {
                teaseChance += (60 - this.timeUntilLured) * 0.01F;
            }

            if (this.random.nextFloat() < teaseChance) {
                float angle = Mth.nextFloat(this.random, 0.0F, 360.0F) * (float) (Math.PI / 180.0);
                float dist = Mth.nextFloat(this.random, 25.0F, 60.0F);
                double fishX = this.getX() + Mth.sin(angle) * dist * 0.1;
                double fishY = Mth.floor(this.getY()) + 1.0F;
                double fishZ = this.getZ() + Mth.cos(angle) * dist * 0.1;
                BlockState splashBlockState = serverLevel.getBlockState(BlockPos.containing(fishX, fishY - 1.0, fishZ));
                if (splashBlockState.is(Blocks.WATER)) {
                    serverLevel.sendParticles(ParticleTypes.SPLASH, fishX, fishY, fishZ, 2 + this.random.nextInt(2), 0.1F, 0.0, 0.1F, 0.0);
                }
            }

            if (this.timeUntilLured <= 0) {
                this.fishAngle = Mth.nextFloat(this.random, 0.0F, 360.0F);
                this.timeUntilHooked = Mth.nextInt(this.random, 20, 80);
            }
        } else {
            this.timeUntilLured = Mth.nextInt(this.random, 100, 600);
            this.timeUntilLured = this.timeUntilLured - this.lureSpeed;
        }
    }

    private boolean calculateOpenWater(BlockPos blockPos) {
        OpenWaterType previousLayer = OpenWaterType.INVALID;

        for (int y = -1; y <= 2; y++) {
            OpenWaterType layer = this.getOpenWaterTypeForArea(blockPos.offset(-2, y, -2), blockPos.offset(2, y, 2));
            switch (layer) {
                case ABOVE_WATER:
                    if (previousLayer == OpenWaterType.INVALID) {
                        return false;
                    }
                    break;
                case INSIDE_WATER:
                    if (previousLayer == OpenWaterType.ABOVE_WATER) {
                        return false;
                    }
                    break;
                case INVALID:
                    return false;
            }

            previousLayer = layer;
        }

        return true;
    }

    private OpenWaterType getOpenWaterTypeForArea(BlockPos from, BlockPos to) {
        return BlockPos.betweenClosedStream(from, to)
                       .map(this::getOpenWaterTypeForBlock)
                       .reduce((a, b) -> a == b ? a : OpenWaterType.INVALID)
                       .orElse(OpenWaterType.INVALID);
    }

    private OpenWaterType getOpenWaterTypeForBlock(BlockPos pos) {
        BlockState state = this.level().getBlockState(pos);
        if (!state.isAir() && !state.is(Blocks.LILY_PAD)) {
            FluidState fluidState = state.getFluidState();
            return fluidState.is(FluidTags.WATER) && fluidState.isSource() && state.getCollisionShape(this.level(), pos).isEmpty()
                    ? OpenWaterType.INSIDE_WATER
                    : OpenWaterType.INVALID;
        } else {
            return OpenWaterType.ABOVE_WATER;
        }
    }

    public boolean isOpenWaterFishing() {
        return this.openWater;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    public int retrieve(ItemStack rod) {
        BaseNPCLikeEntity owner = this.getEntityOwner();
        if (!this.level().isClientSide() && owner != null && !this.shouldStopFishing(owner)) {
            int dmg = 0;
            if (this.hookedIn != null) {
                this.pullEntity(this.hookedIn);
                this.level().broadcastEntityEvent(this, (byte) 31);
                dmg = this.hookedIn instanceof ItemEntity ? 3 : 5;
            } else if (this.nibble > 0) {
                LootParams params = new LootParams.Builder((ServerLevel) this.level())
                        .withParameter(LootContextParams.ORIGIN, this.position())
                        .withParameter(LootContextParams.TOOL, rod)
                        .withParameter(LootContextParams.THIS_ENTITY, this)
                        .withLuck(this.luck + owner.getLuck())
                        .create(LootContextParamSets.FISHING);
                LootTable lootTable = this.level().getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
                List<ItemStack> items = lootTable.getRandomItems(params);
                for (ItemStack itemStack : items) {
                    ItemEntity entity = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), itemStack);
                    double xa = owner.getX() - this.getX();
                    double ya = owner.getY() - this.getY();
                    double za = owner.getZ() - this.getZ();
                    double speed = 0.1;
                    entity.setDeltaMovement(xa * 0.1, ya * 0.1 + Math.sqrt(Math.sqrt(xa * xa + ya * ya + za * za)) * 0.08, za * 0.1);
                    this.level().addFreshEntity(entity);
                    owner.level()
                         .addFreshEntity(new ExperienceOrb(owner.level(), owner.getX(), owner.getY() + 0.5, owner.getZ() + 0.5, this.random.nextInt(6) + 1));
//                    if (itemStack.is(ItemTags.FISHES)) {
//                        owner.awardStat(Stats.FISH_CAUGHT, 1);
//                    }
                }

                dmg = 1;
            }

            if (this.onGround()) {
                dmg = 2;
            }

            this.discard();
            return dmg;
        } else {
            return 0;
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 31 && this.level().isClientSide() && this.hookedIn instanceof Player player && player.isLocalPlayer()) {
            this.pullEntity(this.hookedIn);
        }

        super.handleEntityEvent(id);
    }

    protected void pullEntity(Entity entity) {
        Entity owner = this.getOwner();
        if (owner != null) {
            Vec3 delta = new Vec3(owner.getX() - this.getX(), owner.getY() - this.getY(), owner.getZ() - this.getZ()).scale(0.1);
            entity.setDeltaMovement(entity.getDeltaMovement().add(delta));
        }
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        this.updateOwnerInfo(null);
        super.remove(reason);
    }

    @Override
    public void onClientRemoval() {
        this.updateOwnerInfo(null);
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        this.updateOwnerInfo(this);
    }

    private void updateOwnerInfo(@Nullable NPCFishingHook hook) {
        BaseNPCLikeEntity owner = this.getEntityOwner();
        if (owner != null) {
            owner.fishing = hook;
        }
    }

    public @Nullable BaseNPCLikeEntity getEntityOwner() {
        return this.getOwner() instanceof BaseNPCLikeEntity npc ? npc : null;
    }

    public @Nullable Entity getHookedIn() {
        return this.hookedIn;
    }

    @Override
    public boolean canUsePortal(boolean ignorePassenger) {
        return false;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        Entity owner = this.getOwner();
        return new ClientboundAddEntityPacket(this, serverEntity, owner == null ? this.getId() : owner.getId());
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        if (this.getEntityOwner() == null) {
//            int ownerId = packet.getData();
//            LOGGER.error("Failed to recreate fishing hook on client. {} (id: {}) is not a valid owner.", this.level().getEntity(ownerId), ownerId);
            this.discard();
        }
    }

    private enum FishHookState {
        FLYING,
        HOOKED_IN_ENTITY,
        BOBBING;
    }

    private enum OpenWaterType {
        ABOVE_WATER,
        INSIDE_WATER,
        INVALID;
    }
}
