package cc.thonly.reverie_dreams.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import net.minecraft.world.level.storage.WorldData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldOpenFlows.class)
public class IntegratedServerLoaderMixin {
    // Make SaveProperties.getLifecycle() always return Lifecycle.stable()
    @Redirect(
            method = "checkBackupAndStart",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/SaveProperties;getLifecycle()Lcom/mojang/serialization/Lifecycle;"
            )
    )
    private Lifecycle removeAdviceOnLoad(WorldData saveProperties) {
        return Lifecycle.stable();
    }

    // Set bypassWarnings = true
    @ModifyVariable(
            method = "tryLoad",
            at = @At("HEAD"),
            argsOnly = true,
            index = 4
    )
    private static boolean removeAdviceOnCreation(boolean original) {
        return true;
    }
}
