package cc.thonly.reverie_dreams.registry.content.villager;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.villager.offer.*;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.trading.VillagerTrade;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

@Slf4j
public class RDVillagerTrades {
    private static final Collection<Tuple<List<ResourceKey<VillagerTrade>>, List<VillagerTrade>>> REGISTRIES = new ArrayList<>();
    private static final Map<TagKey<VillagerTrade>, Set<ResourceKey<VillagerTrade>>> BOUNDED_TAG_KEYS = new HashMap<>();
    public static final String HAWKERS_LEVEL_TEMPLATE = "hawkers_level_%s_result_item_%s";
    public static final String PRIEST_LEVEL_TEMPLATE = "priest_level_%s_result_item_%s";
    public static final String MONEY_SHOP_CLERK_TEMPLATE = "money_shop_clerk_level_%s_result_item_%s";

    public static void bootstrap(BootstrapContext<VillagerTrade> context) {
        HolderGetter<Item> items = context.lookup(Registries.ITEM);
        HolderGetter<Enchantment> enchantments = context.lookup(Registries.ENCHANTMENT);
        Collection<Tuple<List<ResourceKey<VillagerTrade>>, List<VillagerTrade>>> tuples = fillRegistries(items, enchantments);
        for (Tuple<List<ResourceKey<VillagerTrade>>, List<VillagerTrade>> tuple : tuples) {
            List<ResourceKey<VillagerTrade>> keys = tuple.getA();
            List<VillagerTrade> values = tuple.getB();
            if (keys.size() != values.size()) {
                continue;
            }
            int size = keys.size();
            for (int i = 0; i < size; i++) {
                ResourceKey<VillagerTrade> key = keys.get(i);
                VillagerTrade trade = values.get(i);
                context.register(key, trade);
            }
        }
    }

    private static Collection<Tuple<List<ResourceKey<VillagerTrade>>, List<VillagerTrade>>> fillRegistries(HolderGetter<Item> items, HolderGetter<Enchantment> enchantments) {
        REGISTRIES.clear();
        BOUNDED_TAG_KEYS.clear();
        REGISTRIES.add(PreparingTradeInfo.make(items, enchantments).register(HawkerOffers::makeOffers).build());
        REGISTRIES.add(PreparingTradeInfo.make(items, enchantments).register(PriestOffers::makeOffers).build());
        REGISTRIES.add(PreparingTradeInfo.make(items, enchantments).register(MoneyShopClerkOffers::makeOffers).build());
        REGISTRIES.add(PreparingTradeInfo.make(items, enchantments).register(MystiaModOffers::makeOffers).build());
        return List.copyOf(REGISTRIES);
    }

    public static Map<TagKey<VillagerTrade>, Set<ResourceKey<VillagerTrade>>> getBoundedTagKeys(HolderGetter<Item> items, HolderGetter<Enchantment> enchantments) {
        if (isPrepared()) {
            return Map.copyOf(BOUNDED_TAG_KEYS);
        }
        fillRegistries(items, enchantments);
        return Map.copyOf(BOUNDED_TAG_KEYS);
    }

    public static Collection<Tuple<List<ResourceKey<VillagerTrade>>, List<VillagerTrade>>> getPreparedRegistries(HolderGetter<Item> items, HolderGetter<Enchantment> enchantments) {
        return isPrepared() ? List.copyOf(REGISTRIES) : fillRegistries(items, enchantments);
    }

    public static boolean isPrepared() {
        return !REGISTRIES.isEmpty() && !BOUNDED_TAG_KEYS.isEmpty();
    }

    public static class PreparingTradeInfo {
        @Nullable
        private HolderGetter<Item> itemRegistry;
        private HolderGetter<Enchantment> enchantmentRegistry;
        private final List<ResourceKey<VillagerTrade>> keys = new ArrayList<>();
        private final List<VillagerTrade> trades = new ArrayList<>();

        public ResourceKey<VillagerTrade> keyInstance(TagKey<VillagerTrade> tagKey, String template, int level, ItemLike result) {
            return this.keyInstance(ReverieDreams.MOD_ID, tagKey, template, level, result);
        }

        public ResourceKey<VillagerTrade> keyInstance(String namespace, TagKey<VillagerTrade> tagKey, String template, int level, ItemLike result) {
            Identifier itemId = BuiltInRegistries.ITEM.getKey(result.asItem());
            String itemIdName = "%s_%s".formatted(itemId.getNamespace(), itemId.getPath());
            ResourceKey<VillagerTrade> key = ResourceKey.create(
                    Registries.VILLAGER_TRADE,
                    Identifier.fromNamespaceAndPath(namespace, template.formatted(level, itemIdName))
            );
            Set<ResourceKey<VillagerTrade>> keys = BOUNDED_TAG_KEYS.computeIfAbsent(tagKey, tk -> new LinkedHashSet<>());
            keys.add(key);
            return key;
        }

        public PreparingTradeInfo add(ResourceKey<VillagerTrade> key, VillagerTrade trade) {
            this.keys.add(key);
            this.trades.add(trade);
            return this;
        }

        public @NotNull HolderGetter<Item> getItemRegistry() {
            return Objects.requireNonNull(
                    this.itemRegistry,
                    "Enchantment registry is null"
            );
        }

        public @NotNull HolderGetter<Enchantment> getEnchantmentRegistry() {
            return Objects.requireNonNull(
                    this.enchantmentRegistry,
                    "Enchantment registry is null"
            );
        }

        public Tuple<List<ResourceKey<VillagerTrade>>, List<VillagerTrade>> build() {
            return new Tuple<>(this.keys, this.trades);
        }

        public PreparingTradeInfo register(Consumer<PreparingTradeInfo> consumer) {
            consumer.accept(this);
            return this;
        }

        public static PreparingTradeInfo make(HolderGetter<Item> items) {
            PreparingTradeInfo info = new PreparingTradeInfo();
            info.itemRegistry = items;
            return info;
        }

        public static PreparingTradeInfo make(HolderGetter<Item> items, HolderGetter<Enchantment> enchantments) {
            PreparingTradeInfo info = new PreparingTradeInfo();
            info.itemRegistry = items;
            info.enchantmentRegistry = enchantments;
            return info;
        }

        public static PreparingTradeInfo make(Consumer<PreparingTradeInfo> consumer) {
            PreparingTradeInfo info = new PreparingTradeInfo();
            consumer.accept(info);
            return info;
        }

        public static PreparingTradeInfo make() {
            return new PreparingTradeInfo();
        }
    }

    public static ResourceKey<VillagerTrade> key(String name) {
        return ResourceKey.create(Registries.VILLAGER_TRADE, ReverieDreams.id(name));
    }
}