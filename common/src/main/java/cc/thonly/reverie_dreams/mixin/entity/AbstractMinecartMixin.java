package cc.thonly.reverie_dreams.mixin.entity;

import cc.thonly.reverie_dreams.api.entity.CartSignal;
import cc.thonly.reverie_dreams.api.entity.LockedCart;
import cc.thonly.reverie_dreams.block.redstone.RailControllerBlock;
import cc.thonly.reverie_dreams.block.redstone.SignalRailBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.Ticket;
import net.minecraft.server.level.TicketType;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractMinecart.class)
public abstract class AbstractMinecartMixin extends VehicleEntity implements LockedCart, CartSignal {

    @Shadow
    public static boolean useExperimentalMovement(Level level) {
        return false;
    }

    @Unique
    private boolean reverie_dreams$lockedCart = false;

    @Unique
    private Vec3 reverie_dreams$lockedSpeed = null;

    @Unique
    private ChunkPos reverie_dreams$chunkPos, reverie_dreams$oldChunkPos;

    @Unique
    private int reverie_dreams$minecartId;

    @Unique
    private final Ticket reverie_dreams$ticket = new Ticket(TicketType.FORCED, 1200);

    @Unique
    private String reverie_dreams$signalName = "";

    public AbstractMinecartMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void reverie_dreams$lockCart() {
        this.reverie_dreams$lockedCart = true;
        this.reverie_dreams$lockedSpeed = new Vec3(this.getDeltaMovement().toVector3f());
    }

    @Override
    public void reverie_dreams$releaseCart() {
        this.reverie_dreams$lockedCart = false;
        this.setDeltaMovement(this.reverie_dreams$lockedSpeed);
        this.reverie_dreams$lockedSpeed = null;
    }

    public boolean reverie_dreams$isCartLocked() {
        return this.reverie_dreams$lockedCart;
    }

//    @Inject(method = "pushOtherMinecart", at = @At("HEAD"), cancellable = true)
//    public void cancelPushIfLocked(AbstractMinecart other, double d, double e, CallbackInfo ci) {
//        ci.cancel();
//    }

    @Inject(method = "getCurrentBlockPosOrRailBelow", at = @At("RETURN"), cancellable = true)
    private void modifyGetRailOrMinecartPos(CallbackInfoReturnable<BlockPos> cir) {
        Level level = this.level();
        if (useExperimentalMovement(level())) {
            BlockPos pos = cir.getReturnValue();
            BlockPos above = pos.above();
            if (
                    !level.getBlockState(pos).is(BlockTags.RAILS) &&
                            level.getBlockState(above).is(BlockTags.RAILS)
            ) {
                cir.setReturnValue(above);
            }
        }
    }

    @Inject(method = "tick", at = @At("RETURN"))
    public void onCartMove(CallbackInfo ci) {
        Level level = this.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        AbstractMinecart cart = (AbstractMinecart) (Object) this;

        this.reverie_dreams$oldChunkPos = this.reverie_dreams$chunkPos;
        this.reverie_dreams$chunkPos = cart.chunkPosition();
        if (this.reverie_dreams$oldChunkPos == null) {
            this.reverie_dreams$oldChunkPos = this.reverie_dreams$chunkPos;
        }

        if (serverLevel.getPlayers(p -> p.level() == serverLevel).isEmpty()) {
            serverLevel.resetEmptyTime();
        }

        if (!cart.hasExactlyOnePlayerPassenger() && !this.reverie_dreams$oldChunkPos.equals(this.reverie_dreams$chunkPos)) {
            serverLevel.setChunkForced(this.reverie_dreams$oldChunkPos.x, this.reverie_dreams$oldChunkPos.z, false);
            serverLevel.setChunkForced(this.reverie_dreams$chunkPos.x, this.reverie_dreams$chunkPos.z, true);
        }
    }


    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void saveCartData(ValueOutput view, CallbackInfo ci) {
        view.storeNullable("LockedSpeed", Vec3.CODEC, this.reverie_dreams$lockedSpeed);
        view.putBoolean("LockedCart", this.reverie_dreams$lockedCart);
        if (this.reverie_dreams$signalName != null && !this.reverie_dreams$signalName.isEmpty()) {
            view.putString("SignalName", this.reverie_dreams$signalName);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readCartData(ValueInput view, CallbackInfo ci) {
        this.reverie_dreams$signalName = view.getStringOr("SignalName", "");
        this.reverie_dreams$lockedCart = view.getBooleanOr("LockedCart", false);
        view.read("LockedSpeed", Vec3.CODEC).ifPresent(vec3 -> {
            this.reverie_dreams$lockedSpeed = vec3;
        });
    }

    @Inject(method = "push", at = @At("HEAD"), cancellable = true)
    public void cartPush(Entity entity, CallbackInfo ci) {
        Level level = this.level();
        if (!level.isClientSide()) {
            BlockPos pos = this.getOnPos();
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof RailControllerBlock) {
                boolean occupied = state.getValue(RailControllerBlock.OCCUPIED);
                if (!occupied) {
                    this.setDeltaMovement(0, this.getDeltaMovement().y, 0);
//                    this.hasImpulse = true;
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "createMinecart", at = @At(value = "RETURN"))
    private static <T extends AbstractMinecart> void createMinecartForController(
            Level level, double d, double e, double f, EntityType<T> entityType, EntitySpawnReason entitySpawnReason, ItemStack itemStack, @Nullable Player player, CallbackInfoReturnable<T> cir
    ) {
        T cart = cir.getReturnValue();
        if (cart == null) return;
        if (level.isClientSide()) {
            return;
        }
        BlockPos pos = BlockPos.containing(d, e, f);
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof RailControllerBlock || state.getBlock() instanceof SignalRailBlock) {
            RailShape shape = state.getValue(RailControllerBlock.RAIL_SHAPE);
            float yaw = switch (shape) {
                case NORTH_SOUTH, ASCENDING_NORTH, ASCENDING_SOUTH -> 90f;
                case EAST_WEST, ASCENDING_EAST, ASCENDING_WEST -> 0f;
                default -> cart.getYRot();
            };

            cart.setYRot(yaw);
            cart.yRotO = yaw;
        }
    }

    @Override
    public void reverie_dreams$setSignName(String newName) {
        this.reverie_dreams$signalName = newName;
    }

    @Override
    public String reverie_dreams$getSignName() {
        return this.reverie_dreams$signalName;
    }
}
