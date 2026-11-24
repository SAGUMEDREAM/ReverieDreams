package cc.thonly.reverie_dreams.world.dream;

import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolSingletonContainer;
import net.minecraft.world.level.storage.loot.functions.EnchantWithLevelsFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * First-batch travel supplies and themed rewards; the old trial-room table stays separate.
 */
public final class DreamChestLoot {
    public static final ResourceKey<LootTable> SUPPLY = key("traveler_supply");
    public static final ResourceKey<LootTable> CRYSTAL = key("crystal_reward");
    public static final ResourceKey<LootTable> GARDEN = key("garden_reward");
    public static final ResourceKey<LootTable> ARCHIVE = key("archive_reward");
    public static final ResourceKey<LootTable> ARCHIVE_ALCOVE = key("archive_alcove_reward");
    public static final List<ResourceKey<LootTable>> TABLES = List.of(SUPPLY, CRYSTAL, GARDEN, ARCHIVE, ARCHIVE_ALCOVE);

    private static ResourceKey<LootTable> key(String name) {
        return ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath("reverie_dreams", "chest/dream/" + name));
    }

    /**
     * Independent of iteration order, neighbouring chunks and first-opening player. Zero is vanilla's unseeded sentinel.
     */
    public static long seed(long worldSeed, BlockPos pos, ResourceKey<LootTable> table) {
        long value = worldSeed ^ pos.asLong() ^ 0x647265616D636865L;
        for (char c : table.identifier().toString().toCharArray())
            value = (value ^ c) * 0x100000001b3L;
        value = (value ^ (value >>> 30)) * 0xbf58476d1ce4e5b9L;
        value = (value ^ (value >>> 27)) * 0x94d049bb133111ebL;
        value ^= value >>> 31;
        return value == 0 ? 1 : value;
    }

    public static void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output, HolderLookup.Provider registries) {
        output.accept(SUPPLY, table()
                .withPool(once().add(stack(Items.BREAD, 2, 4, 3)).add(stack(Items.BAKED_POTATO, 3, 5, 2)))
                .withPool(once().add(stack(Items.TORCH, 6, 10, 1)))
                .withPool(once().add(stack(Items.OAK_PLANKS, 8, 16, 1)).add(stack(Items.COBBLESTONE, 8, 16, 1)))
                .withPool(once().add(stack(RDItems.COPPER_COIN, 1, 3, 1))));
        output.accept(CRYSTAL, reward(2, 4)
                .withPool(once().add(stack(Items.AMETHYST_SHARD, 3, 6, 1)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                        .add(stack(Items.QUARTZ, 3, 6, 3)).add(stack(Items.AMETHYST_BLOCK, 1, 2, 2))
                        .add(stack(Items.IRON_INGOT, 1, 2, 1))));
        output.accept(GARDEN, reward(1, 3)
                .withPool(once().add(stack(Items.CHERRY_SAPLING, 1, 2, 1)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                        .add(stack(Items.PINK_PETALS, 4, 8, 3)).add(stack(Items.HONEY_BOTTLE, 1, 2, 2))
                        .add(stack(Items.PUMPKIN_PIE, 1, 2, 2))));
        output.accept(ARCHIVE, reward(1, 3)
                .withPool(once().add(stack(Items.BOOK, 2, 4, 1)))
                .withPool(LootPool.lootPool().setRolls(UniformGenerator.between(1, 2))
                        .add(stack(Items.PAPER, 4, 8, 3)).add(stack(Items.INK_SAC, 1, 3, 2))
                        .add(stack(Items.FEATHER, 1, 3, 2)).add(stack(Items.CANDLE, 1, 2, 1))));
        output.accept(ARCHIVE_ALCOVE, reward(2, 4)
                .withPool(once().add(LootItem.lootTableItem(Items.BOOK)
                        .apply(EnchantWithLevelsFunction.enchantWithLevels(registries, UniformGenerator.between(6, 12)))))
                .withPool(once().add(stack(Items.PAPER, 3, 6, 1))));
    }

    private static LootTable.Builder table() {
        return LootTable.lootTable().setParamSet(LootContextParamSets.CHEST);
    }

    private static LootPool.Builder once() {
        return LootPool.lootPool().setRolls(ConstantValue.exactly(1));
    }

    private static LootTable.Builder reward(int min, int max) {
        return table().withPool(once().add(stack(RDItems.DREAM_CRYSTAL_FRAGMENT, min, max, 1)))
                .withPool(once().add(stack(RDItems.COPPER_COIN, 2, 5, 1)));
    }

    private static LootPoolSingletonContainer.Builder<?> stack(ItemLike item, int min, int max, int weight) {
        return LootItem.lootTableItem(item).setWeight(weight)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(min, max)));
    }

    private DreamChestLoot() {
    }
}
