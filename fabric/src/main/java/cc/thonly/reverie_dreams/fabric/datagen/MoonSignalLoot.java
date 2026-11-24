package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

public final class MoonSignalLoot {
    private MoonSignalLoot() { }
    public static LootPool.Builder salvage() {
        return LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                .add(EmptyLootItem.emptyItem().setWeight(3))
                .add(LootItem.lootTableItem(RDItems.LUNAR_POWER_CELL).setWeight(1))
                .add(LootItem.lootTableItem(RDItems.LUNAR_SIGNAL_COIL).setWeight(2))
                .add(LootItem.lootTableItem(RDItems.LUNAR_SIGNAL_CRYSTAL).setWeight(3));
    }
    public static LootTable.Builder maintenance() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(RDItems.LUNAR_POWER_CELL)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(3))
                        .add(LootItem.lootTableItem(Items.COPPER_INGOT).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                        .add(LootItem.lootTableItem(Items.IRON_INGOT).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3))))
                        .add(LootItem.lootTableItem(Items.REDSTONE).apply(SetItemCountFunction.setCount(UniformGenerator.between(2, 5))))
                        .add(LootItem.lootTableItem(Items.QUARTZ).apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 3)))))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(LootItem.lootTableItem(Items.BREAD).apply(SetItemCountFunction.setCount(ConstantValue.exactly(3)))));
    }
}
