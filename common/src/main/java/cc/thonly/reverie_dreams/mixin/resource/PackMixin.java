package cc.thonly.reverie_dreams.mixin.resource;

import cc.thonly.reverie_dreams.api.resource.PackSourceTracker;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({Pack.class})
public abstract class PackMixin {
    @Shadow
    public abstract PackLocationInfo location();

    @Inject(
            method = {"open"},
            at = {@At("RETURN")}
    )
    private void onCreateResourcePack(CallbackInfoReturnable<PackResources> cir) {
        PackSourceTracker.setSource((PackResources)cir.getReturnValue(), this.location().source());
    }
}
