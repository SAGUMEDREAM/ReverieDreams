package cc.thonly.reverie_dreams.fabric;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsClient;
import cc.thonly.reverie_dreams.api.client.BlockRenderTypeRegistry;
import cc.thonly.reverie_dreams.api.client.BlockEntityRendererRegistry;
import cc.thonly.reverie_dreams.api.client.EntityRendererRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.fabricmc.fabric.impl.client.rendering.EntityRendererRegistryImpl;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

@SuppressWarnings({"rawtypes", "unchecked"})
public class ReverieDreamsFabricClient implements ClientModInitializer {
    public static final String MOD_ID = ReverieDreams.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReverieDreamsClient.initialize(() -> {
            for (BlockEntityRendererRegistry.Entry<?, ?> entry : BlockEntityRendererRegistry.ENTRIES) {
                BlockEntityRendererRegistryImpl.register((BlockEntityType) entry.type().value(), entry.provider());
            }
            for (EntityRendererRegistry.Entry<?> entry : EntityRendererRegistry.ENTRIES) {
                EntityRendererRegistryImpl.register((EntityType) entry.entityType().value(), entry.entityRendererProvider());
            }
            for (BlockRenderTypeRegistry.Entry entry : BlockRenderTypeRegistry.ENTRIES) {
                Block block = entry.block().value();
                ChunkSectionLayer layer = entry.layer();
                BlockRenderLayerMap.putBlock(block, layer);
            }
        });
        Iterator<Runnable> lateInit = ReverieDreamsClient.LATE_INIT.iterator();
        while (lateInit.hasNext()) {
            Runnable next = lateInit.next();
            next.run();
            lateInit.remove();
        }
    }

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

}