package cc.thonly.reverie_dreams.block.props;

import cc.thonly.reverie_dreams.api.entity.LockedCart;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RailControllerBlock extends BaseRailBlock {
    public static final MapCodec<RailControllerBlock> CODEC = simpleCodec(RailControllerBlock::new);
    public static final EnumProperty<RailShape> RAIL_SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty OCCUPIED = BlockStateProperties.OCCUPIED;

    public RailControllerBlock(Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(OCCUPIED, false)
                .setValue(RAIL_SHAPE, RailShape.NORTH_SOUTH)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
        builder.add(OCCUPIED);
        builder.add(RAIL_SHAPE);
        builder.add(WATERLOGGED);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos blockPos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block,
                                   @Nullable Orientation orientation, boolean bl) {
        super.neighborChanged(state, level, pos, block, orientation, bl);
        if (!level.isClientSide()) {
            boolean hasSignal = level.hasNeighborSignal(pos);
            if (hasSignal != state.getValue(OCCUPIED)) {
                level.setBlock(pos, state.setValue(OCCUPIED, hasSignal), 3);
            }
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity,
                                InsideBlockEffectApplier applier, boolean bl) {
        super.entityInside(state, level, pos, entity, applier, bl);
        if (!(entity instanceof AbstractMinecart cart)) return;
        if (!(entity instanceof LockedCart lockedCart)) return;

        if (!level.isClientSide()) {
            boolean nearCenter = isCartNearCenter(cart, pos);

            if (!state.getValue(OCCUPIED) && nearCenter) {
                if (!lockedCart.reverie_dreams$isCartLocked()) {
                    lockedCart.reverie_dreams$lockCart();
                }
                cart.setDeltaMovement(0, cart.getDeltaMovement().y, 0);
//                cart.hasImpulse = true;
            }

            this.checkMinecart(level, pos, state);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);

        if (state.getValue(OCCUPIED)) {
            this.releaseMinecarts(level, pos);
        }

        this.checkMinecart(level, pos, state);
    }

    private void checkMinecart(Level level, BlockPos pos, BlockState state) {
        boolean hasCart = hasCartNearCenter(level, pos);
        boolean old = state.getValue(POWERED);

        if (hasCart != old) {
            BlockState newState = state.setValue(POWERED, hasCart);
            level.setBlock(pos, newState, 3);
            level.updateNeighbourForOutputSignal(pos, this);
        }

        if (hasCart) {
            level.scheduleTick(pos, this, 4);
        }
    }

    private boolean hasCartNearCenter(Level level, BlockPos pos) {
        return level.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos))
                .stream()
                .anyMatch(cart -> isCartNearCenter(cart, pos));
    }

    private boolean isCartNearCenter(AbstractMinecart cart, BlockPos pos) {
        double dx = cart.getX() - (pos.getX() + 0.5);
        double dz = cart.getZ() - (pos.getZ() + 0.5);
        return dx * dx + dz * dz < 0.7 * 0.7;
    }

    private void releaseMinecarts(Level level, BlockPos pos) {
        List<AbstractMinecart> carts = level.getEntitiesOfClass(AbstractMinecart.class, new AABB(pos));
        for (AbstractMinecart cart : carts) {
            if (cart instanceof LockedCart locked && locked.reverie_dreams$isCartLocked()) {
                locked.reverie_dreams$releaseCart();
            }
        }
    }

    @Override
    public @NotNull MapCodec<? extends BaseRailBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull Property<RailShape> getShapeProperty() {
        return RAIL_SHAPE;
    }
}
