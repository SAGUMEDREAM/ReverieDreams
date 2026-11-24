package cc.thonly.reverie_dreams.block.base;

import cc.thonly.reverie_dreams.entity.SeatEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Implemented by blocks a player or NPC can sit on (chairs, floor cushions).
 * The shared {@link #OCCUPIED} property marks a block that already has a
 * rider, and the helpers below create and remove the invisible seat entity.
 */
public interface SeatBlock {
    BooleanProperty OCCUPIED = BooleanProperty.create("occupied");

    /**
     * Height of the seat surface above the block origin, in blocks.
     */
    default double seatHeight() {
        return 0.0;
    }

    default SeatEntity createSeat(Level level) {
        return new SeatEntity(level);
    }

    static boolean sit(BlockState state, Level level, BlockPos pos, LivingEntity livingEntity) {
        if (!(state.getBlock() instanceof SeatBlock seatBlock) || !state.hasProperty(OCCUPIED)) {
            return false;
        }

        if (state.getValue(OCCUPIED) && unlockIfFree(state, level, pos)) {
            state = level.getBlockState(pos);
        }

        if (state.getValue(OCCUPIED)) {
            return false;
        }

        SeatEntity seat = seatBlock.createSeat(level);
        seat.setPos(
                pos.getX() + 0.5,
                pos.getY() + seatBlock.seatHeight(),
                pos.getZ() + 0.5
        );
        level.addFreshEntity(seat);

        if (!livingEntity.startRiding(seat)) {
            seat.discard();
            return false;
        }

        level.setBlock(pos, state.setValue(OCCUPIED, true), Block.UPDATE_ALL);
        return true;
    }

    static boolean ejectSeat(BlockState state, Level level, BlockPos pos) {
        if (!(state.getBlock() instanceof SeatBlock)) {
            return false;
        }

        List<SeatEntity> entities = level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(0.25D),
                seat -> seat.blockPosition().equals(pos));

        if (entities.isEmpty()) {
            if (state.hasProperty(OCCUPIED) && state.getValue(OCCUPIED)) {
                level.setBlock(pos, state.setValue(OCCUPIED, false), Block.UPDATE_ALL);
            }
            return false;
        }

        boolean ejected = false;
        for (SeatEntity seat : entities) {
            if (!seat.getPassengers().isEmpty()) {
                seat.ejectPassengers();
                ejected = true;
            }
        }
        return ejected;
    }

    static boolean unlockIfFree(BlockState state, Level level, BlockPos pos) {
        if (!state.hasProperty(OCCUPIED) || !state.getValue(OCCUPIED)) {
            return false;
        }

        if (hasSeatEntity(level, pos)) {
            return false;
        }

        level.setBlock(pos, state.setValue(OCCUPIED, false), Block.UPDATE_ALL);
        return true;
    }

    static boolean hasSeatEntity(Level level, BlockPos pos) {
        return !level.getEntitiesOfClass(SeatEntity.class, new AABB(pos).inflate(1),
                seat -> seat.isAlive() && seat.blockPosition().equals(pos) && !seat.getPassengers().isEmpty()).isEmpty();
    }
}
