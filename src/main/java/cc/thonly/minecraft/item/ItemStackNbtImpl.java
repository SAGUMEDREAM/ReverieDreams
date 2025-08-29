package cc.thonly.minecraft.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface ItemStackNbtImpl {
    public static final  Logger LOGGER = Logger.getLogger(ItemStackNbtImpl.class.getName());
    public NbtElement toNbt(RegistryWrapper.WrapperLookup registryAccess, NbtCompound prefix);
    public NbtElement toNbt(RegistryWrapper.WrapperLookup registryAccess);
    public static Optional<ItemStack> fromNbt(DynamicRegistryManager registryAccess, NbtCompound compoundTag) {
        RegistryOps<NbtElement> ops = registryAccess.getOps(NbtOps.INSTANCE);
        return ItemStack.CODEC.parse(ops, compoundTag).resultOrPartial((error) -> {
            LOGGER.log(Level.SEVERE, String.format("Tried to load invalid item: '%s'", error));
        });
    }
}
