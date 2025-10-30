package cc.thonly.minecraft.mixin;

import cc.thonly.minecraft.item.ItemStackNbtImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ItemStack.class)
public class ItemStackNbtMixin implements ItemStackNbtImpl {

    @Unique
    @Override
    public Tag toNbt(HolderLookup.Provider registryAccess, CompoundTag prefix) {
        ItemStack pThis = (ItemStack) (Object) this;
        if (pThis.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        }
        RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.CODEC.encode(pThis, ops, prefix).getOrThrow();
    }

    @Unique
    @Override
    public Tag toNbt(HolderLookup.Provider registryAccess) {
        ItemStack pThis = (ItemStack) (Object) this;
        if (pThis.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        }
        RegistryOps<Tag> ops = registryAccess.createSerializationContext(NbtOps.INSTANCE);
        return ItemStack.CODEC.encodeStart(ops, pThis).getOrThrow();
    }
}
