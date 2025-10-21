package cc.thonly.reverie_dreams.entity.skin;

import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.util.SkinFetcher;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
public class NPCSkinConfigs {

    public static void bootstrap(IntrinsicalRegister<NPCSkinConfig> registry) {

    }

    public static void reload(ResourceManager manager) {
        for (NPCSkin skin : RegistryManager.ROLE_SKIN) {
            NPCSkinConfig config = new NPCSkinConfig(NPCSkinConfig.ModelType.SLIM, Optional.empty(), Optional.empty());
            skin.setConfig(config);
            config.setSkin(skin);
        }
        Map<Identifier, Resource> resources = manager.findResources("skin_config", id -> id.getPath().endsWith(".json"));
//        List<Runnable> tasks = new ArrayList<>();
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
                            data.setSkin(skin);
                            RegistryManager.register(RegistryManager.SKIN_CONFIG, key, data);
//                            tasks.add(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Optional<Property> skinFromNPCSkin = SkinFetcher.getSkinFromNPCSkin(data);
//                                    skinFromNPCSkin.ifPresent(skin::setInstance);
//                                }
//                            });
                        });
            } catch (IOException e) {
                log.error("Failed to load Skin Config {}: {}", resourceId, e.getMessage(), e);
            }

//            runAllAsync(tasks);
        }
    }
    public static CompletableFuture<Void> runAllAsync(List<Runnable> tasks) {
        ExecutorService executor = Executors.newFixedThreadPool(
                Math.min(tasks.size(), Runtime.getRuntime().availableProcessors())
        );

        List<CompletableFuture<Void>> futures = tasks.stream()
                .map(task -> CompletableFuture.runAsync(task, executor))
                .toList();

        // 返回一个 future，让调用者决定何时等待
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .whenComplete((v, e) -> executor.shutdown());
    }

}
