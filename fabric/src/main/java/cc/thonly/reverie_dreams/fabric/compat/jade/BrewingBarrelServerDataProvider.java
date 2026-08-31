package cc.thonly.reverie_dreams.fabric.compat.jade;

import cc.thonly.reverie_dreams.block.entity.BrewingBarrelBlockEntity;
import cc.thonly.reverie_dreams.item.IngredientStack;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.StreamServerDataProvider;

import java.util.Optional;

public class BrewingBarrelServerDataProvider implements StreamServerDataProvider<BlockAccessor, BrewingBarrelData> {
    public static final BrewingBarrelServerDataProvider INSTANCE = new BrewingBarrelServerDataProvider();


    @Override
    public @Nullable BrewingBarrelData streamData(BlockAccessor blockAccessor) {
        if (!(blockAccessor.getBlockEntity() instanceof BrewingBarrelBlockEntity entity))
            return null;
        Optional<IngredientStack> output = entity.getOutput();
        int brewingTick = entity.getBrewingTick();
        int maxBrewingTick = entity.getMaxBrewingTick();
        boolean brewing = entity.isBrewing();
        return new BrewingBarrelData(output, brewingTick, maxBrewingTick, entity.getCount(), BrewingBarrelBlockEntity.MAX_COUNT, brewing);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, BrewingBarrelData> streamCodec() {
        return BrewingBarrelData.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.BREWING_BARREL_PROVIDER;
    }
}
