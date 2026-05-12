package cc.thonly.reverie_dreams.client.registry;

import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import cc.thonly.reverie_dreams.client.renderer.blockentity.FoodDisplayBlockEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.blockentity.GensokyoAltarBlockEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.blockentity.KitchenBlockEntityRenderer;
import net.blay09.mods.balm.client.renderer.blockentity.BalmBlockEntityRendererRegistrar;

public class RDBlockEntityRenderers {
    public static void initialize(BalmBlockEntityRendererRegistrar registrar) {
        registrar.register(RDBlockEntityTypes.FOOD_DISPLAY, FoodDisplayBlockEntityRenderer::new);
        registrar.register(RDBlockEntityTypes.GENSOKYO_ALTAR, GensokyoAltarBlockEntityRenderer::new);
        registrar.register(RDBlockEntityTypes.KITCHENWARE_BLOCK, KitchenBlockEntityRenderer::new);
    }
}
