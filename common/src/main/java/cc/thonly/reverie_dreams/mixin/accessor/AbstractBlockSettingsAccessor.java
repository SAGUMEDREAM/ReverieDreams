package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BlockBehaviour.Properties.class)
public interface AbstractBlockSettingsAccessor {
    @Accessor("soundType")
    SoundType getSoundGroup();
}
