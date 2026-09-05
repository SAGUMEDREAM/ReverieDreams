package cc.thonly.reverie_dreams.compat.jei.category;

import cc.thonly.reverie_dreams.compat.jei.JeiRecipeTypes;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import java.util.List;

public class BrewingBarrelRecipeCategory implements IRecipeCategory<BrewingBarrelRecipe> {
    private final IGuiHelper helper;
    private final IDrawable icon;
    private final IDrawable arrow;

    public BrewingBarrelRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, RDBlocks.BREWING_BARREL.createStack());
        this.arrow = helper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                RDGuiPlaceholderItems.PROGRESS_TO_RESULT.createStack()
        );
    }

    @Override
    public IRecipeType<BrewingBarrelRecipe> getRecipeType() {
        return JeiRecipeTypes.BREWING;
    }

    @Override
    public Component getTitle() {
        return RDBlocks.BREWING_BARREL.asBlock().getName();
    }

    @Override
    public int getWidth() {
        return 18 * 9 + 18 * 2;
    }

    @Override
    public int getHeight() {
        return 18;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(BrewingBarrelRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        this.arrow.draw(graphics, 9
                * 18, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, BrewingBarrelRecipe recipe, IFocusGroup iFocusGroup) {
        IDrawable slot = this.helper.getSlotDrawable();
        List<IngredientStack> materials = recipe.getMaterials();
        IngredientStack output = recipe.getOutput();
        for (int i = 0; i < 9; i++) {
            var slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, i * 18, 0)
                    .setBackground(slot, -1, -1);
            if (i < materials.size()) {
                IngredientStack stack = materials.get(i);
                slotBuilder.add(stack.build());
            }
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 10 * 18, 0)
                .setBackground(slot, -1, -1)
                .add(output.build());
    }
}
