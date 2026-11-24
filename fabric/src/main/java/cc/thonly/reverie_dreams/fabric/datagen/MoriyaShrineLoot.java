package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.item.RDWorkshopItems;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootTable;

import static cc.thonly.reverie_dreams.fabric.datagen.ChestLootTableProvider.supplies;
import static cc.thonly.reverie_dreams.fabric.datagen.GensokyoWonderLoot.*;
import static cc.thonly.reverie_dreams.fabric.datagen.GensokyoWonderLoot.Tier.*;

/** Authored themed rewards; table IDs stay compatible with existing structure containers. */
final class MoriyaShrineLoot {
    static void generateLoot(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output,
                         HolderLookup.Provider registries) {
        output.accept(key("moriya_shrine", "rest_stop"), cache(registries, SUPPLY, RDCuisineItems.WARM_RICE_BALL)
                .withPool(supplies(Items.GOLDEN_CARROT, 3, 6)));
        output.accept(key("moriya_shrine", "shore_rations"), cache(registries, SUPPLY, RDBeverageItems.BLESSING_WIND)
                .withPool(supplies(RDCuisineItems.RICE_BALL, 3, 6)));
        output.accept(key("moriya_shrine", "wayside_offerings"), cache(registries, SUPPLY, RDItems.EXORCISM_PAPER)
                .withPool(supplies(RDWorkshopItems.OMAMORI, 2, 4)));
        output.accept(key("moriya_shrine", "wind_rites"), cache(registries, CACHE, RDItems.BOUNDARY_OFUDA)
                .withPool(supplies(RDItems.EXORCISM_PAPER, 6, 12))
                .withPool(book(registries, 20, 30)));
        output.accept(key("moriya_shrine", "autumn_festival"), cache(registries, CACHE, RDBeverageItems.BLESSING_WIND)
                .withPool(supplies(Items.GOLDEN_APPLE, 1, 2))
                .withPool(supplies(RDItems.POWER, 4, 8)));
        output.accept(key("moriya_shrine", "pillar_repairs"), cache(registries, CACHE, RDItems.SILVER_INGOT)
                .withPool(supplies(Items.IRON_INGOT, 6, 12))
                .withPool(bonus(RDItems.WIND_BLESSING_CANE, .15F)));
        output.accept(key("moriya_shrine", "iron_ring_offering"), cache(registries, TREASURY, RDItems.UPGRADED_HEALTH_FRAGMENT)
                .withPool(supplies(RDItems.SILVER_COIN, 2, 4))
                .withPool(book(registries, 28, 38)));
        output.accept(key("moriya_shrine", "sanae_cache"), cache(registries, TREASURY, RDItems.WIND_BLESSING_CANE)
                .withPool(supplies(RDBeverageItems.BLESSING_WIND, 2, 4))
                .withPool(book(registries, 30, 40)));
    }

    private static ResourceKey<LootTable> key(String structure, String theme) {
        return ChestLootTableProvider.getKey("gensokyo/" + structure + "/" + theme);
    }

    private MoriyaShrineLoot() {
    }
}
