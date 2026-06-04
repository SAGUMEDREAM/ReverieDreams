package cc.thonly.reverie_dreams.mixin.client;

import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.dialog.DialogScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@SuppressWarnings("rawtypes")
@Mixin(DialogScreen.class)
public class NoDialogWarningMixin {
    @Redirect(method = "createTitleWithWarningButton", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/LinearLayout;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 1))
    private LayoutElement noDialogWarningLayout(LinearLayout instance, LayoutElement widget) {
        return widget;
    }

    @Redirect(method = "init()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/dialog/DialogScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;"))
    private GuiEventListener noCreateWarningButton(DialogScreen instance, GuiEventListener guiEventListener) {
        return guiEventListener;
    }
}
