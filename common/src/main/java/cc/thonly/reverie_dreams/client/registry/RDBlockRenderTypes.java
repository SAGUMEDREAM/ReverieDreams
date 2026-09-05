package cc.thonly.reverie_dreams.client.registry;

import cc.thonly.reverie_dreams.api.client.BlockRenderTypeRegistry;
import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;

import java.util.Map;

@SuppressWarnings("deprecation")
public class RDBlockRenderTypes {
    public static void initialize() {
        for (FumoType fumoType : BuiltInRegistryProviders.FUMO) {
            BlockRenderTypeRegistry.setRenderLayer(fumoType.blockAsDeferred(), ChunkSectionLayer.CUTOUT);
        }
        BlockTypeGroup.LEAVES.stream().forEach(block -> BlockRenderTypeRegistry.setRenderLayer(block.builtInRegistryHolder(), ChunkSectionLayer.CUTOUT));
        BlockTypeGroup.SAPLING.stream().forEach(block -> BlockRenderTypeRegistry.setRenderLayer(block.builtInRegistryHolder(), ChunkSectionLayer.CUTOUT));
        BlockTypeGroup.KITCHENWARE.stream().forEach(block -> BlockRenderTypeRegistry.setRenderLayer(block.builtInRegistryHolder(), ChunkSectionLayer.CUTOUT));
        BlockTypeGroup.PLANT.stream().forEach(block -> BlockRenderTypeRegistry.setRenderLayer(block.builtInRegistryHolder(), ChunkSectionLayer.CUTOUT));
        BlockRenderTypeRegistry.setRenderLayer(RDBlocks.MARISA_HAT_BLOCK, ChunkSectionLayer.CUTOUT);
        BlockRenderTypeRegistry.setRenderLayer(RDBlocks.CASH_BOX_BLOCK, ChunkSectionLayer.CUTOUT);
        BlockRenderTypeRegistry.setRenderLayer(RDBlocks.PLATE, ChunkSectionLayer.CUTOUT);
        BlockRenderTypeRegistry.setRenderLayer(RDBlocks.GENSOKYO_ALTAR, ChunkSectionLayer.CUTOUT);
        BlockRenderTypeRegistry.setRenderLayer(RDBlocks.CHAIR, ChunkSectionLayer.CUTOUT);
        BlockRenderTypeRegistry.setRenderLayer(RDBlocks.TABLE, ChunkSectionLayer.CUTOUT);
        BlockRenderTypeRegistry.setRenderLayer(RDBlocks.BREWING_BARREL, ChunkSectionLayer.CUTOUT);
        BlockRenderTypeRegistry.setRenderLayer(RDBlocks.CUPBOARD, ChunkSectionLayer.CUTOUT);
        BlockRenderTypeRegistry.setRenderLayer(RDBlocks.ICE_MAKING_MACHINE, ChunkSectionLayer.CUTOUT);
        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
            BlockRenderTypeRegistry.setRenderLayer(view.getValue().getCropBlock(), ChunkSectionLayer.CUTOUT);
        }
    }
}
