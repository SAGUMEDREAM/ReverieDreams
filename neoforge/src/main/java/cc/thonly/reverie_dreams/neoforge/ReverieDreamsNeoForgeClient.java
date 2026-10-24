package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsClient;
import cc.thonly.reverie_dreams.neoforge.compat.AppleSkinEventHandler;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Iterator;

@Mod(value = ReverieDreams.MOD_ID, dist = Dist.CLIENT)
public class ReverieDreamsNeoForgeClient {
    public ReverieDreamsNeoForgeClient(ModContainer modContainer, IEventBus modEventBus) {
        ReverieDreamsClient.initialize(() -> {

        });
        modEventBus.addListener(this::onClientSetup);
    }

    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            Iterator<Runnable> lateInit = ReverieDreamsClient.LATE_INIT.iterator();
            while (lateInit.hasNext()) {
                Runnable next = lateInit.next();
                next.run();
                lateInit.remove();
            }
            if (ModList.get().isLoaded("appleskin")) {
                NeoForge.EVENT_BUS.register(new AppleSkinEventHandler());
            }
        });
    }
}
