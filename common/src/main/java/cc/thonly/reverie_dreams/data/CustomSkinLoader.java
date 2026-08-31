package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.CustomSkinConfig;
import cc.thonly.reverie_dreams.data.skin.CustomType;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class CustomSkinLoader {

    public static void onReload(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.listResources(("custom_skin_config"), id -> {
            return id.getPath().endsWith(".json");
        });
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier resourceId = entry.getKey();
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);
                DataResult<CustomSkinConfig> result = CustomSkinConfig.CODEC.parse(input);
                result.resultOrPartial(error -> ReverieDreams.LOGGER.warn("Failed to parse tags for {}: {}", resourceId, error))
                      .ifPresent(config -> {
                          SkinType value = config.value();
                          RegistryProvider<CustomType> registry = BuiltInRegistryProviders.CUSTOM_SKIN_TYPE;
                          ResourceKey<CustomType> key = registry.createKey(value.getId());
                          if (registry.containsKey(key)) {
                              registry.unregister(key);
                          }
                          if (value instanceof CustomType customType) {
                              registry.register(key, customType, RegistrationInfo.BUILT_IN);
                          }
                      });
            } catch (Exception e) {
                ReverieDreams.LOGGER.error("Failed to load custom_skin_config {}: {}", resourceId, e.getMessage(), e);
            }
        }
    }

}
