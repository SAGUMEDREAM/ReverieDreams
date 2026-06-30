package cc.thonly.reverie_dreams.registry.content.villager;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.tag.RDVillagerTradeTags;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.item.trading.TradeSets;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class RDTradeSets {
    public static final ResourceKey<TradeSet> HAWKERS_LEVEL_1 = key(RDVillagerTradeTags.BUTCHER_LEVEL_1);
    public static final ResourceKey<TradeSet> HAWKERS_LEVEL_2 = key(RDVillagerTradeTags.BUTCHER_LEVEL_1);
    public static final ResourceKey<TradeSet> HAWKERS_LEVEL_3 = key(RDVillagerTradeTags.BUTCHER_LEVEL_1);
    public static final ResourceKey<TradeSet> HAWKERS_LEVEL_4 = key(RDVillagerTradeTags.BUTCHER_LEVEL_1);
    public static final ResourceKey<TradeSet> HAWKERS_LEVEL_5 = key(RDVillagerTradeTags.BUTCHER_LEVEL_1);
    public static final ResourceKey<TradeSet> PRIEST_LEVEL_1 = key(RDVillagerTradeTags.PRIEST_LEVEL_1);
    public static final ResourceKey<TradeSet> PRIEST_LEVEL_2 = key(RDVillagerTradeTags.PRIEST_LEVEL_2);
    public static final ResourceKey<TradeSet> PRIEST_LEVEL_3 = key(RDVillagerTradeTags.PRIEST_LEVEL_3);
    public static final ResourceKey<TradeSet> PRIEST_LEVEL_4 = key(RDVillagerTradeTags.PRIEST_LEVEL_4);
    public static final ResourceKey<TradeSet> PRIEST_LEVEL_5 = key(RDVillagerTradeTags.PRIEST_LEVEL_5);
    public static final ResourceKey<TradeSet> MONEY_SHOP_CLERK_LEVEL_1 = key(RDVillagerTradeTags.MONEY_SHOP_CLERK_LEVEL_1);
    public static final ResourceKey<TradeSet> MONEY_SHOP_CLERK_LEVEL_2 = key(RDVillagerTradeTags.MONEY_SHOP_CLERK_LEVEL_2);
    public static final ResourceKey<TradeSet> MONEY_SHOP_CLERK_LEVEL_3 = key(RDVillagerTradeTags.MONEY_SHOP_CLERK_LEVEL_3);
    public static final ResourceKey<TradeSet> MONEY_SHOP_CLERK_LEVEL_4 = key(RDVillagerTradeTags.MONEY_SHOP_CLERK_LEVEL_4);
    public static final ResourceKey<TradeSet> MONEY_SHOP_CLERK_LEVEL_5 = key(RDVillagerTradeTags.MONEY_SHOP_CLERK_LEVEL_5);
    public static final ResourceKey<TradeSet> BUTCHER_LEVEL_1 = TradeSets.BUTCHER_LEVEL_1;
    public static final ResourceKey<TradeSet> BUTCHER_LEVEL_2 = TradeSets.BUTCHER_LEVEL_2;
    public static final ResourceKey<TradeSet> BUTCHER_LEVEL_3 = TradeSets.BUTCHER_LEVEL_3;
    public static final ResourceKey<TradeSet> BUTCHER_LEVEL_4 = TradeSets.BUTCHER_LEVEL_4;
    public static final ResourceKey<TradeSet> BUTCHER_LEVEL_5 = TradeSets.BUTCHER_LEVEL_5;
    public static final ResourceKey<TradeSet> FARMER_LEVEL_1 = TradeSets.FARMER_LEVEL_1;
    public static final ResourceKey<TradeSet> FARMER_LEVEL_2 = TradeSets.FARMER_LEVEL_2;
    public static final ResourceKey<TradeSet> FARMER_LEVEL_3 = TradeSets.FARMER_LEVEL_3;
    public static final ResourceKey<TradeSet> FARMER_LEVEL_4 = TradeSets.FARMER_LEVEL_4;
    public static final ResourceKey<TradeSet> FARMER_LEVEL_5 = TradeSets.FARMER_LEVEL_5;

    public static void bootstrap(BootstrapContext<TradeSet> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        Map<TagKey<VillagerTrade>, Set<ResourceKey<VillagerTrade>>> boundedTagKeys = RDVillagerTrades.getBoundedTagKeys(items, enchantments);
        boundedTagKeys.forEach((tagKey, keys) -> {
            register(context, key(tagKey), tagKey);
        });
    }

    public static Holder.Reference<TradeSet> register(BootstrapContext<TradeSet> context,
                                                      ResourceKey<TradeSet> resourceKey,
                                                      TagKey<VillagerTrade> tradeTag) {
        return register(context, resourceKey, tradeTag, ConstantValue.exactly(2.0F));
    }

    public static Holder.Reference<TradeSet> register(
            BootstrapContext<TradeSet> context,
            ResourceKey<TradeSet> resourceKey,
            TagKey<VillagerTrade> tradeTag,
            NumberProvider numberProvider
    ) {
        Identifier id = resourceKey.identifier().withPrefix("trade_set/");
        Identifier finalId = id.getNamespace().equals(Identifier.DEFAULT_NAMESPACE)
                ? ReverieDreams.id(id.getPath())
                : id;
        ResourceKey<TradeSet> finalResourceKey = resourceKey.identifier().getNamespace().equals(Identifier.DEFAULT_NAMESPACE)
                ? key(finalId)
                : resourceKey;
        return context.register(
                finalResourceKey,
                new TradeSet(
                        context.lookup(Registries.VILLAGER_TRADE).getOrThrow(tradeTag),
                        numberProvider,
                        false,
                        Optional.of(finalId)
                )
        );
    }

    public static ResourceKey<TradeSet> key(TagKey<VillagerTrade> key) {
        return ResourceKey.create(Registries.TRADE_SET, key.location());
    }

    public static ResourceKey<TradeSet> key(Identifier location) {
        return ResourceKey.create(Registries.TRADE_SET, location);
    }

    public static ResourceKey<TradeSet> key(String path) {
        return ResourceKey.create(Registries.TRADE_SET, ReverieDreams.id(path));
    }
}
