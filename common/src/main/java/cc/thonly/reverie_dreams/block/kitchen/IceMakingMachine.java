package cc.thonly.reverie_dreams.block.kitchen;

import cc.thonly.reverie_dreams.api.block.DispenserBlockItemBehaviors;
import cc.thonly.reverie_dreams.block.entity.IceMakingMachineBlockEntity;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.util.sound.SoundEventPlayUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class IceMakingMachine extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    public IceMakingMachine(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    static {
        DispenserBlockItemBehaviors.add(
                stack -> stack.is(Items.WATER_BUCKET),
                (source, stack) -> {
                    ServerLevel level = source.level();

                    BlockPos targetPos = source.pos().relative(
                            source.state().getValue(DispenserBlock.FACING)
                    );

                    BlockState targetState = level.getBlockState(targetPos);

                    if (!(targetState.getBlock() instanceof IceMakingMachine machine)) {
                        return DispenserBlockItemBehaviors.TriggerResult.pass();
                    }

                    IceMakingMachineBlockEntity blockEntity =
                            machine.getBlockEntity(level, targetPos);

                    if (blockEntity == null || !blockEntity.tryStart(stack)) {
                        return DispenserBlockItemBehaviors.TriggerResult.pass();
                    }

                    stack.shrink(1);

                    ItemStack bucket = Items.BUCKET.getDefaultInstance();

                    if (stack.isEmpty()) {
                        return DispenserBlockItemBehaviors.TriggerResult.success(bucket);
                    }

                    Containers.dropItemStack(
                            level,
                            source.pos().getX(),
                            source.pos().getY(),
                            source.pos().getZ(),
                            bucket
                    );

                    return DispenserBlockItemBehaviors.TriggerResult.success(stack);
                }
        );
        DispenserBlockItemBehaviors.add(
                stack -> stack.is(ItemTags.PICKAXES),
                (source, stack) -> {
                    ServerLevel level = source.level();

                    BlockPos targetPos = source.pos().relative(
                            source.state().getValue(DispenserBlock.FACING)
                    );

                    BlockState targetState = level.getBlockState(targetPos);

                    // 不是制冰机，交给原版镐子行为
                    if (!(targetState.getBlock() instanceof IceMakingMachine machine)) {
                        return DispenserBlockItemBehaviors.TriggerResult.pass();
                    }

                    IceMakingMachineBlockEntity blockEntity =
                            machine.getBlockEntity(level, targetPos);

                    // 对着制冰机，永远拦截镐子发射
                    if (blockEntity == null) {
                        return DispenserBlockItemBehaviors.TriggerResult.fail(stack);
                    }

                    // 没有成品，也拦截，不允许镐子射出去
                    if (blockEntity.getOutput().isEmpty()) {
                        return DispenserBlockItemBehaviors.TriggerResult.fail(stack);
                    }

                    Optional<ItemStack> optional = blockEntity.take();

                    if (optional.isEmpty()) {
                        return DispenserBlockItemBehaviors.TriggerResult.fail(stack);
                    }

                    ItemStack output = optional.get();

                    Direction facing = targetState.getValue(
                            IceMakingMachine.FACING
                    );

                    BlockPos outputPos = targetPos.relative(facing, -1);

                    Containers.dropItemStack(
                            level,
                            outputPos.getX() + 0.5,
                            outputPos.getY() + 0.5,
                            outputPos.getZ() + 0.5,
                            output
                    );

                    SoundEventPlayUtils.playSound(
                            level,
                            targetPos,
                            SoundEvents.GLASS_BREAK,
                            SoundSource.BLOCKS
                    );

                    stack.hurtAndBreak(
                            1,
                            level,
                            null,
                            item -> {}
                    );

                    return DispenserBlockItemBehaviors.TriggerResult.success(stack);
                }
        );
    }

    @Override
    public InteractionResult useItemOn(
            ItemStack itemStack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        IceMakingMachineBlockEntity blockEntity = this.getBlockEntity(level, pos);
        if (blockEntity == null) {
            return InteractionResult.FAIL;
        }

        if (!blockEntity.getOutput().isEmpty()) {
            Optional<ItemStack> optional = blockEntity.take();

            if (optional.isEmpty()) {
                return InteractionResult.FAIL;
            }

            ItemStack output = optional.get();

            if (!player.addItem(output)) {
                player.drop(output, false);
            }

            SoundEventPlayUtils.playSound(
                    player,
                    SoundEvents.GLASS_BREAK,
                    SoundSource.BLOCKS
            );

            return InteractionResult.SUCCESS_SERVER;
        }

        if (itemStack.is(Items.WATER_BUCKET) && blockEntity.canStart()) {
            SoundEventPlayUtils.playSound(
                    player,
                    SoundEvents.BUCKET_FILL,
                    SoundSource.BLOCKS
            );

            itemStack.consume(1, player);
            player.addItem(Items.BUCKET.getDefaultInstance());
            blockEntity.start();

            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.FAIL;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction direction = ctx.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, direction);
    }

    public @Nullable IceMakingMachineBlockEntity getBlockEntity(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity instanceof IceMakingMachineBlockEntity iceMakingMachineBlockEntity ? iceMakingMachineBlockEntity : null;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos worldPosition, BlockState blockState) {
        return new IceMakingMachineBlockEntity(worldPosition, blockState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> type) {
        return createTickerHelper(type, RDBlockEntityTypes.ICE_MAKING_MACHINE.get(), IceMakingMachineBlockEntity::onBlockEntityTick);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        IceMakingMachineBlockEntity blockEntity = this.getBlockEntity(level, pos);

        if (blockEntity == null || blockEntity.getOutput().isEmpty()) {
            return 0;
        }

        int count = blockEntity.getOutput().size();
        int max = Math.max(blockEntity.efficiencyToSize(), 1);

        return Math.min(15, Mth.ceil((float) count / max * 15.0F));
    }

    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> blockEntityType, BlockEntityType<E> blockEntityType2, BlockEntityTicker<? super E> blockEntityTicker) {
        return blockEntityType2 == blockEntityType ? (BlockEntityTicker<A>) blockEntityTicker : null;
    }
}
