package cc.thonly.reverie_dreams.polymer.block;

import eu.pb4.factorytools.api.block.model.generic.BlockStateModel;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import xyz.nucleoid.packettweaker.PacketContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class RailPolymerBlock {
    public static final BlockState OVERLAY_0 = PolymerBlockResourceUtils.requestEmpty(BlockModelType.ACTIVE_PRESSURE_PLATE);
    public static final BlockState OVERLAY_1 = PolymerBlockResourceUtils.requestEmpty(BlockModelType.KELP_BLOCK);

    public static final BaseFactoryBlock INSTANCE = new BaseFactoryBlock(OVERLAY_0, false, BlockStateModel::midRange) {
        @Override
        public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
            if (state.hasProperty(BlockStateProperties.WATERLOGGED) && state.getValue(BlockStateProperties.WATERLOGGED)) {
                return OVERLAY_1;
            }
            return super.getPolymerBlockState(state, context);
        }
    };
}
