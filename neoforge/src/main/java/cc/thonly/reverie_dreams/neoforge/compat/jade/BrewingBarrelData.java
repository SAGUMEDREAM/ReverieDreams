package cc.thonly.reverie_dreams.neoforge.compat.jade;

import cc.thonly.reverie_dreams.item.IngredientStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Optional;

public record BrewingBarrelData(Optional<IngredientStack> output, int brewingTick, int maxBrewingTick, int count, int maxCount, boolean brewing) {
    public static final StreamCodec<RegistryFriendlyByteBuf, BrewingBarrelData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.optional(
                            ByteBufCodecs.fromCodec(IngredientStack.CODEC)
                    ),
                    BrewingBarrelData::output,
                    ByteBufCodecs.VAR_INT,
                    BrewingBarrelData::brewingTick,
                    ByteBufCodecs.VAR_INT,
                    BrewingBarrelData::maxBrewingTick,
                    ByteBufCodecs.VAR_INT,
                    BrewingBarrelData::count,
                    ByteBufCodecs.VAR_INT,
                    BrewingBarrelData::maxCount,
                    ByteBufCodecs.BOOL,
                    BrewingBarrelData::brewing,
                    BrewingBarrelData::new
            );
}
