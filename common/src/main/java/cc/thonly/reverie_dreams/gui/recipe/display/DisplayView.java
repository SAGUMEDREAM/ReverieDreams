package cc.thonly.reverie_dreams.gui.recipe.display;

import cc.thonly.reverie_dreams.gui.recipe.GuiOpeningPrevCallback;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.view.RecipeKeyEntry;
import cc.thonly.reverie_dreams.util.item.GuiElementBuilderSetter;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.server.level.ServerPlayer;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public interface DisplayView {
    default void init() {

    }

    public static SimpleGui create(Class<? extends SimpleGui> clazz, ServerPlayer player, RecipeKeyEntry<?> key2ValueEntry, GuiOpeningPrevCallback prevGuiCallback) {
        try {
            Constructor<?> constructor = clazz.getConstructor(ServerPlayer.class, RecipeKeyEntry.class, GuiOpeningPrevCallback.class);
            return (SimpleGui) constructor.newInstance(player, key2ValueEntry, prevGuiCallback);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            return null;
        }
    }


    default GuiElementBuilder getGuiElementBuilder(IngredientStack recipe) {
        GuiElementBuilder builder = new GuiElementBuilder();
        GuiElementBuilderSetter.setter(builder, recipe.build());
        return builder;
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
