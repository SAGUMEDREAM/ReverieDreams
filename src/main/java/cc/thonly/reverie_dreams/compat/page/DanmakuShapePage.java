package cc.thonly.reverie_dreams.compat.page;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.gui.recipe.display.DanmakuShapeDisplayView;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuShapeDrawRecipe;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import eu.pb4.polydex.api.v1.recipe.*;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.elements.GuiElementInterface;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
public class DanmakuShapePage implements PolydexPage {
    public static final Identifier id = ReverieDreams.id("recipe/danmaku_shape");
    public static final PolydexCategory CATEGORY = PolydexCategory.of(id);
    private static final Component TEXTURE = Component.empty();
    public static final ItemStack ICON = new GuiElementBuilder(RDItems.DANMAKU_SHAPE_CREATOR).setName(Component.translatable(id.toLanguageKey())).asStack();
    public final Identifier key;
    public final DanmakuShapeDrawRecipe value;
    private final List<PolydexIngredient<?>> ingredients;
    private final PolydexStack<?> output;

    public DanmakuShapePage(Identifier key, DanmakuShapeDrawRecipe value) {
        this.key = key.withPrefix("recipe/");
        this.value = value;
        this.ingredients = List.of(PolydexIngredient.of(Ingredient.of(RDGuiItems.ENABLE)));
        this.output = PolydexStack.of(this.value.getOutput().getItemStack());
    }

    @Override
    public Identifier identifier() {
        return key;
    }

    @Override
    public ItemStack typeIcon(ServerPlayer serverPlayerEntity) {
        return ICON;
    }

    @Override
    public ItemStack entryIcon(@Nullable PolydexEntry polydexEntry, ServerPlayer serverPlayerEntity) {
        return this.value.getOutput().getItemStack();
    }

    @Override
    public void createPage(@Nullable PolydexEntry polydexEntry, ServerPlayer serverPlayerEntity, PageBuilder layout) {
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
            return new GuiElementBuilder(RDGuiItems.EMPTY_SLOT);
        }
        if (s.equalsIgnoreCase("E")) {
            return new GuiElementBuilder(this.value.getOutput().getItemStack().copy())
                    .setCallback(callback)
                    .setLore(List.of(Component.empty().append(Component.translatable("item.tooltip.recipe.no_compat"))));
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
