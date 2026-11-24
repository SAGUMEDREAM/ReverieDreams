package cc.thonly.reverie_dreams.block.decor;

import cc.thonly.reverie_dreams.block.base.SeatBlock;
import cc.thonly.reverie_dreams.entity.CushionSeatEntity;
import cc.thonly.reverie_dreams.entity.SeatEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * A floor cushion: a one pixel thick decoration that a player can sit on.
 * Sitting reuses the shared seat entity, so it behaves like the kitchen
 * chairs (shift-click or shift to stand up).
 */
public class ZabutonBlock extends ThinDecorBlock implements SeatBlock {
    public static final MapCodec<ZabutonBlock> CODEC = simpleCodec(ZabutonBlock::new);

    private static final VoxelShape CUSHION = Block.box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0);
    private final DyeColor color;

    public ZabutonBlock(Properties properties) {
        this(DyeColor.BLUE, properties);
    }

    public ZabutonBlock(DyeColor color, Properties properties) {
        this(color, properties, CUSHION, true);
    }

    public ZabutonBlock(Properties properties, VoxelShape northShape, boolean hasCollision) {
        this(DyeColor.BLUE, properties, northShape, hasCollision);
    }

    private ZabutonBlock(DyeColor color, Properties properties, VoxelShape northShape, boolean hasCollision) {
        super(properties, northShape, hasCollision);
        this.color = color;
        this.registerDefaultState(this.defaultBlockState()
                .setValue(FACING, Direction.NORTH)
                .setValue(OCCUPIED, false));
    }

    public DyeColor color() {
        return this.color;
    }

    @Override
    public double seatHeight() {
        return 1.0 / 16.0;
    }

    @Override
    public SeatEntity createSeat(Level level) {
        return new CushionSeatEntity(level);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return super.getVisualShape(state, level, pos, context);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                            BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                return SeatBlock.ejectSeat(state, level, pos)
                        ? InteractionResult.SUCCESS_SERVER
                        : InteractionResult.FAIL;
            }

            if (state.getValue(OCCUPIED) && SeatBlock.unlockIfFree(state, level, pos)) {
                state = level.getBlockState(pos);
            }

            if (state.getValue(OCCUPIED)) {
                return InteractionResult.FAIL;
            }

            return SeatBlock.sit(state, level, pos, player)
                    ? InteractionResult.SUCCESS_SERVER
                    : InteractionResult.FAIL;
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OCCUPIED);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
