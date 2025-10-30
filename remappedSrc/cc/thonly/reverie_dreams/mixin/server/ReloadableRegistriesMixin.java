package cc.thonly.reverie_dreams.mixin.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.packs.resources.ResourceManager;

@Mixin(ReloadableServerRegistries.class)
public class ReloadableRegistriesMixin {
    @Inject(method = "reload", at = @At("HEAD"))
    private static void reload(LayeredRegistryAccess<RegistryLayer> dynamicRegistries, List<Registry.PendingTags<?>> pendingTagLoads, ResourceManager resourceManager, Executor prepareExecutor, CallbackInfoReturnable<CompletableFuture<ReloadableServerRegistries.LoadResult>> cir) {

    }
}
