package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.world.trading_card.ITradingCardPlayer;
import cc.thonly.reverie_dreams.world.trading_card.TradingCardManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player implements ITradingCardPlayer {
    @Unique
    private TradingCardManager tradingCardManager;

    public ServerPlayerEntityMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions, CallbackInfo ci) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        this.tradingCardManager = new TradingCardManager(serverPlayer);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void read(ValueInput view, CallbackInfo ci) {
        this.tradingCardManager.read(view);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void write(ValueOutput view, CallbackInfo ci) {
        this.tradingCardManager.write(view);
    }

    @Override
    public TradingCardManager getTradingCardManager() {
        return this.tradingCardManager;
    }
}
