package cc.thonly.reverie_dreams.fabric.compat.jade;

import cc.thonly.reverie_dreams.block.entity.CupboardBlockEntity;
import cc.thonly.reverie_dreams.inventory.InfiniteInventory;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.StreamServerDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CupboardServerDataProvider implements StreamServerDataProvider<BlockAccessor, CupboardData> {
    public static final CupboardServerDataProvider INSTANCE = new CupboardServerDataProvider();

    @Override
    public @Nullable CupboardData streamData(BlockAccessor blockAccessor) {
        if (!(blockAccessor.getBlockEntity() instanceof CupboardBlockEntity entity))
            return null;
        List<ItemStack> stacks = new ArrayList<>();
        InfiniteInventory inventory = entity.getInventory();
        List<InfiniteInventory.Entry> entries = inventory.getEntries();
        for (InfiniteInventory.Entry entry : entries) {
            entry.itemStack().ifPresent(stack->stacks.add(stack.copy()));
        }

        return new CupboardData(Optional.of(stacks));
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, CupboardData> streamCodec() {
        return CupboardData.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return JadePlugin.CUPBOARD_PROVIDER;
    }
}
