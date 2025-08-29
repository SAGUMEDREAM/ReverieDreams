package cc.thonly.minecraft.mixin;

import cc.thonly.minecraft.item.ItemStackNbtImpl;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ItemStack.class)
public class ItemStackNbtMixin implements ItemStackNbtImpl {

    @Override
    public NbtElement toNbt(RegistryWrapper.WrapperLookup registryAccess, NbtCompound prefix) {
        ItemStack pThis = (ItemStack) (Object) this;
        if (pThis.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        }
        RegistryOps<NbtElement> ops = registryAccess.getOps(NbtOps.INSTANCE);
        return ItemStack.CODEC.encode(pThis, ops, prefix).getOrThrow();
    }

    @Override
    public NbtElement toNbt(RegistryWrapper.WrapperLookup registryAccess) {
        ItemStack pThis = (ItemStack) (Object) this;
        if (pThis.isEmpty()) {
            throw new IllegalStateException("Cannot encode empty ItemStack");
        }
        RegistryOps<NbtElement> ops = registryAccess.getOps(NbtOps.INSTANCE);
        return ItemStack.CODEC.encodeStart(ops, pThis).getOrThrow();
    }
}
