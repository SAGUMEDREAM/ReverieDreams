package cc.thonly.reverie_dreams.fabric;

import cc.thonly.keine.fabric.FabricKeine;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.command.CommandInit;
import cc.thonly.reverie_dreams.creative_tab.content.BaseCreativeTab;
import cc.thonly.reverie_dreams.fabric.api.ReverieDreamsPolymerBridge;
import cc.thonly.reverie_dreams.fabric.compat.ReverieDreamsFabricCompats;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.util.PlatformContext;
import com.mojang.serialization.Lifecycle;
import eu.pb4.placeholders.api.PlaceholderResult;
import eu.pb4.placeholders.api.Placeholders;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.fabric.platform.runtime.FabricLoadContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
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

    @Override
    public void onInitialize() {
        this.setupEarly();
        FabricKeine.loadApiImpl();
        FabricKeine.serverSideOnly();
        ReverieDreams.REGISTRY_GETTER = resourceKey -> new RegistryImpl<>((ResourceKey<? extends Registry<Object>>) resourceKey, Lifecycle.stable()) {

        };
        ReverieDreams.REGISTRY_SHADOWER = (resourceKey, objects) -> new RegistryImpl<>((ResourceKey<? extends Registry<Object>>) resourceKey, (RegistryImpl<Object>) objects) {
        };
        if (PlatformContext.hasPolymer()) {
            ReverieDreamsPolymerBridge.tryReplaceGuidebook();
        }
        Balm.initializeMod(ReverieDreams.MOD_ID, FabricLoadContext.INSTANCE, registrars -> ReverieDreams.initialize(registrars, () -> {
            ReverieDreams.ENTITY_DATA_SERIALIZER_REGISTRY.forEach(FabricEntityDataRegistry::register);
            ReverieDreamsPolymerBridge.tryPolymerify();
            ReverieDreamsFabricCompats.initialize();
            ReverieDreams.LATE_INIT.forEach(Runnable::run);
            ReverieDreams.LATE_INIT.clear();
            ReverieDreams.BUS_LATE_INIT.forEach(Runnable::run);
            ReverieDreams.BUS_LATE_INIT.clear();
            ReverieDreamsPluginLoader.run();
            CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
                CommandInit.initialize(dispatcher, registryAccess);
            });
            CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register(BaseCreativeTab::busInvoker);
            Placeholders.registerCommon(ReverieDreams.id("version"), (ctx, args) -> PlaceholderResult.value(PlatformContext.VERSION.get()));
        }));
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
