package cc.thonly.reverie_dreams.fabric.polymer.block.model;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.core.api.block.PolymerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;

public interface TransparentPlantWatterlogged extends PolymerBlock, PolymerTexturedBlock {
    BlockState TRANSPARENT_WATTERLOGGED = PolymerBlockResourceUtils.requestEmpty(BlockModelType.KELP);
    @Override
    default BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return TRANSPARENT_WATTERLOGGED;
    }
}