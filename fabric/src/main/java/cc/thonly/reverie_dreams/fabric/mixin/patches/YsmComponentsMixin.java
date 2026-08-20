package cc.thonly.reverie_dreams.fabric.mixin.patches;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import com.micaftic.morpher.capability.fabric.AuthModelsComponent;
import com.micaftic.morpher.capability.fabric.ModelInfoComponent;
import com.micaftic.morpher.capability.fabric.StarModelsComponent;
import com.micaftic.morpher.fabric.YsmComponents;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(YsmComponents.class)
public class YsmComponentsMixin {
    @Inject(method = "registerEntityComponentFactories", at = @At("TAIL"))
    public void reverie_dreams$registerEntityComponentFactories(EntityComponentFactoryRegistry registry, CallbackInfo ci) {
        registry.registerFor(NPCSimpleEntity.class, YsmComponents.STAR_MODELS, (p) -> new StarModelsComponent());
        registry.registerFor(NPCSimpleEntity.class, YsmComponents.AUTH_MODELS, (p) -> new AuthModelsComponent());
        registry.registerFor(NPCSimpleEntity.class, YsmComponents.MODEL_INFO, (p) -> new ModelInfoComponent());
    }
}
