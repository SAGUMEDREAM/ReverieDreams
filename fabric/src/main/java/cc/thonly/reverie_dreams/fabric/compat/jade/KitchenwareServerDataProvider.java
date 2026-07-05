package cc.thonly.reverie_dreams.fabric.compat.jade;

import cc.thonly.reverie_dreams.block.entity.KitchenwareBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.StreamServerDataProvider;

import java.util.Optional;

public class KitchenwareServerDataProvider implements StreamServerDataProvider<BlockAccessor, KitchenwareData> {

    public static final KitchenwareServerDataProvider INSTANCE = new KitchenwareServerDataProvider();

    @Override
    public @Nullable KitchenwareData streamData(BlockAccessor blockAccessor) {
        if (!(blockAccessor.getBlockEntity() instanceof KitchenwareBlockEntity entity))
            return null;
        return new KitchenwareData(
                entity.getPreOutput().isEmpty() ? Optional.empty() : Optional.of(entity.getPreOutput().build()),
                entity.getTickLeft().intValue(),
                entity.getTickMax().intValue()
        );
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, KitchenwareData> streamCodec() {
        return KitchenwareData.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.KITCHENWARE_DATA_PROVIDER;
    }
}
