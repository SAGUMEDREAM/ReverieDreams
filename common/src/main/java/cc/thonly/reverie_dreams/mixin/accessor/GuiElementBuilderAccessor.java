package cc.thonly.reverie_dreams.mixin.accessor;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GuiElementBuilder.class)
public interface GuiElementBuilderAccessor {
    @Accessor("callback")
    GuiElement.ClickCallback getCallback();
}
