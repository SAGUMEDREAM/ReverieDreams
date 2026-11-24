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
final class CastleSanctuaryLoot {
    static void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output,
                         HolderLookup.Provider registries) {
        output.accept(key("shining_needle_castle", "retainer_rations"), cache(registries, SUPPLY, RDCuisineItems.WARM_RICE_BALL)
                .withPool(supplies(RDBeverageItems.GREEN_TEA, 2, 4)));
        output.accept(key("shining_needle_castle", "sewing_supplies"), cache(registries, SUPPLY, RDItems.SILVER_INGOT)
                .withPool(supplies(Items.STRING, 8, 16)));
        output.accept(key("shining_needle_castle", "castle_repairs"), cache(registries, SUPPLY, RDItems.DANMAKU_CORE)
                .withPool(supplies(Items.IRON_INGOT, 4, 8)));
        output.accept(key("shining_needle_castle", "discarded_tricks"), cache(registries, CACHE, RDItems.CURSED_DECOY_DOLL)
                .withPool(bonus(RDItems.BOUNDARY_OFUDA, .35F)));
        output.accept(key("shining_needle_castle", "guard_stores"), cache(registries, CACHE, RDItems.BOMB)
                .withPool(supplies(Items.SPECTRAL_ARROW, 12, 24)));
        output.accept(key("shining_needle_castle", "tsukumogami_parts"), cache(registries, CACHE, RDItems.DANMAKU_CORE)
                .withPool(bonus(RDItems.VIOLIN, .25F)));
        output.accept(key("shining_needle_castle", "rebellion_notes"), cache(registries, CACHE, RDItems.BOUNDARY_OFUDA)
                .withPool(book(registries, 22, 32)));
        output.accept(key("shining_needle_castle", "needle_treasury"), cache(registries, TREASURY, RDItems.CROSSING_CHISEL)
                .withPool(supplies(RDItems.SILVER_COIN, 3, 6))
                .withPool(book(registries, 30, 40)));
        output.accept(key("shining_needle_castle", "mallet_remnants"), cache(registries, TREASURY, RDItems.UPGRADED_HEALTH)
                .withPool(supplies(RDItems.POWER, 6, 12))
                .withPool(bonus(RDItems.CURSED_DECOY_DOLL, .3F)));
        output.accept(key("sacred_asanagi_mountain", "pilgrim_supplies"), cache(registries, SUPPLY, RDCuisineItems.WARM_RICE_BALL)
                .withPool(supplies(Items.GOLDEN_CARROT, 4, 8)));
        output.accept(key("sacred_asanagi_mountain", "ritual_supplies"), cache(registries, SUPPLY, RDItems.EXORCISM_PAPER)
                .withPool(supplies(RDWorkshopItems.OFUDA, 4, 8)));
        output.accept(key("sacred_asanagi_mountain", "four_seasons"), cache(registries, CACHE, RDItems.DANMAKU_CORE)
                .withPool(supplies(Items.EXPERIENCE_BOTTLE, 8, 16)));
        output.accept(key("sacred_asanagi_mountain", "labyrinth_provisions"), cache(registries, CACHE, RDItems.BOUNDARY_OFUDA)
                .withPool(supplies(Items.ENDER_PEARL, 3, 6)));
        output.accept(key("sacred_asanagi_mountain", "purification_materials"), cache(registries, CACHE, RDItems.LUNAR_SIGNAL_CRYSTAL)
                .withPool(supplies(RDWorkshopItems.HERBAL_POWDER, 4, 8)));
        output.accept(key("sacred_asanagi_mountain", "lunar_maintenance"), cache(registries, CACHE, RDItems.LUNAR_POWER_CELL)
                .withPool(supplies(RDItems.LUNAR_SIGNAL_COIL, 2, 4))
                .withPool(bonus(RDItems.LOW_GRAVITY_BOOT, .18F)));
        output.accept(key("sacred_asanagi_mountain", "fossil_samples"), cache(registries, CACHE, RDItems.DANMAKU_CORE)
                .withPool(supplies(Items.DIAMOND, 2, 4)));
        output.accept(key("sacred_asanagi_mountain", "buried_shrine"), cache(registries, TREASURY, RDItems.UPGRADED_HEALTH)
                .withPool(supplies(RDWorkshopItems.OMAMORI, 2, 4))
                .withPool(book(registries, 30, 40)));
        output.accept(key("sacred_asanagi_mountain", "sealed_archive"), cache(registries, TREASURY, RDItems.LUNAR_SIGNAL_CRYSTAL)
                .withPool(book(registries, 30, 40))
                .withPool(bonus(RDItems.LOW_GRAVITY_BOOT, .35F)));
    }

    private static ResourceKey<LootTable> key(String structure, String theme) {
        return ChestLootTableProvider.getKey("gensokyo/" + structure + "/" + theme);
    }

    private CastleSanctuaryLoot() {
    }
}
