package cc.thonly.reverie_dreams.mixin.server;

import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.profiler.Profiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerRecipeManager.class)
public class ServerRecipeManagerMixin {
    @Inject(method = "apply(Lnet/minecraft/recipe/PreparedRecipes;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
    public void afterReload(PreparedRecipes preparedRecipes, ResourceManager resourceManager, Profiler profiler, CallbackInfo ci) {
//        if (Touhou.getServer() != null) {
//            CookingInputRecipeManager.getInstance().load(Touhou.getServer());
//        }
    }
}
