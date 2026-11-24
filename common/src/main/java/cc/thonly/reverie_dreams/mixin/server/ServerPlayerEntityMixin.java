package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.restaurant.Settlement;
import cc.thonly.reverie_dreams.restaurant.SettlementParty;
import cc.thonly.reverie_dreams.restaurant.SettlementRewards;
import cc.thonly.reverie_dreams.world.dream.DreamSleepAnchor;
import cc.thonly.reverie_dreams.world.trading_card.ITradingCardPlayer;
import cc.thonly.reverie_dreams.world.trading_card.TradingCardManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
public abstract class ServerPlayerEntityMixin extends Player implements ITradingCardPlayer, DreamSleepAnchor, SettlementParty {
    @Unique
    private TradingCardManager reverie_dreams$tradingCardManager;
    @Unique
    private BlockPos reverie_dreams$dreamSleepAnchor;

    public ServerPlayerEntityMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void onInit(MinecraftServer server, ServerLevel world, GameProfile profile, ClientInformation clientOptions, CallbackInfo ci) {
        ServerPlayer serverPlayer = (ServerPlayer) (Object) this;
        this.reverie_dreams$tradingCardManager = new TradingCardManager(serverPlayer);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void read(ValueInput view, CallbackInfo ci) {
        this.reverie_dreams$tradingCardManager.read(view);
        this.reverie_dreams$dreamSleepAnchor = view.read("ReverieDreamSleepAnchor", BlockPos.CODEC).orElse(null);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void write(ValueOutput view, CallbackInfo ci) {
        this.reverie_dreams$tradingCardManager.write(view);
        view.storeNullable("ReverieDreamSleepAnchor", BlockPos.CODEC, this.reverie_dreams$dreamSleepAnchor);
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void reverie_dreams$restoreSleepAnchor(ServerPlayer previous, boolean keepEverything, CallbackInfo ci) {
        this.reverie_dreams$dreamSleepAnchor = ((DreamSleepAnchor) previous).reverie_dreams$getDreamSleepAnchor();
    }

    @Override
    public BlockPos reverie_dreams$getDreamSleepAnchor() {
        return this.reverie_dreams$dreamSleepAnchor;
    }

    @Override
    public void reverie_dreams$setDreamSleepAnchor(BlockPos pos) {
        this.reverie_dreams$dreamSleepAnchor = pos == null ? null : pos.immutable();
    }

    @Override
    public TradingCardManager getReverie_dreams$tradingCardManager() {
        return this.reverie_dreams$tradingCardManager;
    }

    @Override
    public LivingEntity getBaseEntity() {
        return (ServerPlayer) (Object) this;
    }

    @Override
    public void settle(Entity source, Settlement settlement) {
        SettlementRewards.grant(source, (ServerPlayer) (Object) this, settlement);
    }
}
