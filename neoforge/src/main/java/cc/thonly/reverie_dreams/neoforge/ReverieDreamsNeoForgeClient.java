package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsClient;
import cc.thonly.reverie_dreams.api.client.BlockRenderTypeRegistry;
import cc.thonly.reverie_dreams.api.client.BlockEntityRendererRegistry;
import cc.thonly.reverie_dreams.api.client.EntityRendererRegistry;
import cc.thonly.reverie_dreams.neoforge.compat.AppleSkinEventHandler;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Iterator;

@SuppressWarnings({"rawtypes", "unchecked"})
@Mod(value = ReverieDreams.MOD_ID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ReverieDreams.MOD_ID, value = Dist.CLIENT)
public class ReverieDreamsNeoForgeClient {
    public ReverieDreamsNeoForgeClient(ModContainer modContainer, IEventBus modEventBus) {
        ReverieDreamsClient.initialize(() -> {

        });
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (BlockEntityRendererRegistry.Entry<?, ?> entry : BlockEntityRendererRegistry.ENTRIES) {
            event.registerBlockEntityRenderer((BlockEntityType) entry.type().value(), entry.provider());
        }
        for (EntityRendererRegistry.Entry<?> entry : EntityRendererRegistry.ENTRIES) {
            event.registerEntityRenderer((EntityType) entry.entityType().value(), entry.entityRendererProvider());
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Iterator<Runnable> lateInit = ReverieDreamsClient.LATE_INIT.iterator();
            while (lateInit.hasNext()) {
                Runnable next = lateInit.next();
                next.run();
                lateInit.remove();
            }
            for (BlockRenderTypeRegistry.Entry entry : BlockRenderTypeRegistry.ENTRIES) {
                Block block = entry.block().value();
                ChunkSectionLayer layer = entry.layer();
                ItemBlockRenderTypes.setRenderLayer(block, layer);
            }
            if (ModList.get().isLoaded("appleskin")) {
                NeoForge.EVENT_BUS.register(new AppleSkinEventHandler());
            }
        });
    }
}
