package cc.thonly.reverie_dreams.util.trading;

import cc.thonly.keine.item.ItemStackTemplate;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class VillagerTrade {
    public static final Codec<VillagerTrade> CODEC = RecordCodecBuilder.create(
            (i) -> i.group(
                    TradeCost.CODEC.fieldOf("wants").forGetter((villagerTrade) -> villagerTrade.wants),
                    TradeCost.CODEC.optionalFieldOf("additional_wants").forGetter((villagerTrade) -> villagerTrade.additionalWants),
                    ItemStackTemplate.CODEC.fieldOf("gives").forGetter((villagerTrade) -> villagerTrade.gives),
                    NumberProviders.CODEC.lenientOptionalFieldOf("max_uses", ConstantValue.exactly(4.0F)).forGetter((villagerTrade) -> villagerTrade.maxUses),
                    NumberProviders.CODEC.lenientOptionalFieldOf("reputation_discount", ConstantValue.exactly(0.0F)).forGetter((villagerTrade) -> villagerTrade.reputationDiscount),
                    NumberProviders.CODEC.lenientOptionalFieldOf("xp", ConstantValue.exactly(1.0F)).forGetter((villagerTrade) -> villagerTrade.xp),
                    LootItemCondition.DIRECT_CODEC.optionalFieldOf("merchant_predicate").forGetter((villagerTrade) -> villagerTrade.merchantPredicate),
                    LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("given_item_modifiers", List.of()).forGetter((villagerTrade) -> villagerTrade.givenItemModifiers),
                    RegistryCodecs.homogeneousList(Registries.ENCHANTMENT).optionalFieldOf("double_trade_price_enchantments").forGetter((villagerTrade) -> villagerTrade.doubleTradePriceEnchantments)
            ).apply(i, VillagerTrade::new));
    private final TradeCost wants;
    private final Optional<TradeCost> additionalWants;
    private final ItemStackTemplate gives;
    private final Optional<LootItemCondition> merchantPredicate;
    private final List<LootItemFunction> givenItemModifiers;
    private final NumberProvider maxUses;
    private final NumberProvider reputationDiscount;
    private final NumberProvider xp;
    private final Optional<HolderSet<Enchantment>> doubleTradePriceEnchantments;

    private VillagerTrade(TradeCost wants, Optional<TradeCost> additionalWants, ItemStackTemplate gives, NumberProvider maxUses, NumberProvider reputationDiscount, NumberProvider xp, Optional<LootItemCondition> merchantPredicate, List<LootItemFunction> givenItemModifiers, Optional<HolderSet<Enchantment>> doubleTradePriceEnchantments) {
        this.wants = wants;
        this.additionalWants = additionalWants;
        this.gives = gives;
        this.maxUses = maxUses;
        this.reputationDiscount = reputationDiscount;
        this.xp = xp;
        this.merchantPredicate = merchantPredicate;
        this.givenItemModifiers = givenItemModifiers;
        this.doubleTradePriceEnchantments = doubleTradePriceEnchantments;
    }

    public VillagerTrade(TradeCost wants, Optional<TradeCost> additionalWants, ItemStackTemplate gives, int maxUses, int xp, float reputationDiscount, Optional<LootItemCondition> merchantPredicate, List<LootItemFunction> givenItemModifiers, Optional<HolderSet<Enchantment>> doubleTradePriceEnchantments) {
        this(wants, additionalWants, gives, ConstantValue.exactly((float)maxUses), ConstantValue.exactly(reputationDiscount), ConstantValue.exactly((float)xp), merchantPredicate, givenItemModifiers, doubleTradePriceEnchantments);
    }

    public VillagerTrade(TradeCost wants, Optional<TradeCost> additionalWants, ItemStackTemplate gives, int maxUses, int xp, float reputationDiscount, Optional<LootItemCondition> merchantPredicate, List<LootItemFunction> givenItemModifiers) {
        this(wants, additionalWants, gives, ConstantValue.exactly((float)maxUses), ConstantValue.exactly(reputationDiscount), ConstantValue.exactly((float)xp), merchantPredicate, givenItemModifiers, Optional.empty());
    }

    public VillagerTrade(TradeCost wants, ItemStackTemplate gives, int maxUses, int xp, float reputationDiscount, Optional<LootItemCondition> merchantPredicate, List<LootItemFunction> givenItemModifiers) {
        this(wants, Optional.empty(), gives, ConstantValue.exactly((float)maxUses), ConstantValue.exactly(reputationDiscount), ConstantValue.exactly((float)xp), merchantPredicate, givenItemModifiers, Optional.empty());
    }

    public @Nullable MerchantOffer getOffer(LootContext lootContext) {
        if (this.merchantPredicate.isPresent() && !((LootItemCondition)this.merchantPredicate.get()).test(lootContext)) {
            return null;
        } else {
            ItemStack result = this.gives.create();
            int additionalCost = 0;

            for(LootItemFunction outputItemModifier : this.givenItemModifiers) {
                result = (ItemStack)outputItemModifier.apply(result, lootContext);
                if (result.isEmpty()) {
                    return null;
                }
            }

            Integer additionalTradeCost = result.remove(RDDataComponentTypes.ADDITIONAL_TRADE_COST.get());
            if (additionalTradeCost != null) {
                additionalCost += additionalTradeCost;
            }

            if (this.doubleTradePriceEnchantments.isPresent()) {
                HolderSet<Enchantment> enchantments = this.doubleTradePriceEnchantments.get();
                ItemEnchantments itemEnchantments = result.get(DataComponents.STORED_ENCHANTMENTS);
                if (itemEnchantments != null) {
                    Stream<Holder<Enchantment>> stream = itemEnchantments.keySet().stream();
                    if (stream.anyMatch(enchantments::contains)) {
                        additionalCost *= 2;
                    }
                }
            }

            ItemCost itemCost = this.wants.toItemCost(lootContext, additionalCost);
            if (itemCost.count() < 1) {
                return null;
            } else {
                Optional<ItemCost> additionalItemCost = this.additionalWants.map((tradeCost) -> tradeCost.toItemCost(lootContext, 0));
                return additionalItemCost.isPresent() && (additionalItemCost.get()).count() < 1 ? null : new MerchantOffer(itemCost, additionalItemCost, result, Math.max(this.maxUses.getInt(lootContext), 1), Math.max(this.xp.getInt(lootContext), 0), Math.max(this.reputationDiscount.getFloat(lootContext), 0.0F));
            }
        }
    }

}
