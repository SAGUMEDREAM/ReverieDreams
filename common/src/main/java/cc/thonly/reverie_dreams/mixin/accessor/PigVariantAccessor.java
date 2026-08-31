package cc.thonly.reverie_dreams.mixin.accessor;

import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PigVariant.class)
public interface PigVariantAccessor {
    @Invoker("<init>")
    static PigVariant invokeStaticInit(ModelAndTexture<PigVariant.ModelType> modelAndTexture, ClientAsset.ResourceTexture babyTexture, SpawnPrioritySelectors spawnConditions) {
        throw new AssertionError();
    }

    @Invoker("<init>")
    static PigVariant invokeStaticInit(ModelAndTexture<PigVariant.ModelType> assetInfo, ClientAsset.ResourceTexture babyTexture) {
        throw new AssertionError();
    }
}
