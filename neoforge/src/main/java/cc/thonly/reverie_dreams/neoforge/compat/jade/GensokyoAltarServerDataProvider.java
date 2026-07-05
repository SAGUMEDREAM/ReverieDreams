package cc.thonly.reverie_dreams.neoforge.compat.jade;

import cc.thonly.reverie_dreams.block.entity.GensokyoAltarBlockEntity;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.StreamServerDataProvider;

import java.util.Optional;

public class GensokyoAltarServerDataProvider implements StreamServerDataProvider<BlockAccessor, GensokyoAltarData> {
    public static final GensokyoAltarServerDataProvider INSTANCE = new GensokyoAltarServerDataProvider();

    @Override
    public @Nullable GensokyoAltarData streamData(BlockAccessor blockAccessor) {
        if (!(blockAccessor.getBlockEntity() instanceof GensokyoAltarBlockEntity entity))
            return null;
        ItemStack itemStack = entity.getInventory().getItem(8);
        return new GensokyoAltarData(itemStack.isEmpty() ? Optional.empty() : Optional.of(itemStack.copy()));
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, GensokyoAltarData> streamCodec() {
        return GensokyoAltarData.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.GENSOKYO_ALTAR_DATA_PROVIDER;
    }
}
