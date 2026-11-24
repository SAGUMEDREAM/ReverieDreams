package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.world.dimension.RDBuiltInDimensions;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoFeature;
import cc.thonly.reverie_dreams.world.gensokyo.GensokyoRegenerator;
import cc.thonly.reverie_dreams.world.gensokyo.data.GensokyoMapLoader;
import cc.thonly.reverie_dreams.world.gensokyo.gen.GensokyoChunkGenerator;
import dev.architectury.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.LevelResource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    @Inject(method = "reloadResources", at = @At("RETURN"))
    private void reverie$reloadStructureVersions(java.util.Collection<String> packs, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<java.util.concurrent.CompletableFuture<Void>> ci) {
        var server = (MinecraftServer) (Object) this;
        ci.getReturnValue().thenRun(() -> server.execute(() -> cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService.reloaded(server)));
    }

    @Inject(method = "saveEverything", at = @At("HEAD"))
    private void reverie$flushStructureLedger(boolean suppressLogs, boolean flush, boolean force, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> ci) {
        cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService.flush((MinecraftServer) (Object) this);
    }

    @Inject(method = "saveEverything", at = @At("RETURN"))
    private void reverie$acknowledgeThawedActors(boolean suppressLogs, boolean flush, boolean force, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<Boolean> ci) {
        if (flush && ci.getReturnValueZ())
            cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateService.saved((MinecraftServer) (Object) this);
    }

    /**
     * 在维度创建之前处理待处理的“重新生成幻想乡”标记。
     * 此时区域文件尚未被打开，可以安全地把旧维度目录整个移走。
     */
    @Inject(method = "createLevels", at = @At("HEAD"))
    private void reverie_dreams$processGensokyoRegeneration(CallbackInfo ci) {
        MinecraftServer server = (MinecraftServer) (Object) this;
        GensokyoMapLoader.configureSharedCache(Platform.getGameFolder(), server.getWorldPath(LevelResource.ROOT));
        GensokyoRegenerator.processPending(server);
        cc.thonly.reverie_dreams.world.gensokyo.update.StructureUpdateGate.restore(server);
    }

    /**
     * No player can be in the Gensokyo dimension yet when createLevels returns.
     * Start the decompression/read of the fixed map off-thread. If a player
     * enters the dimension while it is still running, GensokyoMapLoader.get()
     * joins this task on the server thread.
     */
    @Inject(method = "createLevels", at = @At("TAIL"))
    private void reverie_dreams$preloadGensokyoMap(CallbackInfo ci) {
        if (GensokyoFeature.isEnabled()) {
            GensokyoMapLoader.preloadAsync(GensokyoChunkGenerator.DEFAULT_MAP);
        }
    }

    /**
     * 当 GensokyoFeature.ENABLED 为 false 时不创建幻想乡维度。
     *
     * <p>只过滤 createLevels 遍历的 LevelStem，不动动态注册表本身：
     * 维度 JSON 仍能被正常解析，其他维度不受影响，旧存档也不会因为注册表缺项而崩溃。
     * 玩家如果保存位置在幻想乡，原版 PrepareSpawnTask 会回退到主世界。</p>
     */
    @Redirect(
            method = "createLevels",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Registry;entrySet()Ljava/util/Set;"
            )
    )
    private Set<Map.Entry<ResourceKey<LevelStem>, LevelStem>> reverie_dreams$filterGensokyoLevel(
            Registry<LevelStem> registry) {
        Set<Map.Entry<ResourceKey<LevelStem>, LevelStem>> entries = registry.entrySet();
        if (GensokyoFeature.isEnabled()) {
            return entries;
        }
        if (entries.stream().noneMatch(entry -> entry.getKey().equals(RDBuiltInDimensions.GENSOKYO))) {
            return entries;
        }
        GensokyoFeature.logDisabledOnce();
        return entries.stream()
                .filter(entry -> !entry.getKey().equals(RDBuiltInDimensions.GENSOKYO))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
