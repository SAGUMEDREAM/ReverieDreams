package cc.thonly.reverie_dreams.mixin.accessor;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FireBlock.class)
public interface FireBlockAccessor {

    @Accessor("igniteOdds")
    Object2IntMap<Block> reverie_dreams$getIgniteOdds();

    @Accessor("burnOdds")
    Object2IntMap<Block> reverie_dreams$getBurnOdds();

    @Invoker("setFlammable")
    void reverie_dreams$setFlammable(Block block, int igniteOdds, int burnOdds);
}