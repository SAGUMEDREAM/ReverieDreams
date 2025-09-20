package cc.thonly.reverie_dreams.entity.skin;

import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class NPCSkinConfigs {

    public static void bootstrap(IntrinsicalRegister<NPCSkinConfig> registry) {

    }

    public static void reload(ResourceManager manager) {
        for (NPCSkin skin : RegistryManager.ROLE_SKIN) {
            skin.setConfig(new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty()));
        }
        Map<Identifier, Resource> resources = manager.findResources("skin_config", id -> id.getPath().endsWith(".json"));
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Identifier key = Identifier.of(resourceId.getNamespace(), resourceId.getPath()
                    .replace("skin_config/", "")
                    .replace(".json", "")
            );
            Resource resource = entry.getValue();
            NPCSkin skin = RegistryManager.ROLE_SKIN.get(key);
            if (skin == null) {
                log.warn("Unknown skin id: {}", resourceId);
                continue;
            }
            try (InputStream stream = resource.getInputStream()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<NPCSkinConfig> result = NPCSkinConfig.CODEC.parse(input);
                result.resultOrPartial(error -> log.warn("Failed to parse Skin Config for {}: {}", resourceId, error))
                        .ifPresent(data -> {
                            skin.setConfig(data);
                            RegistryManager.register(RegistryManager.SKIN_CONFIG, key, data);
                        });
            } catch (IOException e) {
                log.error("Failed to load Skin Config {}: {}", resourceId, e.getMessage(), e);
            }

        }
    }
}
