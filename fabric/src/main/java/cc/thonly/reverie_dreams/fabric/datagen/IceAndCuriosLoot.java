package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetEnchantmentsFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import static cc.thonly.reverie_dreams.fabric.datagen.ChestLootTableProvider.supplies;
import static cc.thonly.reverie_dreams.fabric.datagen.GensokyoWonderLoot.*;
import static cc.thonly.reverie_dreams.fabric.datagen.GensokyoWonderLoot.Tier.*;

/** Stock and discoveries are exploration rewards, not a replacement for a future shop system. */
final class IceAndCuriosLoot {
    static void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output, HolderLookup.Provider registries) {
        output.accept(key("cirno_house", "frozen_larder"), cache(registries, SUPPLY, RDCuisineItems.MAOYU_TRICOLOR_ICE_CREAM)
                .withPool(supplies(RDBeverageItems.BIG_POPSICLE, 2, 4)));
        output.accept(key("cirno_house", "lake_supplies"), cache(registries, SUPPLY, RDItems.FAIRY_DUST)
                .withPool(supplies(Items.PACKED_ICE, 8, 16)).withPool(supplies(Items.GOLDEN_CARROT, 3, 6)));
        output.accept(key("cirno_house", "fairy_pranks"), cache(registries, CACHE, RDItems.CURSED_DECOY_DOLL)
                .withPool(supplies(Items.SNOWBALL, 8, 16)).withPool(bonus(RDItems.BOMB, .3F)));
        output.accept(key("cirno_house", "ice_study"), cache(registries, CACHE, RDItems.DANMAKU_CORE)
                .withPool(supplies(Items.BLUE_ICE, 3, 6)).withPool(book(registries, 20, 30)));
        output.accept(key("cirno_house", "strongest_treasure"), cache(registries, TREASURY, RDItems.UPGRADED_HEALTH)
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.BOOK).apply(new SetEnchantmentsFunction.Builder()
                                .withEnchantment(registries.lookupOrThrow(Registries.ENCHANTMENT)
                                        .getOrThrow(Enchantments.FROST_WALKER), ConstantValue.exactly(2)))))
                .withPool(supplies(Items.DIAMOND, 2, 4)));

        output.accept(key("kourindou", "daily_goods"), cache(registries, SUPPLY, RDItems.SILVER_INGOT)
                .withPool(supplies(Items.EXPERIENCE_BOTTLE, 6, 12)));
        output.accept(key("kourindou", "outside_world"), cache(registries, CACHE, RDItems.EARPHONE)
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.CLOCK)).add(LootItem.lootTableItem(Items.SPYGLASS))
                        .add(LootItem.lootTableItem(RDItems.HIMEKAIDOU_HATATES_PHONE))
                        .add(LootItem.lootTableItem(RDItems.TENGU_CAMERA))));
        output.accept(key("kourindou", "repair_stock"), cache(registries, SUPPLY, RDItems.LUNAR_SIGNAL_COIL)
                .withPool(supplies(Items.IRON_INGOT, 6, 12)).withPool(supplies(Items.REDSTONE, 8, 16)));
        output.accept(key("kourindou", "magic_tools"), cache(registries, CACHE, RDItems.DANMAKU_CORE)
                .withPool(bonus(RDItems.MAGIC_BROOM, .12F)).withPool(bonus(RDItems.DREAM_MIRROR, .2F)));
        output.accept(key("kourindou", "weapon_stock"), cache(registries, CACHE, RDItems.SILVER_SWORD)
                .withPool(supplies(RDItems.EXORCISM_PAPER, 6, 12)).withPool(bonus(RDItems.TENGU_SHIELD, .2F)));
        output.accept(key("kourindou", "old_books"), cache(registries, CACHE, RDItems.BOUNDARY_OFUDA)
                .withPool(book(registries, 22, 32)).withPool(supplies(Items.BOOK, 3, 6)));
        output.accept(key("kourindou", "music_collection"), cache(registries, CACHE, RDItems.VIOLIN)
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1))
                        .add(LootItem.lootTableItem(Items.MUSIC_DISC_CAT))
                        .add(LootItem.lootTableItem(Items.MUSIC_DISC_MALL))
                        .add(LootItem.lootTableItem(Items.MUSIC_DISC_WAIT)))
                .withPool(bonus(RDItems.TRUMPET, .2F)));
        output.accept(key("kourindou", "rare_curios"), cache(registries, CACHE, RDItems.DREAM_CRYSTAL_FRAGMENT)
                .withPool(bonus(RDItems.GAP_BALL, .12F)).withPool(bonus(RDItems.TIME_STOP_CLOCK, .08F)));
        output.accept(key("kourindou", "keeper_treasury"), cache(registries, TREASURY, RDItems.DREAM_MIRROR)
                .withPool(book(registries, 30, 40)).withPool(supplies(RDItems.SILVER_COIN, 3, 6))
                .withPool(bonus(Items.ELYTRA, .15F)).withPool(bonus(RDItems.TIME_STOP_CLOCK, .25F)));
    }

    private static ResourceKey<LootTable> key(String landmark, String theme) {
        return ChestLootTableProvider.getKey("gensokyo/" + landmark + "/" + theme);
    }

    private IceAndCuriosLoot() {
    }
}
