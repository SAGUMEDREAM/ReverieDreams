package cc.thonly.reverie_dreams.fabric.compat.jade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record KitchenwareData(Optional<ItemStack> target, int cookingTime, int maxCookingTime) {
    public static final StreamCodec<RegistryFriendlyByteBuf, KitchenwareData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC),
            KitchenwareData::target,
            ByteBufCodecs.VAR_INT,
            KitchenwareData::cookingTime,
            ByteBufCodecs.VAR_INT,
            KitchenwareData::maxCookingTime,
            KitchenwareData::new
    );
}
