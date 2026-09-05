package cc.thonly.reverie_dreams.neoforge.mixin.lootr;

import cc.thonly.reverie_dreams.advancement.SimpleTrigger;
import cc.thonly.reverie_dreams.util.advancements.SimpleTriggerKeys;
import net.minecraft.server.level.ServerPlayer;
import noobanidus.mods.lootr.common.api.MenuBuilder;
import noobanidus.mods.lootr.common.api.data.ILootrInfoProvider;
import noobanidus.mods.lootr.common.impl.DefaultLootrAPIImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(DefaultLootrAPIImpl.class)
public class ILootrAPIMixin {
    @Inject(method = "handleProviderOpen(Lnoobanidus/mods/lootr/common/api/data/ILootrInfoProvider;Lnet/minecraft/server/level/ServerPlayer;Lnoobanidus/mods/lootr/common/api/MenuBuilder;)V", at = @At("TAIL"), cancellable = true)
    public void reverie_dreams$handleInstanceOpen(ILootrInfoProvider par1, ServerPlayer player, MenuBuilder par3, CallbackInfo ci) {
        SimpleTrigger.trigger(player, SimpleTriggerKeys.OPEN_CHEST);
    }
}
