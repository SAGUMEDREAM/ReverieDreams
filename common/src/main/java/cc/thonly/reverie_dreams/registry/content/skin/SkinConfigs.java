package cc.thonly.reverie_dreams.registry.content.skin;

import cc.thonly.reverie_dreams.data.skin.SkinConfig;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class SkinConfigs {

    public static void bootstrap(RegistryProvider<SkinConfig> registry) {

    }

    public static void onReload(ResourceManager manager) {
        for (SkinType skin : BuiltInRegistryProviders.SKIN_TYPE) {
            SkinConfig config = new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty());
            skin.setConfig(config);
            config.setSkin(skin);
        }
        Map<Identifier, Resource> resources = manager.listResources("skin_config", id -> id.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier key = entry.getKey();
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);
                DataResult<SkinConfig> result = SkinConfig.CODEC.parse(input);
                result.resultOrPartial(error -> log.warn("Failed to parse Skin Config for {}: {}", key, error))
                        .ifPresent(skinConfig -> {
                            Identifier registryKey = skinConfig.getRegistryKey();
                            SkinType skin = BuiltInRegistryProviders.SKIN_TYPE.getValue(registryKey);
                            if (skin == null) {
                                log.warn("Unknown skin id: {}", registryKey);
                                return;
                            }
                            skin.setConfig(skinConfig);
                            skinConfig.setSkin(skin);
                            BuiltInRegistryProviders.register(BuiltInRegistryProviders.SKIN_CONFIG, key, skinConfig);
                        });
            } catch (IOException e) {
                log.error("Failed to load Skin Config {}: {}", key, e.getMessage(), e);
            }
        }
    }

}
