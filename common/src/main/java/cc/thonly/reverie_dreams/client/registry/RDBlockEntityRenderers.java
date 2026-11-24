package cc.thonly.reverie_dreams.client.registry;

import cc.thonly.reverie_dreams.ReverieDreamsClient;
import cc.thonly.reverie_dreams.client.renderer.blockentity.FumoBlockEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.blockentity.PlateBlockEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.blockentity.GensokyoAltarBlockEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.blockentity.KitchenBlockEntityRenderer;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import cc.thonly.reverie_dreams.client.renderer.blockentity.BoardGameBlockEntityRenderer;
import cc.thonly.reverie_dreams.client.renderer.blockentity.WorkshopBlockEntityRenderer;

public class RDBlockEntityRenderers {
    public static void initialize() {
        ReverieDreamsClient.LATE_INIT.add(() -> {
            BlockEntityRendererRegistry.register(RDBlockEntityTypes.WORKSHOP_STATION.value(), WorkshopBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(RDBlockEntityTypes.LIVING_STORAGE.value(), WorkshopBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(RDBlockEntityTypes.BOARD_GAME.value(), BoardGameBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(RDBlockEntityTypes.FUMO.value(), FumoBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(RDBlockEntityTypes.PLATE.value(), PlateBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(RDBlockEntityTypes.GENSOKYO_ALTAR.value(), GensokyoAltarBlockEntityRenderer::new);
            BlockEntityRendererRegistry.register(RDBlockEntityTypes.KITCHENWARE_BLOCK.value(), KitchenBlockEntityRenderer::new);
        });
    }
}
