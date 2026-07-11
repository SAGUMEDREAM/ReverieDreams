package cc.thonly.reverie_dreams.fabric;

import cc.thonly.keine.fabric.FabricKeine;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.api.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.plugin.callback.ReverieDreamsExtensionEvents;
import cc.thonly.reverie_dreams.creative_tab.content.BaseCreativeTab;
import cc.thonly.reverie_dreams.fabric.api.ReverieDreamsPolymerBridge;
import cc.thonly.reverie_dreams.fabric.compat.ReverieDreamsFabricCompats;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.util.PlatformContext;
import com.mojang.serialization.Lifecycle;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityDataRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import java.util.*;
import java.util.List;

@SuppressWarnings({"unchecked"})
@Slf4j
public class ReverieDreamsFabric implements ModInitializer {
    public static final List<Runnable> FABRIC_LATE_INIT = new ArrayList<>();

    static {
        ReverieDreams.REGISTRY_GETTER = resourceKey -> new RegistryImpl<>((ResourceKey<? extends Registry<Object>>) resourceKey, Lifecycle.stable()) {
        };
        ReverieDreams.REGISTRY_SHADOWER = (resourceKey, objects) -> new RegistryImpl<>((ResourceKey<? extends Registry<Object>>) resourceKey, (RegistryImpl<Object>) objects) {
        };
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onInitialize() {
        this.setupEarly();
        this.checkApiLoaded();
        if (PlatformContext.hasPolymer()) {
            ReverieDreamsPolymerBridge.tryPreloadPolymer();
        }
        ReverieDreams.initialize(() -> {
            ReverieDreams.ENTITY_DATA_SERIALIZER_REGISTRY.forEach(FabricEntityDataRegistry::register);
            ReverieDreamsPolymerBridge.tryPolymerify();
            ReverieDreamsFabricCompats.initialize();
            ReverieDreams.COMMON_LATE_INIT.forEach(Runnable::run);
            ReverieDreams.COMMON_LATE_INIT.clear();
            ReverieDreams.BUS_LATE_INIT.forEach(Runnable::run);
            ReverieDreams.BUS_LATE_INIT.clear();
            ReverieDreamsPluginLoader.run();
            CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register(BaseCreativeTab::busInvoker);
            Placeholders.registerCommon(ReverieDreams.id("version"), (ctx, args) -> PlaceholderResult.value(PlatformContext.VERSION.get()));
        });
    }

    public void checkApiLoaded() {
        FabricKeine.loadApiImpl();
        ReverieDreamsExtensionEvents.SCAN_EVENT.register(this::loadPlugins);
    }

    public List<ReverieDreamsPlugin> loadPlugins() {
        return FabricLoader.getInstance()
                .getEntrypoints("reverie_dreams:extension", ReverieDreamsPlugin.class);
    }

    private void setupEarly() {
        List<String> launchArgs = Arrays.stream(FabricLoader.getInstance().getLaunchArguments(true)).toList();
        for (String arg : launchArgs) {
            if (arg.contains("--output") || arg.contains("--input") || arg.contains("--mod") || arg.contains("--all")) {
                PlatformContext.IS_DATAGEN_MODE = true;
                break;
            }
        }
    }

}
