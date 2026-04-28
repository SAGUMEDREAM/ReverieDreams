package cc.thonly.reverie_dreams.fabric.polymer.block.model;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.world.level.block.state.BlockState;
import xyz.nucleoid.packettweaker.PacketContext;

public interface TransparentFlatTripWire extends PolymerBlock, PolymerTexturedBlock {
    BlockState TRANSPARENT_FLAT_TRIPIWIRE = PolymerBlockResourceUtils.requestEmpty(BlockModelType.TRIPWIRE_BLOCK_FLAT);
    @Override
    default BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return TRANSPARENT_FLAT_TRIPIWIRE;
    }
}