package cc.thonly.reverie_dreams.neoforge.mixin.client;

import cc.thonly.reverie_dreams.client.NPCScreen;
import com.micaftic.morpher.client.ClientModelManager;
import com.micaftic.morpher.client.gui.ModernPlayerModelScreen;
import com.micaftic.morpher.client.model.ModelAssembly;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiConsumer;

@Pseudo
@Mixin(ModernPlayerModelScreen.class)
public abstract class ModernPlayerModelScreenMixin implements NPCScreen {

    @Shadow
    @Final
    private BiConsumer<String, String> modelSelectionTarget;

    @Shadow
    protected abstract void setStatus(Component component, ChatFormatting green);

    @Unique
    public boolean reverie_dreams$applyForNPC = false;

    @Inject(method = "applyModelAndTexture", at = @At("HEAD"), cancellable = true)
    public void reverie_dreams$applyModelAndTexture(String modelId, String textureId, ModelAssembly assembly, CallbackInfo ci) {
        if (this.reverie_dreams$applyForNPC) {
            if (this.modelSelectionTarget != null) {
                this.modelSelectionTarget.accept(modelId, textureId);
                this.setStatus(Component.translatable("gui.sparkle_morpher.model_panel.applied_model", new Object[]{modelId}), ChatFormatting.GREEN);
                ci.cancel();
            }
        }
    }

    @Override
    public void reverie_dreams$setApplyForNPC(boolean value) {
        this.reverie_dreams$applyForNPC = value;
    }

    @Override
    public boolean reverie_dreams$isApplyForNPC() {
        return this.reverie_dreams$applyForNPC;
    }
}
