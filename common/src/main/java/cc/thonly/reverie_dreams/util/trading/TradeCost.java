package cc.thonly.reverie_dreams.util.trading;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

@SuppressWarnings("deprecation")
public record TradeCost(Holder<Item> item, NumberProvider count, DataComponentExactPredicate components) {
    public static final Codec<TradeCost> CODEC = RecordCodecBuilder.create((i) -> i.group(Item.CODEC.fieldOf("id").forGetter(TradeCost::item), NumberProviders.CODEC.optionalFieldOf("count", ConstantValue.exactly(1.0F)).forGetter(TradeCost::count), DataComponentExactPredicate.CODEC.optionalFieldOf("components", DataComponentExactPredicate.EMPTY).forGetter(TradeCost::components)).apply(i, TradeCost::new));

    public TradeCost(ItemLike item, int count) {
        this(item.asItem().builtInRegistryHolder(), ConstantValue.exactly((float)count), DataComponentExactPredicate.EMPTY);
    }

    public TradeCost(ItemLike item, NumberProvider count) {
        this(item.asItem().builtInRegistryHolder(), count, DataComponentExactPredicate.EMPTY);
    }

    public ItemCost toItemCost(LootContext lootContext, int additionalCost) {
        int count = Mth.clamp(this.count().getInt(lootContext) + additionalCost, 0, ((Item)this.item().value()).getDefaultMaxStackSize());
        return new ItemCost(this.item(), count, this.components());
    }

}
