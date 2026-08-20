package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.keine.neoforge.NeoForgeKeine;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.api.ReverieDreamsExtension;
import cc.thonly.reverie_dreams.api.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.plugin.callback.ReverieDreamsExtensionEvents;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.api.registry.EntityDataSerializerProviders;
import cc.thonly.reverie_dreams.creative_tab.content.BaseCreativeTab;
import cc.thonly.reverie_dreams.neoforge.compat.ReverieDreamsNeoForgeCompats;
import cc.thonly.reverie_dreams.neoforge.impl.NeoMergeRegistry;
import cc.thonly.reverie_dreams.neoforge.impl.NeoRegistryProvider;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Mod(ReverieDreams.MOD_ID)
@EventBusSubscriber(modid = ReverieDreams.MOD_ID)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ReverieDreamsNeoForge {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ReverieDreams.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ReverieDreams.MOD_ID);
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(ReverieDreams.MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ReverieDreams.MOD_ID);

    public ReverieDreamsNeoForge(ModContainer modContainer, IEventBus modBus) {
        NeoForgeKeine.loadApiImpl();
        checkApiLoaded();
        ReverieDreams.initialize(() -> {
            EntityDataSerializerProviders.get().forEach((identifier, serializer) -> Registry.register(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, identifier, serializer));
            AliasManager.execute(Registries.ITEM, map -> map.forEach(ITEMS::addAlias));
            AliasManager.execute(Registries.BLOCK, map -> map.forEach(BLOCKS::addAlias));
            AliasManager.execute(Registries.ENTITY_TYPE, map -> map.forEach(ENTITY_TYPES::addAlias));
            AliasManager.execute(Registries.DATA_COMPONENT_TYPE, map -> map.forEach(DATA_COMPONENT_TYPES::addAlias));
            ITEMS.register(modBus);
            BLOCKS.register(modBus);
            ENTITY_TYPES.register(modBus);
            DATA_COMPONENT_TYPES.register(modBus);
        });
    }

    public void checkApiLoaded() {
        ReverieDreamsExtensionEvents.SCAN_EVENT.register(ReverieDreamsNeoForge::loadPlugins);
    }

    @SubscribeEvent
    public static void onRegisterEntityAttribute(EntityAttributeCreationEvent event) {

    }

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
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
    public static void onCreativeTabEvent(BuildCreativeModeTabContentsEvent event) {
        CreativeModeTab tab = event.getTab();
        BaseCreativeTab.busInvoker(tab, event);
    }

    public static List<ReverieDreamsPlugin> loadPlugins() {
        List<ReverieDreamsPlugin> plugins = new ArrayList<>();
        for (var mod : ModList.get().getMods()) {

            ModFileScanData scanData = mod.getOwningFile().getFile().getScanResult();

            for (var ann : scanData.getAnnotations()) {

                if (!ann.annotationType().getClassName()
                        .equals(ReverieDreamsExtension.class.getName()))
                    continue;

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
