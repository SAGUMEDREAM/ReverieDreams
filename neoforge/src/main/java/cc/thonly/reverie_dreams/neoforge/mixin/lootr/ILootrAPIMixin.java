package cc.thonly.reverie_dreams.neoforge.mixin.lootr;

import cc.thonly.reverie_dreams.advancement.SimpleTrigger;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import net.minecraft.server.level.ServerPlayer;
import noobanidus.mods.lootr.common.api.data.ILootrContainerInstance;
import noobanidus.mods.lootr.common.api.interfaces.lootr.ILootrAPI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("UnstableApiUsage")
@Pseudo
@Mixin(ILootrAPI.class)
public interface ILootrAPIMixin {
    @Inject(method = "handleInstanceOpen(Lnoobanidus/mods/lootr/common/api/data/ILootrContainerInstance;Lnet/minecraft/server/level/ServerPlayer;)V", at=@At("TAIL"), cancellable = true)
    default void reverie_dreams$handleInstanceOpen(ILootrContainerInstance instance, ServerPlayer player, CallbackInfo ci) {
        SimpleTrigger.trigger(player, SimpleTriggerKeys.OPEN_CHEST);
    }
}
