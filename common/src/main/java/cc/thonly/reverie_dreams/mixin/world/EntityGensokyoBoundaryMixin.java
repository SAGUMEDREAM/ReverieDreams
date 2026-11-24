package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.GensokyoBoundaryCollision;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public abstract class EntityGensokyoBoundaryMixin {
    @Inject(method = "collectColliders", at = @At("RETURN"), cancellable = true)
    private static void reverie_dream$borderCollision(Entity source, Level level, List<VoxelShape> entityColliders,
                                                      AABB area, CallbackInfoReturnable<List<VoxelShape>> cir) {
        cir.setReturnValue(GensokyoBoundaryCollision.append(source, level, area, cir.getReturnValue()));
    }
}
