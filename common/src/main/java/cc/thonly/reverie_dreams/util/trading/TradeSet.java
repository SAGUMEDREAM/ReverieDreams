package cc.thonly.reverie_dreams.util.trading;

import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviderKeys;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

@SuppressWarnings({"LombokGetterMayBeUsed", "OptionalUsedAsFieldOrParameterType"})
public class TradeSet {
    public static final Codec<TradeSet> CODEC = RecordCodecBuilder.create((i) -> i.group(RegistryCodecs.homogeneousList(BuiltInRegistryProviderKeys.VILLAGER_TRADE).fieldOf("trades").forGetter((tradeSet) -> tradeSet.trades), NumberProviders.CODEC.fieldOf("amount").forGetter((tradeSet) -> tradeSet.amount), Codec.BOOL.optionalFieldOf("allow_duplicates", false).forGetter((tradeSet) -> tradeSet.allowDuplicates), Identifier.CODEC.optionalFieldOf("random_sequence").forGetter((t) -> t.randomSequence)).apply(i, TradeSet::new));
    private final HolderSet<VillagerTrade> trades;
    private final NumberProvider amount;
    private final boolean allowDuplicates;
    private final Optional<Identifier> randomSequence;

    public TradeSet(HolderSet<VillagerTrade> trades, NumberProvider amount, boolean allowDuplicates, Optional<Identifier> randomSequence) {
        this.trades = trades;
        this.amount = amount;
        this.allowDuplicates = allowDuplicates;
        this.randomSequence = randomSequence;
    }

    public HolderSet<VillagerTrade> getTrades() {
        return this.trades;
    }

    public int calculateNumberOfTrades(LootContext lootContext) {
        return this.amount.getInt(lootContext);
    }

    public boolean allowDuplicates() {
        return this.allowDuplicates;
    }

    public Optional<Identifier> randomSequence() {
        return this.randomSequence;
    }
}
