package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.keine.neoforge.NeoForgeKeine;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.api.ReverieDreamsExtension;
import cc.thonly.reverie_dreams.api.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.plugin.callback.ReverieDreamsExtensionEvents;
import cc.thonly.reverie_dreams.command.CommandInit;
import cc.thonly.reverie_dreams.creative_tab.content.BaseCreativeTab;
import cc.thonly.reverie_dreams.neoforge.compat.ReverieDreamsNeoForgeCompats;
import cc.thonly.reverie_dreams.neoforge.impl.NeoRegistryImpl;
import com.mojang.brigadier.CommandDispatcher;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.Balm;
import net.blay09.mods.balm.neoforge.platform.runtime.NeoForgeLoadContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Mod(ReverieDreams.MOD_ID)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ReverieDreamsNeoForge {
    public ReverieDreamsNeoForge(ModContainer modContainer, IEventBus modEventBus) {
        NeoForgeKeine.loadApiImpl();
        final var context = new NeoForgeLoadContext(modContainer, modEventBus);
        setupApi();
        Balm.initializeMod(ReverieDreams.MOD_ID, context, registrars -> {
            ReverieDreams.initialize(registrars, () -> {
                ReverieDreams.ENTITY_DATA_SERIALIZER_REGISTRY.forEach((identifier, entityDataSerializer) -> {
                    Registry.register(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, identifier, entityDataSerializer);
                });
            });
        });
        modEventBus.addListener(this::onCommonSetup);
        modEventBus.addListener(this::onCreativeTabEvent);
        NeoForge.EVENT_BUS.addListener(this::onRegisterCommands);
    }

    public void setupApi() {
        ReverieDreams.REGISTRY_GETTER = resourceKey -> new NeoRegistryImpl<>((ResourceKey<? extends Registry<Object>>) resourceKey) {
        };
        ReverieDreams.REGISTRY_SHADOWER = (resourceKey, handler) -> new NeoRegistryImpl(resourceKey, handler) {
        };
        ReverieDreamsExtensionEvents.SCAN_EVENT.register(this::loadPlugins);
    }

    @SubscribeEvent
    public void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ReverieDreams.COMMON_LATE_INIT.forEach(Runnable::run);
            ReverieDreams.COMMON_LATE_INIT.clear();
            ReverieDreams.BUS_LATE_INIT.forEach(Runnable::run);
            ReverieDreams.BUS_LATE_INIT.clear();
            ReverieDreamsPluginLoader.run();
            ReverieDreamsNeoForgeCompats.initialize();
        });
    }

    @SubscribeEvent
    public void onCreativeTabEvent(BuildCreativeModeTabContentsEvent event) {
        CreativeModeTab tab = event.getTab();
        BaseCreativeTab.busInvoker(tab, event);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        CommandBuildContext buildContext = event.getBuildContext();
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        CommandInit.initialize(dispatcher, buildContext);
    }

    public List<ReverieDreamsPlugin> loadPlugins() {
        List<ReverieDreamsPlugin> plugins = new ArrayList<>();
        for (var mod : ModList.get().getMods()) {

            ModFileScanData scanData = mod.getOwningFile().getFile().getScanResult();

            for (var ann : scanData.getAnnotations()) {

                if (!ann.annotationType().getClassName()
                        .equals(ReverieDreamsExtension.class.getName())) continue;

                try {
                    Class<?> clazz = Class.forName(ann.clazz().getClassName());

                    if (!ReverieDreamsPlugin.class.isAssignableFrom(clazz)) {
                        continue;
                    }

                    ReverieDreamsPlugin instance = (ReverieDreamsPlugin) clazz.getDeclaredConstructor().newInstance();
                    plugins.add(instance);
                } catch (Exception e) {
                    log.error("Can't load plugin {}", mod.getModId());
                }
            }
        }
        return plugins;
    }

}
