package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.api.recipe.RecipeIngredientItems;
import net.minecraft.server.WorldLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("Convert2MethodRef")
@Mixin(WorldLoader.class)
public class WorldLoaderMixin {
    @Inject(method = "load", at = @At("RETURN"))
    private static <D, R> void loadPatched(WorldLoader.InitConfig config,
                                           WorldLoader.WorldDataSupplier<D> worldDataSupplier,
                                           WorldLoader.ResultFactory<D, R> resultFactory,
                                           Executor backgroundExecutor,
                                           Executor mainThreadExecutor,
                                           CallbackInfoReturnable<CompletableFuture<R>> cir) {
        CompletableFuture<R> completableFuture = cir.getReturnValue();
        completableFuture.thenRun(() -> {
            RecipeIngredientItems.reload();
        });
    }
}
