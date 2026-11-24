package cc.thonly.reverie_dreams.block.nature;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.*;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.phys.shapes.*;

import java.util.Locale;

public final class BambooDebrisBlock extends VegetationBlock {
    public enum Form implements StringRepresentable {
        STUMP, SPLIT, FALLEN;

        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final MapCodec<BambooDebrisBlock> CODEC = simpleCodec(BambooDebrisBlock::new);
    public static final EnumProperty<Form> FORM = EnumProperty.create("form", Form.class);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BambooDebrisBlock(Properties p) {
        super(p);
        registerDefaultState(defaultBlockState().setValue(FORM, Form.STUMP).setValue(FACING, Direction.NORTH));
    }

    @Override
    public MapCodec<BambooDebrisBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected VoxelShape getShape(BlockState s, BlockGetter level, BlockPos p, CollisionContext context) {
        if (s.getValue(FORM) != Form.FALLEN)
            return box(5, 0, 5, 11, s.getValue(FORM) == Form.SPLIT ? 9 : 5, 11);
        return s.getValue(FACING).getAxis() == Direction.Axis.X ? box(0, 0, 5, 16, 5, 11) : box(5, 0, 0, 11, 5, 16);
    }

    @Override
    protected boolean mayPlaceOn(BlockState s, BlockGetter level, BlockPos p) {
        return LargeBambooBlock.soil(s);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!player.isShiftKeyDown())
            return InteractionResult.PASS;
        if (!level.isClientSide())
            level.setBlock(pos, state.cycle(FORM), Block.UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected BlockState rotate(BlockState s, Rotation r) {
        return s.setValue(FACING, r.rotate(s.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState s, Mirror m) {
        return rotate(s, m.getRotation(s.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> b) {
        b.add(FORM, FACING);
    }
}
