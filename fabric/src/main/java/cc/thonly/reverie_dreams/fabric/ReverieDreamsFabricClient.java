package cc.thonly.reverie_dreams.fabric;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsClient;
import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import dev.architectury.platform.Platform;
import dev.architectury.platform.client.ConfigurationScreenRegistry;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

public class ReverieDreamsFabricClient implements ClientModInitializer {
    public static final String MOD_ID = ReverieDreams.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReverieDreamsClient.initialize(() -> {

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