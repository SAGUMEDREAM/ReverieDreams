package cc.thonly.reverie_dreams.mixin.client;

import cc.thonly.reverie_dreams.client.renderer.entity.NPCFishingHookRenderer;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.FishingRodCast;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FishingRodCast.class)
public class FishingRodCastMixin {
    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$getNPCRodCast(ItemStack itemStack, ClientLevel level, LivingEntity owner, int seed, ItemDisplayContext displayContext, CallbackInfoReturnable<Boolean> cir) {
        if (!(owner instanceof BaseNPCLikeEntity npc && npc.fishing == null)) {
            return;
        }
        HumanoidArm holdingArm = NPCFishingHookRenderer.getHoldingArm(npc);
        cir.setReturnValue(npc.getItemHeldByArm(holdingArm) == itemStack);
    }
}
