package cc.thonly.reverie_dreams.fabric.compat.jade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record GensokyoAltarData(Optional<ItemStack> itemStack) {
    public static final StreamCodec<RegistryFriendlyByteBuf, GensokyoAltarData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ItemStack.STREAM_CODEC),
            GensokyoAltarData::itemStack,
            GensokyoAltarData::new
    );
}