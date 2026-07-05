package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsClient;
import cc.thonly.reverie_dreams.neoforge.compat.AppleSkinEventHandler;
import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = ReverieDreams.MOD_ID, dist = Dist.CLIENT)
public class ReverieDreamsNeoForgeClient {
    public ReverieDreamsNeoForgeClient(ModContainer modContainer, IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modContainer, modEventBus);
        BalmClient.initializeMod(ReverieDreams.MOD_ID, context, registrars -> {
            ReverieDreamsClient.initialize(registrars, () -> {

            });
        });
        modEventBus.addListener(this::onClientSetup);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            if (ModList.get().isLoaded("appleskin")) {
                NeoForge.EVENT_BUS.register(new AppleSkinEventHandler());
            }
        });
    }
}
