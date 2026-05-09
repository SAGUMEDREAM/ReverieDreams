package cc.thonly.reverie_dreams.compat.jei.category;

import cc.thonly.reverie_dreams.compat.jei.JeiRecipeTypes;
import cc.thonly.reverie_dreams.gui.recipe.gui.GensokyoAltarGui;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
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

public class GensokyoAltarRecipeCategory implements IRecipeCategory<GensokyoAltarRecipe> {
    private final IGuiHelper helper;
    private final Component title;
    private final IDrawable icon;
    private final IDrawable arrow;

    public GensokyoAltarRecipeCategory(IGuiHelper helper) {
        this.title = RDBlocks.GENSOKYO_ALTAR.asBlock().getName();
        this.helper = helper;
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,RDBlocks.GENSOKYO_ALTAR.createStack());
        this.arrow = helper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                RDGuiItems.PROGRESS_TO_RESULT.asItem().getDefaultInstance()
        );
    }

    @Override
    public IRecipeType<GensokyoAltarRecipe> getRecipeType() {
        return JeiRecipeTypes.GENSOKYO_ALTAR;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 9 * 18; // 162
    }

    @Override
    public int getHeight() {
        return 5 * 18; // 90
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void draw(GensokyoAltarRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        this.arrow.draw(graphics, 18 * 6, 18 * 2);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, GensokyoAltarRecipe recipe, IFocusGroup focuses) {

        IDrawable slot = this.helper.getSlotDrawable();

        int invIndex = 0;

        int coreX = 0;
        int coreY = 0;

        int offsetX = -18 * 2;

        for (int row = 0; row < GensokyoAltarGui.GRID.length; row++) {
            for (int col = 0; col < GensokyoAltarGui.GRID[row].length; col++) {

                String type = GensokyoAltarGui.GRID[row][col];

                int x = col * 18 + offsetX;
                int y = row * 18;

                if (type.equals("X")) {
                    continue;
                }

                if (type.equals("I")) {
                    if (invIndex < recipe.getSlots().size()) {
                        builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                                .setBackground(slot, -1, -1)
                                .add(recipe.getSlots().get(invIndex).build());
                    } else {
                        builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                                .setBackground(slot, -1, -1);
                    }
                    invIndex++;
                    continue;
                }

                if (type.equals("E")) {
                    coreX = x;
                    coreY = y;

                    builder.addSlot(RecipeIngredientRole.INPUT, x, y)
                            .setBackground(slot, -1, -1)
                            .add(recipe.getCore().build());

                    continue;
                }
            }
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, coreX + 18 * 6, coreY)
                .setBackground(slot, -1, -1)
                .add(recipe.getOutput().build().copy());
    }
}
