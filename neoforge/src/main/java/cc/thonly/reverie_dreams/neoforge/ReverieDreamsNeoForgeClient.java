package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsClient;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.client.renderer.danmaku.OrbRenderBatch;
import cc.thonly.reverie_dreams.neoforge.client.PakClientPacks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ReverieDreams.MOD_ID, dist = Dist.CLIENT)
public class ReverieDreamsNeoForgeClient {
    public ReverieDreamsNeoForgeClient(ModContainer modContainer, IEventBus modEventBus) {
        modEventBus.addListener(PakClientPacks::addPacks);
        ReverieDreamsClient.initialize(() -> {});
        NeoForge.EVENT_BUS.addListener((ExtractLevelRenderStateEvent event) -> OrbRenderBatch.prepare(event.getRenderState()));
        modEventBus.addListener(this::onClientSetup);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ReverieDreamsClient.runLateInit();
            ReverieDreamsPluginLoader.runClientSetup();
        });
    }
}
