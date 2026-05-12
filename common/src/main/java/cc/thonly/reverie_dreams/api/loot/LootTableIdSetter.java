package cc.thonly.reverie_dreams.api.loot;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public interface LootTableIdSetter {
    ResourceKey<LootTable> reverie_dreams$getLootTableId();
    void reverie_dreams$setLootTableId(ResourceKey<LootTable> key);
}
