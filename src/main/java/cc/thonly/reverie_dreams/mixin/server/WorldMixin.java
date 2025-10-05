package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.interfaces.IWorld;
import cc.thonly.reverie_dreams.world.gen.WorldInit;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess,
        AutoCloseable, IWorld {

    @Inject(method = "playSound(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V", at = @At("TAIL"))
    public void onPlaySound(Entity source, BlockPos pos, SoundEvent sound, SoundCategory category, float volume, float pitch, CallbackInfo ci) {

    }

    @Override
    public RegistryKey<World> getDreamWorld() {
        return WorldInit.getDreamWorld();
    }

    @Override
    public RegistryKey<World> getMoon() {
        return WorldInit.getMoon();
    }
}
