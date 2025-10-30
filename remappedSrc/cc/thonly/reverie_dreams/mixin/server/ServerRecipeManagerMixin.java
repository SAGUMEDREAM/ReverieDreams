package cc.thonly.reverie_dreams.mixin.server;

import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public class ServerRecipeManagerMixin {
    @Inject(method = "apply(Lnet/minecraft/recipe/PreparedRecipes;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V", at = @At("RETURN"))
    public void afterReload(RecipeMap preparedRecipes, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
//        if (Touhou.getServer() != null) {
//            CookingInputRecipeManager.getInstance().load(Touhou.getServer());
//        }
    }
}
