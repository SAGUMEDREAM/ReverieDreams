package cc.thonly.reverie_dreams.compat.jei.category;

import cc.thonly.reverie_dreams.compat.jei.JeiRecipeTypes;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class StrengthTableRecipeCategory implements IRecipeCategory<StrengthTableRecipe> {
    private final IGuiHelper helper;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable arrow;

    public StrengthTableRecipeCategory(IGuiHelper helper) {
        this.title = RDBlocks.STRENGTH_TABLE.asBlock().getName();
        this.helper = helper;
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, RDBlocks.STRENGTH_TABLE.createStack());
        this.arrow = helper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                RDGuiItems.PROGRESS_TO_RESULT.createStack()
        );
    }

    @Override
    public IRecipeType<StrengthTableRecipe> getRecipeType() {
        return JeiRecipeTypes.STRENGTH_TABLE;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 116;
    }

    @Override
    public int getHeight() {
        return 27;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(StrengthTableRecipe recipe, IRecipeSlotsView view, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        this.arrow.draw(graphics, 42, 9);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StrengthTableRecipe recipe, IFocusGroup focuses) {
        IDrawable slot = this.helper.getSlotDrawable();

        builder.addSlot(RecipeIngredientRole.INPUT, 0, 9)
                .setBackground(slot, -1, -1)
                .add(recipe.getMainItem().build().copy());

        builder.addSlot(RecipeIngredientRole.INPUT, 18, 9)
                .setBackground(slot, -1, -1)
                .add(recipe.getOffItem().build().copy());

        builder.addSlot(RecipeIngredientRole.OUTPUT, 78, 9)
                .setBackground(slot, -1, -1)
                .add(recipe.getOutput().build().copy());
    }
}
