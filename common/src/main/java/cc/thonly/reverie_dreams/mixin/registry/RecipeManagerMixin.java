package cc.thonly.reverie_dreams.mixin.registry;

import cc.thonly.reverie_dreams.server.RecipeManagerHolder;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeManager.class)
public abstract class RecipeManagerMixin extends SimplePreparableReloadListener<RecipeMap> implements RecipeAccess {
    @Inject(
            method = "apply(Lnet/minecraft/world/item/crafting/RecipeMap;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V",
            at = @At("TAIL")
    )
    public void setHolder(RecipeMap recipes, ResourceManager manager, ProfilerFiller profiler, CallbackInfo ci) {
        RecipeManagerHolder.set((RecipeManager) (Object) this);
    }
}
