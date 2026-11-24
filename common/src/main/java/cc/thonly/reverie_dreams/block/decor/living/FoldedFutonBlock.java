package cc.thonly.reverie_dreams.block.decor.living;

import cc.thonly.reverie_dreams.block.decor.ThinDecorBlock;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;

public class FoldedFutonBlock extends ThinDecorBlock {
    public static final MapCodec<FoldedFutonBlock> CODEC = simpleCodec(FoldedFutonBlock::new);

    public FoldedFutonBlock(Properties properties) {
        super(properties, JapaneseLivingShapes.north("folded_futon"), true);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getVisualShape(state, level, pos, context);
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }
}
