package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.world.trading_card.ITradingCardPlayer;
import cc.thonly.reverie_dreams.world.trading_card.TradingCardManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements ITradingCardPlayer {
    @Unique
    private TradingCardManager tradingCardManager;

    public ServerPlayerEntityMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions, CallbackInfo ci) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        this.tradingCardManager = new TradingCardManager(serverPlayer);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void read(CompoundTag compoundTag, CallbackInfo ci) {
        this.tradingCardManager.read(compoundTag);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void write(CompoundTag compoundTag, CallbackInfo ci) {
        this.tradingCardManager.write(compoundTag);
    }

    @Override
    public TradingCardManager getTradingCardManager() {
        return this.tradingCardManager;
    }
}
