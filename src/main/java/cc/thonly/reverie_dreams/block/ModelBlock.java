package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.state.ModBlockStateTemplates;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;

public class ModelBlock extends HorizontalFacingBlock {
    public static final MapCodec<ModelBlock> CODEC = createCodec(ModelBlock::new);
    public static final EnumProperty<SixteenDirection> FACING_16 = ModBlockStateTemplates.FACING_16;

    public ModelBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING_16, SixteenDirection.NORTH));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        double yaw = ctx.getPlayerYaw();
        SixteenDirection direction = SixteenDirection.fromYaw(yaw);
        return this.getDefaultState().with(FACING_16, direction);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING_16);
    }

    @Override
    public MapCodec<? extends HorizontalFacingBlock> getCodec() {
        return CODEC;
    }
}
