package cc.thonly.reverie_dreams.neoforge.mixin.client;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.neoforge.integration.sparkle.NPCCapability;
import cc.thonly.reverie_dreams.neoforge.integration.sparkle.screen.NPCUnifiedRouletteScreen;
import com.micaftic.morpher.client.input.AnimationRouletteKey;
import com.micaftic.morpher.client.model.ModelAssembly;
import com.micaftic.morpher.core.gui.UnifiedRouletteScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(AnimationRouletteKey.class)
public class AnimationRouletteKeyMixin {
//    @Inject(method = "handleRoulettePress", at = @At("HEAD"), cancellable = true)
//    private static void reverie_dreams$ysm$handleRoulettePress(CallbackInfo ci) {
//        Minecraft mc = Minecraft.getInstance();
//        Entity crosshairPickEntity = mc.crosshairPickEntity;
//        if (!(crosshairPickEntity instanceof BaseNPCLikeEntity npc)) {
//            return;
//        }
//        if (!npc.reverie_dreams$hasModel()) {
//            return;
//        }
//        NPCCapability.get(crosshairPickEntity).ifPresent(cap -> {
//            String modelId = cap.getModelId();
//            ModelAssembly modelAssembly = cap.getModelAssembly();
//            if (modelAssembly != null && !modelAssembly.getModelData().getModelProperties().getExtraAnimation().isEmpty()) {
//                if (mc.screen == null) {
//                    mc.setScreen(new NPCUnifiedRouletteScreen(npc, modelId, modelAssembly, cap));
//                } else if (mc.screen instanceof UnifiedRouletteScreen) {
//                    mc.setScreen((Screen) null);
//                }
//            }
//        });
//        ci.cancel();
//    }
}
