package cc.thonly.reverie_dreams.registry.content.skin;

import cc.thonly.reverie_dreams.data.skin.SkinConfig;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
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

    public static void bootstrap(RegistryHandler<SkinConfig> registry) {

    }

    public static void reload(ResourceManager manager) {
        for (SkinType skin : RegistryHandlers.SKIN_TYPE) {
            SkinConfig config = new SkinConfig(SkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty());
            skin.setConfig(config);
            config.setSkin(skin);
        }
        Map<ResourceLocation, Resource> resources = manager.listResources("skin_config", id -> id.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation resourceId = entry.getKey();
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(resourceId.getNamespace(), resourceId.getPath()
                    .replace("skin_config/", "")
                    .replace(".json", "")
            );
            System.out.println(key);
            Resource resource = entry.getValue();
            SkinType skin = RegistryHandlers.SKIN_TYPE.getValue(key);
            if (skin == null) {
                log.warn("Unknown skin id: {}", resourceId);
                continue;
            }
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<SkinConfig> result = SkinConfig.CODEC.parse(input);
                result.resultOrPartial(error -> log.warn("Failed to parse Skin Config for {}: {}", resourceId, error))
                        .ifPresent(data -> {
                            skin.setConfig(data);
                            data.setSkin(skin);
                            log.info("register skin {} {}", key, data);
                            RegistryHandlers.register(RegistryHandlers.SKIN_CONFIG, key, data);
                        });
            } catch (IOException e) {
                log.error("Failed to load Skin Config {}: {}", resourceId, e.getMessage(), e);
            }

//            runAllAsync(tasks);
        }
    }

}
