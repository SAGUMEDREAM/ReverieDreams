package cc.thonly.reverie_dreams.datagen;

import cc.thonly.mystias_izakaya.entity.MIEntities;
import cc.thonly.mystias_izakaya.item.MIItems;
import cc.thonly.reverie_dreams.entity.ModEntities;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricEntityLootTableProvider;
import net.minecraft.item.Items;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.condition.RandomChanceLootCondition;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.function.SetCountLootFunction;
import net.minecraft.loot.provider.number.ConstantLootNumberProvider;
import net.minecraft.loot.provider.number.UniformLootNumberProvider;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

public class ModEntityLootTableProvider extends FabricEntityLootTableProvider {
    public ModEntityLootTableProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, registryLookup);
    }

    @Override
    public void generate() {
        this.register(MIEntities.WILD_PIG_ENTITY_TYPE,
                new LootTable.Builder()
                        .pool(new LootPool.Builder()
                                .rolls(UniformLootNumberProvider.create(1.0f, 3.0f))
                                .with(ItemEntry.builder(MIItems.WILD_BOAR_MEAT))
                        )
        );
        this.register(ModEntities.MOON_RABBIT_ENTITY_TYPE,
                new LootTable.Builder()
                        .pool(new LootPool.Builder()
                                .rolls(ConstantLootNumberProvider.create(1.0f))
                                .with(ItemEntry.builder(Items.RABBIT))
                        )
                        .pool(new LootPool.Builder()
                                .rolls(ConstantLootNumberProvider.create(0.8f))
                                .with(ItemEntry.builder(Items.RABBIT_FOOT))
                        )
                        .pool(LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(1))
                                .conditionally(RandomChanceLootCondition.builder(0.4f))
                                .with(ItemEntry.builder(Items.GOLD_NUGGET)
                                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1f, 3f)))
                                )
                        ).pool(LootPool.builder()
                                .rolls(ConstantLootNumberProvider.create(1))
                                .conditionally(RandomChanceLootCondition.builder(0.1f))
                                .with(ItemEntry.builder(MIItems.MOONLIGHT_DUMPLINGS)
                                        .apply(SetCountLootFunction.builder(UniformLootNumberProvider.create(1f, 3f)))
                                )
                        )
        );
    }
}
