package cc.thonly.reverie_dreams.fabric;

import cc.thonly.keine.fabric.FabricKeine;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.api.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.plugin.callback.ReverieDreamsExtensionEvents;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.api.registry.EntityDataSerializerProviders;
import cc.thonly.reverie_dreams.creative_tab.content.BaseCreativeTab;
import cc.thonly.reverie_dreams.fabric.api.ReverieDreamsPolymerBridge;
import cc.thonly.reverie_dreams.fabric.compat.ReverieDreamsFabricCompats;
import cc.thonly.reverie_dreams.fabric.impl.FabricRegistryProvider;
import cc.thonly.reverie_dreams.registry.impl.MergeRegistry;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
public class ReverieDreamsFabric implements ModInitializer {
    public static final List<Runnable> FABRIC_LATE_INIT = new ArrayList<>();

    @Override
    public void onInitialize() {
        this.setupEarly();
        this.checkApiLoaded();
        if (PlatformContext.hasPolymer()) {
            ReverieDreamsPolymerBridge.tryPreloadPolymer();
        }
        ReverieDreams.initialize(() -> {
            EntityDataSerializerProviders.get().forEach(FabricEntityDataRegistry::register);
            ReverieDreamsFabricCompats.initialize();
            ReverieDreams.COMMON_LATE_INIT.forEach(Runnable::run);
            ReverieDreams.COMMON_LATE_INIT.clear();
            ReverieDreams.BUS_LATE_INIT.forEach(Runnable::run);
            ReverieDreams.BUS_LATE_INIT.clear();
            ReverieDreamsPolymerBridge.tryPolymerify();
            ReverieDreamsPluginLoader.run();
            CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register(BaseCreativeTab::busInvoker);
            AliasManager.execute(Registries.ITEM, map -> map.forEach(BuiltInRegistries.ITEM::addAlias));
            AliasManager.execute(Registries.BLOCK, map -> map.forEach(BuiltInRegistries.BLOCK::addAlias));
            AliasManager.execute(Registries.ENTITY_TYPE, map -> map.forEach(BuiltInRegistries.ENTITY_TYPE::addAlias));
            AliasManager.execute(Registries.DATA_COMPONENT_TYPE, map -> map.forEach(BuiltInRegistries.DATA_COMPONENT_TYPE::addAlias));
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
