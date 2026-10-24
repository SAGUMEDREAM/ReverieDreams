package cc.thonly.reverie_dreams.neoforge;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.ReverieDreamsPluginLoader;
import cc.thonly.reverie_dreams.api.ReverieDreamsExtension;
import cc.thonly.reverie_dreams.api.ReverieDreamsPlugin;
import cc.thonly.reverie_dreams.api.creative_tab.ICreativeModeTabOutputExtension;
import cc.thonly.reverie_dreams.api.plugin.callback.ReverieDreamsExtensionEvents;
import cc.thonly.reverie_dreams.api.registry.AliasManager;
import cc.thonly.reverie_dreams.api.registry.EntityDataSerializerProviders;
import cc.thonly.reverie_dreams.api.registry.CompostingChanceRegistry;
import cc.thonly.reverie_dreams.api.registry.FlammableBlockRegistry;
import cc.thonly.reverie_dreams.creative_tab.content.BaseCreativeTab;
import cc.thonly.reverie_dreams.mixin.accessor.FireBlockAccessor;
import cc.thonly.reverie_dreams.neoforge.compat.ReverieDreamsNeoForgeCompats;
import cc.thonly.reverie_dreams.neoforge.impl.RegistryBuilderFactoryImpl;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforgespi.language.ModFileScanData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Mod(ReverieDreams.MOD_ID)
@EventBusSubscriber(modid = ReverieDreams.MOD_ID)
public class ReverieDreamsNeoForge {
    public static final Object2FloatMap<ItemLike> COMPOSTABLES = new Object2FloatOpenHashMap<>();
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ReverieDreams.MOD_ID);
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ReverieDreams.MOD_ID);
    public static final DeferredRegister.Entities ENTITY_TYPES = DeferredRegister.createEntities(ReverieDreams.MOD_ID);
    public static final DeferredRegister.DataComponents DATA_COMPONENT_TYPES = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ReverieDreams.MOD_ID);

    public ReverieDreamsNeoForge(ModContainer modContainer, IEventBus modBus) {
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
    public static void registerRegistries(NewRegistryEvent event) {
        RegistryBuilderFactoryImpl.BUILDERS.forEach((identifier, factory) -> {
            Registry<?> registry = factory.get();
            event.register(registry);
        });
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
            ReverieDreamsNeoForge.initializeCommonHook();
        });
    }

    private static void initializeCommonHook() {
        FireBlockAccessor fire = (FireBlockAccessor) Blocks.FIRE;
        for (FlammableBlockRegistry.Entry entry : FlammableBlockRegistry.ENTRIES) {
            int igniteOdds = entry.igniteOdds();
            int burnOdds = entry.burnOdds();
            Holder<Block> blockHolder = entry.blockHolder();
            if (blockHolder != null) {
                fire.reverie_dreams$setFlammable(blockHolder.value(), igniteOdds, burnOdds);
            }
            TagKey<Block> tagKey = entry.tagKey();
            if (tagKey != null) {
                for (Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tagKey)) {
                    fire.reverie_dreams$setFlammable(holder.value(), igniteOdds, burnOdds);
                }
            }
        }

        // 把 common 的堆肥注册表同步到 NeoForge 侧的查询 map
        COMPOSTABLES.clear();
        for (CompostingChanceRegistry.Entry entry : CompostingChanceRegistry.ENTRIES) {
            if (entry.item() != null) {
                COMPOSTABLES.put(entry.item().value(), entry.value());
            }
            if (entry.block() != null) {
                Item item = entry.block().value().asItem();
                if (item != Items.AIR) {
                    COMPOSTABLES.put(item, entry.value());
                }
            }
        }
    }

    /**
     * 供 {@code ComposterBlockMixin} 查询；没有注册过的物品返回 null，保持原版 -1 语义。
     */
    @Nullable
    public static Float getCompostValue(ItemStack stack) {
        Item item = stack.getItem();
        if (COMPOSTABLES.containsKey(item)) {
            return COMPOSTABLES.getFloat(item);
        }
        for (CompostingChanceRegistry.Entry entry : CompostingChanceRegistry.ENTRIES) {
            if (entry.tagKey() != null && stack.is(entry.tagKey())) {
                return entry.value();
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onCreativeTabEvent(BuildCreativeModeTabContentsEvent event) {
        CreativeModeTab tab = event.getTab();
        BaseCreativeTab.busInvoker(tab, new ICreativeModeTabOutputExtension() {
            @Override
            public void insertAfter(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
                event.insertAfter(existingEntry, newEntry, visibility);
            }

            @Override
            public void insertBefore(ItemStack existingEntry, ItemStack newEntry, CreativeModeTab.TabVisibility visibility) {
                event.insertBefore(existingEntry, newEntry, visibility);
            }

            @Override
            public void accept(ItemStack itemStack, CreativeModeTab.TabVisibility tabVisibility) {
                event.accept(itemStack, tabVisibility);
            }
        });
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
