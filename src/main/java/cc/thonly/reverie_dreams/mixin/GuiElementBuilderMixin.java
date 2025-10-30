package cc.thonly.reverie_dreams.mixin;

import cc.thonly.reverie_dreams.interfaces.IGuiElementBuilderAccessor;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilderInterface;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

@Pseudo
@Mixin(value = GuiElementBuilder.class, remap = false)
public abstract class GuiElementBuilderMixin implements GuiElementBuilderInterface<GuiElementBuilder>, IGuiElementBuilderAccessor {
    @Shadow protected ItemStack itemStack;

    @Override
    public ItemStack setItemStack(ItemStack stack) {
        this.itemStack = stack;
        return stack;
    }

    @Override
    public ItemStack getItemStack() {
        return this.itemStack;
    }
}
