package cc.thonly.reverie_dreams.block.redstone;

import cc.thonly.reverie_dreams.block.entity.SignalRailBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartCommandBlock;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.NameTagItem;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RailState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class SignalRailBlock extends BaseRailBlock implements EntityBlock {
    public static final MapCodec<SignalRailBlock> CODEC = simpleCodec(SignalRailBlock::new);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final EnumProperty<RailShape> RAIL_SHAPE = BlockStateProperties.RAIL_SHAPE_STRAIGHT;

    public SignalRailBlock(Properties properties) {
        super(true, properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(POWERED, false)
                .setValue(RAIL_SHAPE, RailShape.NORTH_SOUTH)
                .setValue(WATERLOGGED, false)
        );
    }

    @Override
    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
        builder.add(RAIL_SHAPE);
        builder.add(WATERLOGGED);
    }

    @Override
    public @NotNull InteractionResult useItemOn(ItemStack itemStack, BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult blockHitResult) {
        if (!level.isClientSide()) {
            if (itemStack.getItem() instanceof NameTagItem) {
                Component custom = itemStack.getCustomName();
                if (custom == null) {
                    return InteractionResult.FAIL;
                }
                String name = custom.getString();
                SignalRailBlockEntity blockEntity = getBlockEntity(level, blockPos);
                if (blockEntity != null) {
                    blockEntity.setSignName(name);
                    blockEntity.setChanged();
                    player.swing(hand);
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        }
        return super.useItemOn(itemStack, blockState, level, blockPos, player, hand, blockHitResult);
    }

    @Override
    protected void entityInside(
            BlockState blockState, Level level, BlockPos blockPos, Entity entity, InsideBlockEffectApplier insideBlockEffectApplier, boolean bl
    ) {
        if (!level.isClientSide()) {
            if (!(Boolean) blockState.getValue(POWERED)) {
                this.checkPressed(level, blockPos, blockState);
            }
        }
    }

    @Override
    protected void tick(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(POWERED)) {
            this.checkPressed(serverLevel, blockPos, blockState);
        }
    }

    @Override
    protected int getSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        return blockState.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState blockState, BlockGetter blockGetter, BlockPos blockPos, Direction direction) {
        if (!(Boolean) blockState.getValue(POWERED)) {
            return 0;
        } else {
            return direction == Direction.UP ? 15 : 0;
        }
    }

    private void checkPressed(Level level, BlockPos blockPos, BlockState blockState) {
        if (this.canSurvive(blockState, level, blockPos)) {
            boolean bl = blockState.getValue(POWERED);
            boolean bl2 = false;
            SignalRailBlockEntity blockEntity = getBlockEntity(level, blockPos);
            List<AbstractMinecart> list = this.getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, entity -> true);
            boolean signalEquals = false;
            if (!list.isEmpty()) {
                bl2 = true;
                if (blockEntity != null) {
                    for (AbstractMinecart abstractMinecart : list) {
                        if (blockEntity.testSignName(abstractMinecart)) {
                            signalEquals = true;
                            break;
                        }
                    }
                }

            }
            if (bl2 && !bl && signalEquals) {
                BlockState blockState2 = blockState.setValue(POWERED, true);
                level.setBlock(blockPos, blockState2, 3);
                this.updatePowerToConnected(level, blockPos, blockState2, true);
                level.updateNeighborsAt(blockPos, this);
                level.updateNeighborsAt(blockPos.below(), this);
                level.setBlocksDirty(blockPos, blockState, blockState2);
            }

            if (!bl2 && bl) {
                BlockState blockState2 = blockState.setValue(POWERED, false);
                level.setBlock(blockPos, blockState2, 3);
                this.updatePowerToConnected(level, blockPos, blockState2, false);
                level.updateNeighborsAt(blockPos, this);
                level.updateNeighborsAt(blockPos.below(), this);
                level.setBlocksDirty(blockPos, blockState, blockState2);
            }

            if (bl2) {
                level.scheduleTick(blockPos, this, 20);
            }

            level.updateNeighbourForOutputSignal(blockPos, this);
        }
    }

    protected void updatePowerToConnected(Level level, BlockPos blockPos, BlockState blockState, boolean bl) {
        RailState railState = new RailState(level, blockPos, blockState);

        for (BlockPos blockPos2 : railState.getConnections()) {
            BlockState blockState2 = level.getBlockState(blockPos2);
            level.neighborChanged(blockState2, blockPos2, blockState2.getBlock(), null, false);
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState blockState) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState blockState, Level level, BlockPos blockPos, Direction direction) {
        if (blockState.getValue(POWERED)) {
            List<MinecartCommandBlock> list = this.getInteractingMinecartOfType(level, blockPos, MinecartCommandBlock.class, entity -> true);
            if (!list.isEmpty()) {
                return list.get(0).getCommandBlock().getSuccessCount();
            }

            List<AbstractMinecart> list2 = this.getInteractingMinecartOfType(level, blockPos, AbstractMinecart.class, EntitySelector.CONTAINER_ENTITY_SELECTOR);
            if (!list2.isEmpty()) {
                return AbstractContainerMenu.getRedstoneSignalFromContainer((Container) list2.get(0));
            }
        }

        return 0;
    }

    private <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level level, BlockPos blockPos, Class<T> class_, Predicate<Entity> predicate) {
        return level.getEntitiesOfClass(class_, this.getSearchBB(blockPos), predicate);
    }

    private AABB getSearchBB(BlockPos blockPos) {
        double d = 0.2;
        return new AABB(
                blockPos.getX() + 0.2, blockPos.getY(), blockPos.getZ() + 0.2, blockPos.getX() + 1 - 0.2, blockPos.getY() + 1 - 0.2, blockPos.getZ() + 1 - 0.2
        );
    }

    @Override
    protected boolean isSignalSource(BlockState blockState) {
        return true;
    }

    @Override
    public @NotNull MapCodec<? extends BaseRailBlock> codec() {
        return CODEC;
    }

    @Override
    public @NotNull Property<RailShape> getShapeProperty() {
        return RAIL_SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SignalRailBlockEntity(blockPos, blockState);
    }

    public SignalRailBlockEntity getBlockEntity(Level level, BlockPos blockPos) {
        BlockEntity blockEntity = level.getBlockEntity(blockPos);
        return blockEntity instanceof SignalRailBlockEntity ? (SignalRailBlockEntity) blockEntity : null;
    }
}
