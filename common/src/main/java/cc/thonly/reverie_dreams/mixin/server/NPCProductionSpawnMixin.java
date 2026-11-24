package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.entity.npc.work.NPCProduction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevel.class)
public class NPCProductionSpawnMixin {
    @Inject(method = "addFreshEntity", at = @At("RETURN"))
    private void reverie_dreams$creditProduction(Entity entity, CallbackInfoReturnable<Boolean> result) {
        if (result.getReturnValueZ())
            NPCProduction.spawned(entity);
    }
}
