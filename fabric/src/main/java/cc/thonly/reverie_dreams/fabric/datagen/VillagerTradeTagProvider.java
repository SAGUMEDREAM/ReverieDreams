package cc.thonly.reverie_dreams.fabric.datagen;

import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviderKeys;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.villager.RDVillagerTrades;
import cc.thonly.reverie_dreams.util.trading.VillagerTrade;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class VillagerTradeTagProvider extends FabricTagProvider<VillagerTrade> {
    public VillagerTradeTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
        super(output, BuiltInRegistryProviderKeys.VILLAGER_TRADE, registriesFuture);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        HolderLookup.RegistryLookup<Item> items = provider.lookupOrThrow(Registries.ITEM);
        HolderLookup.RegistryLookup<Enchantment> enchantments = provider.lookupOrThrow(Registries.ENCHANTMENT);
        HolderLookup.RegistryLookup<VillagerTrade> villagerTrades = provider.lookupOrThrow(BuiltInRegistryProviderKeys.VILLAGER_TRADE);
        Map<TagKey<VillagerTrade>, Set<ResourceKey<VillagerTrade>>> boundedTagKeys = RDVillagerTrades.getBoundedTagKeys(items, enchantments);
        boundedTagKeys.forEach((tagKey, keys) -> {
            TagAppender<ResourceKey<VillagerTrade>, VillagerTrade> builder = builder(tagKey);
            for (ResourceKey<VillagerTrade> key : keys) {
                builder.add(key);
            }
        });
    }
}
