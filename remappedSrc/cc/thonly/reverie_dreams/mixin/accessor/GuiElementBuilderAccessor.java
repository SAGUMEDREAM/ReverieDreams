package cc.thonly.reverie_dreams.mixin.accessor;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiElementBuilder.class, remap = false)
public interface GuiElementBuilderAccessor {
    @Accessor("callback")
    GuiElementInterface.ClickCallback getCallback();
}
