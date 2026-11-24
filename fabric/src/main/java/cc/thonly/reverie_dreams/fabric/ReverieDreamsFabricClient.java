package cc.thonly.reverie_dreams.fabric;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.ReverieDreamsClient;
import cc.thonly.reverie_dreams.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.client.renderer.danmaku.OrbRenderBatch;
import dev.architectury.platform.Platform;
import dev.architectury.platform.client.ConfigurationScreenRegistry;
import me.shedaniel.autoconfig.AutoConfigClient;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReverieDreamsFabricClient implements ClientModInitializer {
    public static final String MOD_ID = ReverieDreams.MOD_ID;
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        ReverieDreamsClient.initialize(() -> {});
        LevelRenderEvents.END_EXTRACTION.register(context -> OrbRenderBatch.prepare(context.levelState()));
        ReverieDreamsClient.runLateInit();
        ReverieDreamsPluginLoader.runClientSetup();
    }

    public static Identifier id(String id) {
        return Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

}
