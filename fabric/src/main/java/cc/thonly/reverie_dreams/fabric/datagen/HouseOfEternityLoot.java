package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.content.item.RDBeverageItems;
import cc.thonly.reverie_dreams.registry.content.item.RDCuisineItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.registry.content.item.RDWorkshopItems;
import java.util.function.BiConsumer;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetPotionFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import static cc.thonly.reverie_dreams.fabric.datagen.ChestLootTableProvider.supplies;
import static cc.thonly.reverie_dreams.fabric.datagen.GensokyoWonderLoot.*;
import static cc.thonly.reverie_dreams.fabric.datagen.GensokyoWonderLoot.Tier.*;

/** Authored themed rewards; table IDs stay compatible with existing structure containers. */
final class HouseOfEternityLoot {
    static void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output,
                         HolderLookup.Provider registries) {
        output.accept(key("house_of_eternity", "bamboo_stores"), cache(registries, SUPPLY, RDItems.DANMAKU_CORE)
                .withPool(supplies(Items.BAMBOO, 16, 32)));
        output.accept(key("house_of_eternity", "rabbit_rations"), cache(registries, SUPPLY, RDCuisineItems.WARM_RICE_BALL)
                .withPool(supplies(Items.GOLDEN_CARROT, 4, 8))
                .withPool(supplies(RDBeverageItems.GREEN_TEA, 2, 3)));
        output.accept(key("house_of_eternity", "discarded_supplies"), cache(registries, SUPPLY, RDItems.BOMB_FRAGMENT)
                .withPool(supplies(RDWorkshopItems.DRIED_HERBS, 4, 8)));
        output.accept(key("house_of_eternity", "herbal_stock"), cache(registries, CACHE, RDWorkshopItems.HERBAL_POWDER)
                .withPool(supplies(RDWorkshopItems.DRIED_HERBS, 8, 16))
                .withPool(supplies(Items.GLISTERING_MELON_SLICE, 3, 6)));
        output.accept(key("house_of_eternity", "dispensary"), cache(registries, CACHE, RDItems.UPGRADED_HEALTH_FRAGMENT)
                .withPool(medicine()));
        output.accept(key("house_of_eternity", "medical_records"), cache(registries, CACHE, RDItems.LUNAR_SIGNAL_CRYSTAL)
                .withPool(book(registries, 22, 32))
                .withPool(supplies(Items.EXPERIENCE_BOTTLE, 6, 12)));
        output.accept(key("house_of_eternity", "reisen_supplies"), cache(registries, CACHE, RDItems.LUNAR_TYPE_2_MAGAZINE)
                .withPool(supplies(RDItems.LUNAR_BULLET_STANDARD, 16, 32))
                .withPool(bonus(RDItems.LUNAR_TYPE_2_PISTOL, .2F)));
        output.accept(key("house_of_eternity", "lunar_research"), cache(registries, TREASURY, RDItems.LUNAR_POWER_CELL)
                .withPool(supplies(RDItems.LUNAR_SIGNAL_CRYSTAL, 2, 4))
                .withPool(book(registries, 28, 38))
                .withPool(bonus(RDItems.LOW_GRAVITY_BOOT, .25F)));
        output.accept(key("house_of_eternity", "kaguya_treasury"), cache(registries, TREASURY, RDItems.UPGRADED_HEALTH)
                .withPool(supplies(RDItems.SILVER_COIN, 3, 6))
                .withPool(book(registries, 30, 40))
                .withPool(bonus(RDItems.HORAI_DAMA_NO_EDA, .12F)));
    }

    private static ResourceKey<LootTable> key(String structure, String theme) {
        return ChestLootTableProvider.getKey("gensokyo/" + structure + "/" + theme);
    }

    private static LootPool.Builder medicine() {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(2))
                .add(LootItem.lootTableItem(Items.POTION)
                        .apply(SetPotionFunction.setPotion(Potions.STRONG_HEALING)))
                .add(LootItem.lootTableItem(Items.POTION)
                        .apply(SetPotionFunction.setPotion(Potions.REGENERATION)));
    }

    private HouseOfEternityLoot() {
    }
}
