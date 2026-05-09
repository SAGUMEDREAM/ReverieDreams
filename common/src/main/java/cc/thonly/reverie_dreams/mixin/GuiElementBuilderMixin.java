package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.inf.IGuiElementBuilderAccessor;
import eu.pb4.sgui.api.elements.BaseItemStackBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderCreator;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(value = GuiElementBuilder.class, remap = false)
public abstract class GuiElementBuilderMixin implements GuiElementBuilderCreator<GuiElementBuilder> {

}
