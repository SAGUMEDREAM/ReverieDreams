package cc.thonly.reverie_dreams.engine;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.registry.IntrinsicalRegister;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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

    public boolean run(@Nullable ServerPlayerEntity player,
                       @Nullable World world,
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
            return Touhou.MAPPING_RESOLVER.mapClassName("intermediary", classPath.replace('.', '/'));
        }
    }

    public Optional<JavaScriptElement> get(Identifier key) {
        return Optional.ofNullable(REGISTRY.get(key));
    }

    public static void reload(ResourceManager manager) {
        Map<Identifier, Resource> resources = manager.findResources(DIRNAME, id ->
                id.getNamespace().equals(Touhou.MOD_ID) && id.getPath().endsWith(".js")
        );
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier fileId = entry.getKey();
            Resource resource = entry.getValue();
            Identifier key = Identifier.of(fileId.getNamespace(), fileId.getPath().replace(DIRNAME + "/", "").replace(".json", ""));
            try (InputStream inputStream = resource.getInputStream()) {
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
