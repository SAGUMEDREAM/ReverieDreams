package cc.thonly.reverie_dreams.mixin;

import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(value = GuiElementBuilder.class, remap = false)
public abstract class GuiElementBuilderMixin implements GuiElementBuilderInterface<GuiElementBuilder> {

}
