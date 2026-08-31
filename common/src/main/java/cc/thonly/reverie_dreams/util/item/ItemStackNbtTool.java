package cc.thonly.reverie_dreams.util.item;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface ItemStackNbtTool {
    Logger LOGGER = Logger.getLogger(ItemStackNbtTool.class.getName());
    Tag toNbt(HolderLookup.Provider registryAccess, CompoundTag prefix);
    Tag toNbt(HolderLookup.Provider registryAccess);
    static Optional<ItemStack> fromNbt(RegistryAccess registryAccess, CompoundTag compoundTag) {
        RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.CODEC.parse(ops, compoundTag).resultOrPartial((error) -> {
            LOGGER.log(Level.SEVERE, String.format("Tried to load invalid item: '%s'", error));
        });
    }
}
