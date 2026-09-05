package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.Holder;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Mixin(Level.class)
public interface LevelAccessor {
//    @Invoker("getClockTimeTicks")
//    long reverie_dreams$getClockTimeTicks(Optional<? extends Holder<WorldClock>> clock);
}
