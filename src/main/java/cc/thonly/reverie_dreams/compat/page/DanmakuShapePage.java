package cc.thonly.reverie_dreams.compat.page;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.gui.BasePageGui;
import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.gui.recipe.display.DanmakuShapeDisplayView;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import eu.pb4.polydex.api.v1.recipe.*;
import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import eu.pb4.sgui.api.gui.SimpleGui;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DanmakuShapePage implements PolydexPage {
    public static final Identifier id = Touhou.id("recipe/danmaku_shape");
    public static final PolydexCategory CATEGORY = PolydexCategory.of(id);
    private static final Text TEXTURE = Text.empty();
    public static final ItemStack ICON = new GuiElementBuilder(ModItems.DANMAKU_SHAPE_CREATOR).setName(Text.translatable(id.toTranslationKey())).asStack();
    public final Identifier key;
    public final DanmakuShapeDrawRecipe value;
    private final List<PolydexIngredient<?>> ingredients;
    private final PolydexStack<?> output;

    public DanmakuShapePage(Identifier key, DanmakuShapeDrawRecipe value) {
        this.key = key.withPrefixedPath("recipe/");
        this.value = value;
        this.ingredients = List.of(PolydexIngredient.of(Ingredient.ofItem(ModGuiItems.ENABLE)));
        this.output = PolydexStack.of(this.value.getOutput().getItemStack());
    }

    @Override
    public Identifier identifier() {
        return key;
    }

    @Override
    public ItemStack typeIcon(ServerPlayerEntity serverPlayerEntity) {
        return ICON;
    }

    @Override
    public ItemStack entryIcon(@Nullable PolydexEntry polydexEntry, ServerPlayerEntity serverPlayerEntity) {
        return this.value.getOutput().getItemStack();
    }

    @Override
    public void createPage(@Nullable PolydexEntry polydexEntry, ServerPlayerEntity serverPlayerEntity, PageBuilder layout) {
        Runnable runnable = ()-> {
            DanmakuShapeDisplayView view = new DanmakuShapeDisplayView(serverPlayerEntity, RecipeEntryWrapper.of(this.key, this.value), null);
            view.open();
        };
        String[][] views = {
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "E", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
        };
        for (int row = 0; row < views.length; row++) {
            for (int col = 0; col < views[row].length; col++) {
                layout.set(col, row, getViewStack(views[row][col], (i, clickType, slotActionType) -> runnable.run()));
            }
        }
    }

    private GuiElementBuilder getViewStack(String s, GuiElementInterface.ItemClickCallback callback) {
        if (s.equalsIgnoreCase("X")) {
            return new GuiElementBuilder(ModGuiItems.EMPTY_SLOT);
        }
        if (s.equalsIgnoreCase("E")) {
            return new GuiElementBuilder(this.value.getOutput().getItemStack().copy())
                    .setCallback(callback)
                    .setLore(List.of(Text.empty().append(Text.translatable("item.tooltip.recipe.no_compat"))));
        }
        return new GuiElementBuilder(Items.AIR);
    }

    @Override
    public List<PolydexIngredient<?>> ingredients() {
        return this.getIngredients();
    }

    @Override
    public List<PolydexCategory> categories() {
        return List.of(CATEGORY);
    }

    @Override
    public boolean isOwner(MinecraftServer minecraftServer, PolydexEntry polydexEntry) {
        return polydexEntry.isPartOf(output);
    }
}
