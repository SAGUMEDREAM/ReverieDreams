package cc.thonly.reverie_dreams.api.loot;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

public interface LootContextModifier {
    @Nullable LootTable reverie_dreams$getLootTableSource();

    void reverie_dreams$setLootTableSource(LootTable lootTable);

    @Nullable ResourceKey<LootTable> reverie_dreams$getLootTableKey();

    void reverie_dreams$setLootTableId(ResourceKey<LootTable> key);

    static ResourceKey<LootTable> getLootTableKey(LootContext context) {
        LootContextModifier iLootContext = (LootContextModifier) context;
        return iLootContext.reverie_dreams$getLootTableKey();
    }
}
