package cc.thonly.reverie_dreams.fabric.datagen;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

final class HakureiShrineLoot {
    static final ResourceKey<LootTable> PILGRIM_SUPPLIES =
            ChestLootTableProvider.getKey("gensokyo/hakurei_shrine/pilgrim_supplies");
    static final ResourceKey<LootTable> WARDING_SUPPLIES =
            ChestLootTableProvider.getKey("gensokyo/hakurei_shrine/warding_supplies");

    private HakureiShrineLoot() {
    }
}
