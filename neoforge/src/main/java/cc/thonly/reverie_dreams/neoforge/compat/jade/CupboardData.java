package cc.thonly.reverie_dreams.neoforge.compat.jade;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record CupboardData(Optional<List<ItemStack>> stacks) {
    public static final StreamCodec<RegistryFriendlyByteBuf, CupboardData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(
                            ByteBufCodecs.collection(ArrayList::new, ItemStack.STREAM_CODEC)
                    ),
                    CupboardData::stacks,
                    CupboardData::new
            );
}