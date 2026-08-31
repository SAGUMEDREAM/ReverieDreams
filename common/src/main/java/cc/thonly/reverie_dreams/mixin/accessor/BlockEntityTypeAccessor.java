package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor<T extends BlockEntity> {
    @Invoker("<init>")
    static <T extends BlockEntity> BlockEntityType<T> reverie_dreams$init(BlockEntityType.BlockEntitySupplier<? extends T> factory, Set<Block> validBlocks) {
        throw new AssertionError("Implemented By Mixin");
    }
}
