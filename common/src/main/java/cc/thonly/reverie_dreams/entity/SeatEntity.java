package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.block.ChairBlock;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

@SuppressWarnings({"resource", "RedundantMethodOverride"})
public class SeatEntity extends Entity {
    private int liveTick = 3;
    private int checkTick = 5;

    public SeatEntity(Level level) {
        super(RDEntityTypes.SEAT.value(), level);
    }

    public SeatEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void tick() {
        super.tick();

        if (this.liveTick >= 0) {
            this.liveTick--;
        }

        if (this.liveTick < 0 && this.getPassengers().isEmpty()) {
            this.discard();
            return;
        }

        if (--this.checkTick <= 0) {
            this.checkTick = 5;

            BlockPos pos = this.blockPosition();
            if (!(this.level().getBlockState(pos).getBlock() instanceof ChairBlock)) {
                this.discard();
                return;
            }
        }

        this.setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public void removePassenger(Entity passenger) {
        super.removePassenger(passenger);

        BlockPos pos = this.blockPosition();

        if (this.level().getBlockState(pos).getBlock()
                instanceof ChairBlock) {

            this.level().setBlock(
                    pos,
                    this.level().getBlockState(pos)
                        .setValue(ChairBlock.OCCUPIED, false),
                    Block.UPDATE_ALL
            );
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
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {

    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {

    }
}
