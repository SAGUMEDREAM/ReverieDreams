package cc.thonly.minecraft.item;

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

public interface ItemStackNbtImpl {
    public static final Logger LOGGER = Logger.getLogger(ItemStackNbtImpl.class.getName());
    public Tag toNbt(HolderLookup.Provider registryAccess, CompoundTag prefix);
    public Tag toNbt(HolderLookup.Provider registryAccess);
    public static Optional<ItemStack> fromNbt(RegistryAccess registryAccess, CompoundTag compoundTag) {
        RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.CODEC.parse(ops, compoundTag).resultOrPartial((error) -> {
            LOGGER.log(Level.SEVERE, String.format("Tried to load invalid item: '%s'", error));
        });
    }
}
