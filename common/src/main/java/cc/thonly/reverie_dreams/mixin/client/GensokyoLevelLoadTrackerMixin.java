package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.GensokyoLoadTrackerAccess;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.LevelLoadTracker;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.util.Util;
import net.minecraft.world.level.ChunkPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Recover the loading handshake only after the player's actual lit chunk has arrived.
 */
@Mixin(LevelLoadTracker.class)
public abstract class GensokyoLevelLoadTrackerMixin implements GensokyoLoadTrackerAccess {
    @Unique
    private static final Logger RD_LOAD_LOGGER = LoggerFactory.getLogger("GensokyoLevelLoad");
    @Unique
    private LocalPlayer rd$player;
    @Unique
    private ClientLevel rd$level;
    @Unique
    private long rd$startedAt;
    @Unique
    private boolean rd$positionReceived;
    @Unique
    private boolean rd$loadingSignalReceived;
    @Unique
    private boolean rd$renderFallback;
    @Unique
    private boolean rd$waitingLogged;
    @Unique
    private final Set<Long> rd$litChunks = new HashSet<>();

    @Shadow
    public abstract void loadingPacketsReceived();

    @Inject(method = "startClientLoad", at = @At("HEAD"))
    private void rd$start(LocalPlayer player, ClientLevel level, LevelRenderer renderer, CallbackInfo ci) {
        this.rd$player = player;
        this.rd$level = level;
        this.rd$startedAt = Util.getMillis();
        this.rd$positionReceived = false;
        this.rd$loadingSignalReceived = false;
        this.rd$renderFallback = false;
        this.rd$waitingLogged = false;
        this.rd$litChunks.clear();
    }

    @Inject(method = "loadingPacketsReceived", at = @At("TAIL"))
    private void rd$signal(CallbackInfo ci) {
        this.rd$loadingSignalReceived = true;
    }

    @Override
    public void reverie_dreams$receivedPosition() {
        this.rd$positionReceived = true;
    }

    @Override
    public void reverie_dreams$receivedLitChunk(int x, int z) {
        this.rd$litChunks.add(new ChunkPos(x, z).pack());
    }

    @Unique
    private boolean rd$hasPlayerTerrain() {
        if (this.rd$player == null || this.rd$level == null || !this.rd$positionReceived
                || !this.rd$level.dimension().equals(RDBuiltinLevels.getGensokyo())) {
            return false;
        }
        ChunkPos pos = this.rd$player.chunkPosition();
        return this.rd$litChunks.contains(pos.pack())
                && this.rd$level.getChunkSource().getChunk(pos.x(), pos.z(), false) != null;
    }

    @Inject(method = "tickClientLoad", at = @At("TAIL"))
    private void rd$recoverMissingSignal(CallbackInfo ci) {
        if (this.rd$level != null && this.rd$player != null
                && this.rd$level.dimension().equals(RDBuiltinLevels.getGensokyo())
                && !this.rd$waitingLogged && Util.getMillis() - this.rd$startedAt >= 10000) {
            this.rd$waitingLogged = true;
            RD_LOAD_LOGGER.warn("Gensokyo login readiness: position={}, loadingSignal={}, playerLitChunk={}, positionAt={}",
                    this.rd$positionReceived, this.rd$loadingSignalReceived, this.rd$hasPlayerTerrain(),
                    this.rd$player.blockPosition());
        }
        if (!this.rd$loadingSignalReceived && Util.getMillis() - this.rd$startedAt >= 5000
                && this.rd$hasPlayerTerrain()) {
            RD_LOAD_LOGGER.warn("Recovering Gensokyo loading signal: initial position and player chunk light are received");
            this.loadingPacketsReceived();
        }
    }

    @Inject(method = "isLevelReady", at = @At("RETURN"), cancellable = true)
    private void rd$recoverRenderWait(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && this.rd$loadingSignalReceived
                && Util.getMillis() - this.rd$startedAt >= 10000 && this.rd$hasPlayerTerrain()) {
            if (!this.rd$renderFallback) {
                this.rd$renderFallback = true;
                RD_LOAD_LOGGER.warn("Gensokyo player terrain is received; releasing loading screen after render-section wait at {}",
                        this.rd$player.blockPosition());
            }
            cir.setReturnValue(true);
        }
    }
}
