package cc.thonly.reverie_dreams.compat.jei.category;

import cc.thonly.reverie_dreams.compat.jei.JeiRecipeTypes;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.item.DataComponentInitializersAccess;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

public class DanmakuShapeDrawRecipeCategory implements IRecipeCategory<DanmakuShapeDrawRecipe> {
    private final IGuiHelper helper;
    private final Component title;
    private final IDrawable icon;

    public DanmakuShapeDrawRecipeCategory(IGuiHelper helper) {
        this.title = DataComponentInitializersAccess.getNameByNonEmpty(RDItems.DANMAKU_SHAPE_CREATOR);
        this.helper = helper;
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, RDItems.DANMAKU_SHAPE_CREATOR.createStack());
    }

    @Override
    public IRecipeType<DanmakuShapeDrawRecipe> getRecipeType() {
        return JeiRecipeTypes.DANMAKU_SHAPE_DRAW;
    }

    @Override
    public Component getTitle() {
        return this.title;
    }

    @Override
    public int getWidth() {
        return 6 * 18 + 40; // ≈148
    }

    @Override
    public int getHeight() {
        return 6 * 18 + 20; // ≈128
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, DanmakuShapeDrawRecipe recipe, IFocusGroup focuses) {

        IDrawable slot = this.helper.getSlotDrawable();

        int gridWidth = 6 * 18;
        int gridHeight = 6 * 18;

        int startX = (getWidth() - gridWidth) / 2;
        int startY = (getHeight() - gridHeight) / 2;

        var shape = recipe.getShape();

        for (int y = 0; y < 6; y++) {
            for (int x = 0; x < 6; x++) {

                boolean state = false;
                if (y < shape.size() && x < shape.get(y).size()) {
                    state = shape.get(y).get(x);
                }

                builder.addSlot(
                                RecipeIngredientRole.INPUT,
                                startX + x * 18,
                                startY + y * 18
                        )
                        .setBackground(slot, -1, -1)
                        .add(state
                                ? RDGuiItems.ENABLE.createStack()
                                : RDGuiItems.DISABLE.createStack()
                        );
            }
        }

        // 左边工具
        builder.addSlot(RecipeIngredientRole.INPUT, startX - 24 + 4, startY + 36 + 9)
                .setBackground(slot, -1, -1)
                .add(RDItems.DANMAKU_SHAPE_CREATOR.createStack());

        // 右边输出
        builder.addSlot(RecipeIngredientRole.OUTPUT, startX + gridWidth + 8 - 4, startY + 36 + 9)
                .setBackground(slot, -1, -1)
                .add(recipe.getOutput().getItemStack().copy());
    }
}
