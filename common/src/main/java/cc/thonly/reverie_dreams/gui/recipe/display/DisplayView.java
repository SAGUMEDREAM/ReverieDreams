package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.inf.IGuiElementBuilderAccessor;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.view.RecipeEntryWrapper;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public interface DisplayView {
    default void init() {

    }

    public static SimpleGui create(Class<? extends SimpleGui> clazz, ServerPlayer player, RecipeEntryWrapper<?> key2ValueEntry, GuiOpeningPrevCallback prevGuiCallback) {
        try {
            Constructor<?> constructor = clazz.getConstructor(ServerPlayer.class, RecipeEntryWrapper.class, GuiOpeningPrevCallback.class);
            return (SimpleGui) constructor.newInstance(player, key2ValueEntry, prevGuiCallback);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
    }


    default GuiElementBuilder getGuiElementBuilder(ItemStackWrapper recipe) {
        GuiElementBuilder guiElementBuilder = new GuiElementBuilder();
        IGuiElementBuilderAccessor accessor = (IGuiElementBuilderAccessor) guiElementBuilder;
        accessor.reverie_dreams$setItemStack(recipe.getItemStack());
        return guiElementBuilder;
    }

    default String[][] getGrid() {
        return new String[][]{
                {"B", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
        };
    }
}
