package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.item.weapon.WeaponOfTheMoon;
import cc.thonly.reverie_dreams.networking.payload.LunarAimFirePacket;
import dev.architectury.networking.NetworkManager;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeMixin {
    @Inject(method = "releaseUsingItem", at = @At("HEAD"), cancellable = true)
    private void reverieDreams$lowerScopedWeapon(Player player, CallbackInfo ci) {
        var stack = player.getUseItem();
        if (stack.getItem() instanceof WeaponOfTheMoon gun && gun.getUseAnimation(stack) == ItemUseAnimation.BOW
                && player.getTicksUsingItem() >= WeaponOfTheMoon.AIM_HOLD_TICKS
                && NetworkManager.canServerReceive(LunarAimFirePacket.PACKET_ID)) {
            NetworkManager.sendToServer(new LunarAimFirePacket(false));
            player.stopUsingItem();
            ci.cancel();
        }
    }
}
