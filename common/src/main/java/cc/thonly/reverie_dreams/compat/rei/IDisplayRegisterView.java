package cc.thonly.reverie_dreams.compat.rei;

import cc.thonly.reverie_dreams.compat.rei.impl.RegisterViewImpl;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.registry.display.DisplayConsumer;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Function;

public interface IDisplayRegisterView {
    Map<String, IDisplayRegisterView> IMPLS = new Object2ObjectOpenHashMap<>();

    static IDisplayRegisterView getRecipeRegisters(DisplayConsumer registry) {
        return IMPLS.computeIfAbsent("common", x -> new RegisterViewImpl(registry, false));
    }

    static IDisplayRegisterView getClientRecipeRegisters(DisplayConsumer registry) {
        return IMPLS.computeIfAbsent("client", x -> new RegisterViewImpl(registry, true));
    }

    <R extends BaseRecipe, T extends Display> void register(
            Iterable<R> recipes,
            Function<R, T> mapper
    );

    <R extends BaseRecipe, T extends Display> void registerMap(
            Map<Identifier, R> map,
            Function<R, T> mapper
    );

    <R extends BaseRecipe, T extends Display> void registerType(
            BaseRecipeType<R> recipeType,
            Function<R, T> mapper
    );

    @SuppressWarnings("unchecked")
    default <T extends DisplayConsumer> T cast(Class<T> tClass) {
        return (T) this.getDisplayRegistry();
    }

    boolean isClient();

    DisplayConsumer getDisplayRegistry();
}
