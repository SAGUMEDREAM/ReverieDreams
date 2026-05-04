package cc.thonly.reverie_dreams.client.registry;

import cc.thonly.reverie_dreams.block.BlockTypeGroup;
import cc.thonly.reverie_dreams.block.bundle.CropBlockBundle;
import cc.thonly.reverie_dreams.data.FumoType;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import net.blay09.mods.balm.client.renderer.chunk.BalmBlockRenderTypeRegistrar;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;

import java.util.Map;

@SuppressWarnings("deprecation")
public class RDBlockRenderTypes {
    public static void initialize(BalmBlockRenderTypeRegistrar registrar) {
        for (FumoType fumoType : RegistryImpls.FUMO) {
            registrar.setRenderLayer(fumoType.blockAsDeferred(), ChunkSectionLayer.CUTOUT);
        }
        BlockTypeGroup.LEAVES.stream().forEach(block -> registrar.setRenderLayer(block.builtInRegistryHolder(), ChunkSectionLayer.CUTOUT));
        BlockTypeGroup.SAPLING.stream().forEach(block -> registrar.setRenderLayer(block.builtInRegistryHolder(), ChunkSectionLayer.CUTOUT));
        BlockTypeGroup.KITCHENWARE.stream().forEach(block -> registrar.setRenderLayer(block.builtInRegistryHolder(), ChunkSectionLayer.CUTOUT));
        BlockTypeGroup.PLANT.stream().forEach(block -> registrar.setRenderLayer(block.builtInRegistryHolder(), ChunkSectionLayer.CUTOUT));
        registrar.setRenderLayer(RDBlocks.MARISA_HAT_BLOCK, ChunkSectionLayer.CUTOUT);
        registrar.setRenderLayer(RDBlocks.CASH_BOX_BLOCK, ChunkSectionLayer.CUTOUT);
        registrar.setRenderLayer(RDBlocks.FOOD_DISPLAY, ChunkSectionLayer.CUTOUT);
        registrar.setRenderLayer(RDBlocks.GENSOKYO_ALTAR, ChunkSectionLayer.CUTOUT);
        for (Map.Entry<Identifier, CropBlockBundle.Entry> view : CropBlockBundle.getViews()) {
            registrar.setRenderLayer(view.getValue().getCropBlock(), ChunkSectionLayer.CUTOUT);
        }
    }
}
