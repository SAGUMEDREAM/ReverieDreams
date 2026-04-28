package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsClient;
import net.blay09.mods.balm.client.BalmClient;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = ReverieDreams.MOD_ID, dist = Dist.CLIENT)
public class ReverieDreamsNeoForgeClient {
    public ReverieDreamsNeoForgeClient(IEventBus modEventBus) {
        final var context = new NeoForgeLoadContext(modEventBus);
        BalmClient.initializeMod(ReverieDreams.MOD_ID, context, registrars -> {
            ReverieDreamsClient.initialize(registrars, () -> {

            });
        });
    }
}
