package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateGate;
import net.minecraft.server.level.*;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkMap.class)
public abstract class StructureUpdateChunkSendMixin {
    @Shadow
    @Final
    private ServerLevel level;

    @Inject(method = "getChunkToSend", at = @At("HEAD"), cancellable = true)
    private void reverie$holdUncommittedStructure(long chunk, CallbackInfoReturnable<LevelChunk> ci) {
        if (StructureUpdateGate.blocks(level, chunk))
            ci.setReturnValue(null);
    }
}
