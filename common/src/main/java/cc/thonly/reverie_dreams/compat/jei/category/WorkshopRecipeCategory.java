package cc.thonly.reverie_dreams.compat.jei.category;

import cc.thonly.reverie_dreams.compat.jei.JeiRecipeTypes;
import cc.thonly.reverie_dreams.recipe.entry.WorkshopProcessRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDJapaneseSet2Blocks;
import cc.thonly.reverie_dreams.block.decor.workshop.WorkshopKind;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;

public final class WorkshopRecipeCategory implements IRecipeCategory<WorkshopProcessRecipe> {
    private final boolean drying;
    private final IGuiHelper helper;

    public WorkshopRecipeCategory(IGuiHelper helper, boolean drying) {
        this.helper = helper;
        this.drying = drying;
    }

    @Override
    public IRecipeType<WorkshopProcessRecipe> getRecipeType() {
        return drying ? JeiRecipeTypes.HERB_DRYING : JeiRecipeTypes.GRINDING;
    }

    @Override
    public Component getTitle() {
        return RDJapaneseSet2Blocks.get(drying ? WorkshopKind.HERB_DRYING_TRAY : WorkshopKind.YAGEN).asBlock().getName();
    }

    @Override
    public int getWidth() {
        return 90;
    }

    @Override
    public int getHeight() {
        return 22;
    }

    @Override
    public IDrawable getIcon() {
        return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, RDJapaneseSet2Blocks.get(drying ? WorkshopKind.HERB_DRYING_TRAY : WorkshopKind.YAGEN).createStack());
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, WorkshopProcessRecipe recipe, IFocusGroup focus) {
        var input = builder.addSlot(RecipeIngredientRole.INPUT, 3, 3).setBackground(helper.getSlotDrawable(), -1, -1);
        BuiltInRegistries.ITEM.stream().map(item -> item.getDefaultInstance()).filter(recipe::matches).forEach(input::add);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 65, 3).setBackground(helper.getSlotDrawable(), -1, -1).add(recipe.getOutput().build());
    }
}
