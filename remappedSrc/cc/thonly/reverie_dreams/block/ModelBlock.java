package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.state.ModBlockStateTemplates;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public class ModelBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<ModelBlock> CODEC = simpleCodec(ModelBlock::new);
    public static final EnumProperty<SixteenDirection> FACING_16 = ModBlockStateTemplates.FACING_16;

    public ModelBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING_16, SixteenDirection.NORTH));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        double yaw = ctx.getRotation();
        SixteenDirection direction = SixteenDirection.fromYaw(yaw);
        return this.defaultBlockState().setValue(FACING_16, direction);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING_16);
    }

    @Override
    public MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
}
