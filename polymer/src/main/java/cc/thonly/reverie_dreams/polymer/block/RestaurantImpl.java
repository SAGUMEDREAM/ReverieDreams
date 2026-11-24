package cc.thonly.reverie_dreams.polymer.block;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.block.restaurant.RestaurantBlock;
import eu.pb4.factorytools.api.block.FactoryBlock;
import eu.pb4.factorytools.api.virtualentity.*;
import eu.pb4.factorytools.api.util.LazyItemStack;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.*;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** One reused display, matching the normal client's facing and open/closed model. */
public final class RestaurantImpl implements PolymerTexturedBlock, FactoryBlock {
    private final LazyItemStack closedModel, openModel;
    public RestaurantImpl(RestaurantBlock block) {
        String base = "block/" + BuiltInRegistries.BLOCK.getKey(block).getPath();
        // Register both item-model definitions before the Polymer resource pack is generated.
        closedModel = ItemDisplayElementUtil.getModel(ReverieDreams.id(base));
        openModel = ItemDisplayElementUtil.getModel(ReverieDreams.id(base + "_open"));
    }
    @Override public BlockState getPolymerBlockState(BlockState state, PacketContext context) { return Blocks.BARRIER.defaultBlockState(); }
    @Override public ElementHolder createElementHolder(ServerLevel level, BlockPos pos, BlockState state) { return new Model(state, closedModel, openModel); }
    private static final class Model extends BlockModel {
        private final ItemDisplayElement item = new ItemDisplayElement();
        private final LazyItemStack closed, opened;
        Model(BlockState state, LazyItemStack closed, LazyItemStack opened) {
            this.closed = closed; this.opened = opened;
            item.setItemDisplayContext(ItemDisplayContext.NONE); update(state); addElement(item);
        }
        private void update(BlockState state) {
            item.setItem((state.getValue(RestaurantBlock.OPEN) ? opened : closed).get());
            item.setYaw(switch (state.getValue(RestaurantBlock.FACING)) { case NORTH -> 180; case EAST -> 270; case SOUTH -> 0; default -> 90; });
        }
        @Override public void notifyUpdate(HolderAttachment.UpdateType type) {
            if (type == BlockBoundAttachment.BLOCK_STATE_UPDATE) { update(blockState()); tick(); }
            super.notifyUpdate(type);
        }
    }
}
