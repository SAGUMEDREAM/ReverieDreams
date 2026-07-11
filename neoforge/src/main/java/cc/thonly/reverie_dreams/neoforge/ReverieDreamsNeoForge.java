package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.keine.neoforge.NeoForgeKeine;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.api.ReverieDreamsExtension;
import cc.thonly.reverie_dreams.api.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.plugin.callback.ReverieDreamsExtensionEvents;
import cc.thonly.reverie_dreams.creative_tab.content.BaseCreativeTab;
import cc.thonly.reverie_dreams.neoforge.compat.ReverieDreamsNeoForgeCompats;
import cc.thonly.reverie_dreams.neoforge.impl.NeoRegistryImpl;
import cc.thonly.reverie_dreams.neoforge.util.biome.NeoForgeWorldGen;
import dev.architectury.registry.CreativeTabRegistry;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforgespi.language.ModFileScanData;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Mod(ReverieDreams.MOD_ID)
@EventBusSubscriber(modid = ReverieDreams.MOD_ID)
@SuppressWarnings({"unchecked", "rawtypes"})
public class ReverieDreamsNeoForge {
    public ReverieDreamsNeoForge(ModContainer modContainer, IEventBus modBus) {
        NeoForgeKeine.loadApiImpl();
        checkApiLoaded();
        ReverieDreams.initialize(() -> {
            ReverieDreams.ENTITY_DATA_SERIALIZER_REGISTRY.forEach((identifier, entityDataSerializer) -> {
                Registry.register(NeoForgeRegistries.ENTITY_DATA_SERIALIZERS, identifier, entityDataSerializer);
            });
        });
//        NeoForgeWorldGen.init(modBus);
    }

    static {
        ReverieDreams.REGISTRY_GETTER = resourceKey -> new NeoRegistryImpl<>((ResourceKey<? extends Registry<Object>>) resourceKey) {
        };
        ReverieDreams.REGISTRY_SHADOWER = (resourceKey, handler) -> new NeoRegistryImpl(resourceKey, handler) {
        };
    }

    public void checkApiLoaded() {
        ReverieDreamsExtensionEvents.SCAN_EVENT.register(ReverieDreamsNeoForge::loadPlugins);
    }

//    @SubscribeEvent
//    public static void onRegisterEvent(RegisterEvent event) {
//        Registry<MapCodec<? extends BiomeModifier>> registry = event.getRegistry(NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS);
//        if (registry == null) {
//            return;
//        }
//        Identifier id = Identifier.fromNamespaceAndPath(ArchitecturyConstants.MOD_ID, "none_biome_mod_codec");
//        if (registry.containsKey(id)) {
//            return;
//        }
//        try {
//            Class<?> impl$Clazz = Class.forName(
//                    "dev.architectury.registry.level.biome.forge.BiomeModificationsImpl$BiomeModifierImpl"
//            );
//
//            Field field = impl$Clazz.getDeclaredField("INSTANCE");
//            field.setAccessible(true);
//            Object obj = field.get(null);
//            MapCodec<BiomeModifier> unit = MapCodec.unit((BiomeModifier) obj);
//            Registry.register(registry, id, unit);
//
//            Class<?> clazz = Class.forName(
//                    "dev.architectury.registry.level.biome.forge.BiomeModificationsImpl"
//            );
//            Field noneBiomeModCodec = clazz.getDeclaredField("noneBiomeModCodec");
//            noneBiomeModCodec.setAccessible(true);
//            noneBiomeModCodec.set(null, unit);
//        } catch (Exception e) {
//            log.error("Error: ", e);
//        }
//    }

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
