package cc.thonly.reverie_dreams;

import cc.thonly.polymer.*;
import cc.thonly.reverie_dreams.block.AbstractBlockCreator;
import cc.thonly.reverie_dreams.block.ChestBlockCreator;
import cc.thonly.reverie_dreams.block.WoodCreator;
import cc.thonly.reverie_dreams.block.entity.ModBlockEntities;
import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import cc.thonly.reverie_dreams.creative_tab.CreativeTabs;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.danmaku.DanmakuTypes;
import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.entity.villager.ModVillagerProfessions;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.core.api.entity.PolymerVillagerProfession;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.extras.api.ResourcePackExtras;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.api.registry.FuelRegistryEvents;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.Item;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LateLoaderInit implements ModInitializer {
    public static final List<Runnable> LATE_INIT = new ArrayList<>();
    public static final String POLYMER_MOD_ID = "reverie_dreams_polymerify";

    @Override
    public void onInitialize() {
        for (WoodCreator instance : WoodCreator.INSTANCES) {
            FlammableBlockRegistry.getDefaultInstance().add(instance.log(), 5, 20);
            FlammableBlockRegistry.getDefaultInstance().add(instance.strippedLog(), 5, 20);
            FlammableBlockRegistry.getDefaultInstance().add(instance.wood(), 5, 20);
            FlammableBlockRegistry.getDefaultInstance().add(instance.strippedWood(), 5, 20);
            FlammableBlockRegistry.getDefaultInstance().add(instance.planks(), 5, 20);
            FlammableBlockRegistry.getDefaultInstance().add(instance.stairs(), 5, 20);
            FlammableBlockRegistry.getDefaultInstance().add(instance.slab(), 5, 20);
            FlammableBlockRegistry.getDefaultInstance().add(instance.fence(), 5, 20);
            FlammableBlockRegistry.getDefaultInstance().add(instance.fenceGate(), 5, 20);
            FuelRegistryEvents.BUILD.register((builder, context) -> {
                builder.add(instance.fence(), 300);
                builder.add(instance.fenceGate(), 300);
            });
        }
        for (ChestBlockCreator chestBlockCreator : ChestBlockCreator.INSTANCES.get(ChestBlockCreator.class).stream().map((ab)->(ChestBlockCreator)ab).toList()) {
            ModBlockEntities.CUSTOM_CHEST_BLOCK_ENTITY.addSupportedBlock(chestBlockCreator.chestBlock());
        }
        CreativeTabs.registerItemGroups();
        this.polymerify();
    }

    public void polymerify() {
        for (Holder<MobEffect> registryEntry : ModStatusEffects.REVERIE_DREAMS_EFFECTS) {
            PolymerStatusEffectHelper.registerOverlay(registryEntry);
        }
        for (Item item : ModGuiItems.GUI_ITEM_LIST) {
            PolymerItemHelper.registerOverlay(item);
        }
        for (DanmakuType danmakuType : RegistryManager.DANMAKU_TYPE) {
            PolymerItemHelper.registerOverlay(danmakuType.getItem());
        }
        PolymerEntityHelper.bootstrap();
        PolymerVillagerProfessionHelper.bootstrap();

        LATE_INIT.forEach(new Consumer<Runnable>() {
            @Override
            public void accept(Runnable runnable) {
                try {
                    runnable.run();
                } catch (Exception ignored) {

                }
            }
        });

        LATE_INIT.clear();
        PolymerResourcePackUtils.addModAssets(Touhou.MOD_ID);
        PolymerResourcePackUtils.addModAssets(POLYMER_MOD_ID);
        PolymerResourcePackUtils.markAsRequired();
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                Touhou.id("block"),
                Touhou.id("item"),
                Touhou.id("entity"),
                Touhou.id("font")
        );
        ResourcePackExtras.forDefault().addBridgedModelsFolder(
                id("block"),
                id("item"),
                id("entity"),
                id("font")
        );
        ResourcePackGenerator.setup();
    }

    public static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(POLYMER_MOD_ID, name);
    }
}
