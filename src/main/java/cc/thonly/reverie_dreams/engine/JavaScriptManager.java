package cc.thonly.reverie_dreams.engine;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.util.ConstantInfo;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
public class JavaScriptManager {
    private static final String DIRNAME = "javascript_src";
    private static final IntrinsicalRegister<JavaScriptElement> REGISTRY = RegistryManager.JAVASCRIPT_ELEMENT;
    private static final JavaScriptManager INSTANCE = new JavaScriptManager();
    private static final Supplier<ScriptEngine> ENGINE = () -> new ScriptEngineManager().getEngineByName("JavaScript");

    public static JavaScriptManager getInstance() {
        return INSTANCE;
    }

    public boolean run(@Nullable ServerPlayer player,
                       @Nullable Level world,
                       @Nullable BlockPos blockPos,
                       @Nullable Entity target,
                       @Nullable Map<?, ?> args,
                       JavaScriptElement element) {
        try {
            ScriptEngine scriptEngine = ENGINE.get();
            String src = element.getSrc();
            scriptEngine.eval(src);

            Invocable invocable = (Invocable) scriptEngine;

            invocable.invokeFunction("main", player, world, blockPos, target, args);
            return true;
        } catch (Exception e) {
            log.error("Can't run script key {}", element.getSrc(), e);
            return false;
        }
    }

    public String getClassPath(String classPath) {
        return this.getIntermediaryClass(classPath);
    }

    public String getIntermediaryClass(String classPath) {
        if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
            return classPath.replace('.', '/');
        } else {
            return ConstantInfo.MAPPING_RESOLVER.mapClassName("intermediary", classPath.replace('.', '/'));
        }
    }

    public Optional<JavaScriptElement> get(ResourceLocation key) {
        return Optional.ofNullable(REGISTRY.getValue(key));
    }

    public static void reload(ResourceManager manager) {
        Map<ResourceLocation, Resource> resources = manager.listResources(DIRNAME, id ->
                id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".js")
        );
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation fileId = entry.getKey();
            Resource resource = entry.getValue();
            ResourceLocation key = ResourceLocation.fromNamespaceAndPath(fileId.getNamespace(), fileId.getPath().replace(DIRNAME + "/", "").replace(".json", ""));
            try (InputStream inputStream = resource.open()) {
                String src = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                RegistryManager.register(REGISTRY, key, new JavaScriptElement(src));
            } catch (Exception e) {
                log.error("Can't load script {}", key, e);
            }
        }
    }

    public static void bootstrap(IntrinsicalRegister<JavaScriptElement> registry) {

    }
}
