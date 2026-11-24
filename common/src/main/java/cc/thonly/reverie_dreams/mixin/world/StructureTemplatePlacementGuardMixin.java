package cc.thonly.reverie_dreams.mixin.world;

import cc.thonly.reverie_dreams.world.gensokyo.StructurePlacementGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.util.RandomSource;

@Mixin(StructureTemplate.class)
public abstract class StructureTemplatePlacementGuardMixin {
    @Inject(
            method = "placeInWorld",
            at = @At("HEAD"),
            cancellable = true
    )
    private void reverie_dreams$rejectPlacementInUnloadedChunks(
            ServerLevelAccessor level,
            BlockPos startPosition,
            BlockPos pivot,
            StructurePlaceSettings settings,
            RandomSource random,
            int flags,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (!(level instanceof ServerLevel serverLevel)
                || !StructurePlacementGuard.isEnabled(serverLevel.dimension())) {
            return;
        }

        BoundingBox attemptedBounds = settings.getBoundingBox();
        if (attemptedBounds == null) {
            StructureTemplate template = (StructureTemplate) (Object) this;
            attemptedBounds = template.getBoundingBox(settings, startPosition);
        }

        if (!StructurePlacementGuard.isPlacementReady(serverLevel, attemptedBounds)) {
            cir.setReturnValue(false);
        }
    }
}
