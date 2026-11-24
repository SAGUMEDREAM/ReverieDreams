package cc.thonly.reverie_dreams.block.decor.workshop;

import cc.thonly.reverie_dreams.block.base.SeatBlock;
import cc.thonly.reverie_dreams.entity.CushionSeatEntity;
import cc.thonly.reverie_dreams.entity.SeatEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import cc.thonly.reverie_dreams.workshop.FurnitureSounds;
import net.minecraft.sounds.SoundEvents;

public class WorkshopBenchBlock extends WorkshopDecorBlock implements SeatBlock {
    public static final MapCodec<WorkshopBenchBlock> CODEC = simpleCodec(WorkshopBenchBlock::new);

    public WorkshopBenchBlock(Properties properties) {
        super(WorkshopKind.SHOP_BENCH, properties);
        registerDefaultState(defaultBlockState().setValue(OCCUPIED, false));
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OCCUPIED);
        builder.add(WorkshopConnections.PROPERTIES);
    }

    private BlockState connect(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        return WorkshopConnections.update(state, level, pos, (direction, other) -> direction.getAxis() != facing.getAxis() && other.getBlock() instanceof WorkshopBenchBlock && other.getValue(FACING) == facing);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connect(super.getStateForPlacement(context), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighbor, RandomSource random) {
        return direction.getAxis().isHorizontal() ? connect(state, level, pos) : super.updateShape(state, level, ticks, pos, direction, neighborPos, neighbor, random);
    }

    @Override
    protected String model(BlockState state) {
        return kind.id() + "_" + WorkshopConnections.localMask(state);
    }

    @Override
    public double seatHeight() {
        return 8.0 / 16.0;
    }

    @Override
    public SeatEntity createSeat(Level level) {
        return new CushionSeatEntity(level);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (player.isShiftKeyDown()) {
            if (!SeatBlock.ejectSeat(state, level, pos))
                return InteractionResult.PASS;
            FurnitureSounds.play(level, pos, SoundEvents.WOOD_STEP, .25f, 1.1f);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (!SeatBlock.sit(state, level, pos, player))
            return InteractionResult.FAIL;
        FurnitureSounds.play(level, pos, SoundEvents.WOOD_STEP, .25f, .9f);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return WorkshopConnections.rotate(state, rotation);
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return WorkshopConnections.mirror(state, mirror);
    }
}
