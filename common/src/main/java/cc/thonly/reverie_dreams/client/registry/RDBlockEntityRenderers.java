package cc.thonly.reverie_dreams.client.registry;

import cc.thonly.reverie_dreams.ReverieDreamsClient;
import cc.thonly.reverie_dreams.api.client.BlockEntityRendererRegistry;
import cc.thonly.reverie_dreams.client.renderer.blockentity.PlateBlockEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.blockentity.GensokyoAltarBlockEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.blockentity.KitchenBlockEntityRenderer;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;

public class RDBlockEntityRenderers {
    public static void initialize() {
        BlockEntityRendererRegistry.register(RDBlockEntityTypes.PLATE, PlateBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(RDBlockEntityTypes.GENSOKYO_ALTAR, GensokyoAltarBlockEntityRenderer::new);
        BlockEntityRendererRegistry.register(RDBlockEntityTypes.KITCHENWARE_BLOCK, KitchenBlockEntityRenderer::new);
    }
}
