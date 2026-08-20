package cc.thonly.reverie_dreams.compat.jei.category;

import cc.thonly.reverie_dreams.compat.jei.JeiRecipeTypes;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.entry.KitchenRecipe;
import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.registry.content.block.RDKitchenBlocks;
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
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

public abstract class BaseKitchenRecipeCategory implements IRecipeCategory<KitchenRecipe> {
    private final IGuiHelper helper;
    private final IDrawable icon;
    private final IDrawable arrow;

    public BaseKitchenRecipeCategory(IGuiHelper helper, Function<IGuiHelper, IDrawable> icon, KitchenRecipeType.TypeInstance kitchenType) {
        this.helper = helper;
        this.icon = icon.apply(helper);
        this.arrow = helper.createDrawableIngredient(
                VanillaTypes.ITEM_STACK,
                RDGuiPlaceholderItems.PROGRESS_TO_RESULT.createStack()
        );
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public int getWidth() {
        return 18 * 10; // 180
    }

    @Override
    public int getHeight() {
        return 27;
    }

    @Override
    public void draw(KitchenRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
        int startX = 18 * 2;

        this.arrow.draw(graphics, startX + 5 * 18, 0);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, KitchenRecipe recipe, IFocusGroup focuses) {
        IDrawable slot = this.helper.getSlotDrawable();

        List<IngredientStack> ingredients = recipe.getIngredients();
        IngredientStack output = recipe.getOutput();

        int startX = 18 * 2;
        int y = 0;

        for (int i = 0; i < 5; i++) {

            var slotBuilder = builder.addSlot(RecipeIngredientRole.INPUT, startX + i * 18, y)
                    .setBackground(slot, -1, -1);

            if (i < ingredients.size()) {
                slotBuilder.add(ingredients.get(i).build());
            }
        }

        builder.addSlot(RecipeIngredientRole.OUTPUT, startX + 6 * 18, y)
                .setBackground(slot, -1, -1)
                .add(output.build().copy());
    }

    public static IDrawable createIcon(IGuiHelper helper, ItemStack itemStack) {
        return helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, itemStack);
    }

    public static class CookingPotImpl extends BaseKitchenRecipeCategory {
        public CookingPotImpl(IGuiHelper helper) {
            super(helper, h -> createIcon(h, RDKitchenBlocks.COOKING_POT.createStack()), KitchenRecipeType.TypeInstance.COOKING_POT);
        }

        @Override
        public IRecipeType<KitchenRecipe> getRecipeType() {
            return JeiRecipeTypes.COOKING_POT;
        }

        @Override
        public Component getTitle() {
            return RDKitchenBlocks.COOKING_POT.asBlock().getName();
        }
    }

    public static class CuttingBoardImpl extends BaseKitchenRecipeCategory {
        public CuttingBoardImpl(IGuiHelper helper) {
            super(helper, h -> createIcon(h, RDKitchenBlocks.CUTTING_BOARD.createStack()), KitchenRecipeType.TypeInstance.CUTTING_BOARD);
        }

        @Override
        public IRecipeType<KitchenRecipe> getRecipeType() {
            return JeiRecipeTypes.CUTTING_BOARD;
        }

        @Override
        public Component getTitle() {
            return RDKitchenBlocks.CUTTING_BOARD.asBlock().getName();
        }
    }

    public static class FryingPanImpl extends BaseKitchenRecipeCategory {
        public FryingPanImpl(IGuiHelper helper) {
            super(helper, h -> createIcon(h, RDKitchenBlocks.FRYING_PAN.createStack()), KitchenRecipeType.TypeInstance.FRYING_PAN);
        }

        @Override
        public IRecipeType<KitchenRecipe> getRecipeType() {
            return JeiRecipeTypes.FRYING_PAN;
        }

        @Override
        public Component getTitle() {
            return RDKitchenBlocks.FRYING_PAN.asBlock().getName();
        }
    }

    public static class GrillImpl extends BaseKitchenRecipeCategory {
        public GrillImpl(IGuiHelper helper) {
            super(helper, h -> createIcon(h, RDKitchenBlocks.GRILL.createStack()), KitchenRecipeType.TypeInstance.GRILL);
        }

        @Override
        public IRecipeType<KitchenRecipe> getRecipeType() {
            return JeiRecipeTypes.GRILL;
        }

        @Override
        public Component getTitle() {
            return RDKitchenBlocks.GRILL.asBlock().getName();
        }
    }

    public static class SteamerImpl extends BaseKitchenRecipeCategory {
        public SteamerImpl(IGuiHelper helper) {
            super(helper, h -> createIcon(h, RDKitchenBlocks.STEAMER.createStack()), KitchenRecipeType.TypeInstance.STEAMER);
        }

        @Override
        public IRecipeType<KitchenRecipe> getRecipeType() {
            return JeiRecipeTypes.STEAMER;
        }

        @Override
        public Component getTitle() {
            return RDKitchenBlocks.STEAMER.asBlock().getName();
        }
    }
}
