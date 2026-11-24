package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.scope.LunarScopeClient;
import cc.thonly.reverie_dreams.item.weapon.Gungnir;
import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.networking.payload.GungnirThrowPacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void reverieDreams$attackWhileUsingWeapon(CallbackInfo ci) {
        LunarScopeClient.handleAttackKey();
        Minecraft client = (Minecraft) (Object) this;
        if (client.player != null && client.options.keyUse.isDown() && Gungnir.isInChargeWindow(client.player)
                && NetworkManager.canServerReceive(GungnirThrowPacket.PACKET_ID)
                && client.options.keyAttack.consumeClick()) {
            NetworkManager.sendToServer(GungnirThrowPacket.INSTANCE);
        }
    }

    @Inject(method = "startUseItem", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/phys/HitResult;getType()Lnet/minecraft/world/phys/HitResult$Type;"),
            cancellable = true)
    private void reverieDreams$useGunInsteadOfEntityInteraction(CallbackInfo ci) {
        Minecraft client = (Minecraft) (Object) this;
        if (!(client.hitResult instanceof EntityHitResult) || client.player.isSpectator())
            return;
        for (InteractionHand hand : InteractionHand.values()) {
            if (client.player.getItemInHand(hand).getItem() instanceof WeaponOfTheMoon) {
                client.gameMode.useItem(client.player, hand);
                ci.cancel();
                return;
            }
        }
    }
}
