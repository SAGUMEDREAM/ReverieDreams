package cc.thonly.reverie_dreams.mixin.server;

import cc.thonly.reverie_dreams.api.entity.YoukaiFeedingSource;
import cc.thonly.reverie_dreams.entity.ai.YoukaiFeeding;
import cc.thonly.reverie_dreams.entity.ai.YoukaiRelations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class YoukaiPlayerDropMixin {
    @Inject(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At("RETURN"))
    private void reverie_dreams$markFeeding(ItemStack stack, boolean randomly, boolean thrownFromHand,
                                            CallbackInfoReturnable<ItemEntity> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ItemEntity item = cir.getReturnValue();
        if (thrownFromHand && !randomly && player.isAlive() && !player.isSpectator()
                && item != null && YoukaiFeeding.isMeat(item.getItem()) && item instanceof YoukaiFeedingSource source) {
            YoukaiRelations.reputation(player);
            source.reverie_dreams$setYoukaiFeeder(player.getUUID());
        }
    }
}
