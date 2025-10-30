package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.interfaces.IWorld;
import cc.thonly.reverie_dreams.world.dimension.WorldInit;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Level.class)
public abstract class WorldMixin implements LevelAccessor,
        AutoCloseable, IWorld {

    @Inject(method = "playSound(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V", at = @At("TAIL"))
    public void onPlaySound(Entity source, BlockPos pos, SoundEvent sound, SoundSource category, float volume, float pitch, CallbackInfo ci) {

    }

    @Override
    public ResourceKey<Level> getDreamWorld() {
        return WorldInit.getDreamWorld();
    }

    @Override
    public ResourceKey<Level> getMoon() {
        return WorldInit.getMoon();
    }
}
