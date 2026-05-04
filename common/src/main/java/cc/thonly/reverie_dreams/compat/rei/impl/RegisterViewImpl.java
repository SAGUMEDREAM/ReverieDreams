package cc.thonly.reverie_dreams.compat.rei.impl;

import cc.thonly.reverie_dreams.compat.rei.IDisplayRegisterView;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import lombok.Getter;
import me.shedaniel.rei.api.common.display.Display;
import me.shedaniel.rei.api.common.registry.display.DisplayConsumer;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class RegisterViewImpl implements IDisplayRegisterView {
    private final DisplayConsumer registry;
    @Getter
    private final boolean client;

    public RegisterViewImpl(DisplayConsumer registry, boolean client) {
        this.registry = registry;
        this.client = client;
    }

    @Override
    public <R extends BaseRecipe, T extends Display> void register(
            Iterable<R> recipes,
            Function<R, T> mapper
    ) {
        for (R recipe : recipes) {
            if (recipe == null) {
                continue;
            }
            this.registry.add(mapper.apply(recipe));
        }
    }

    @Override
    public <R extends BaseRecipe, T extends Display> void registerMap(
            Map<Identifier, R> map,
            Function<R, T> mapper
    ) {
        register(map.values().stream().filter(Objects::nonNull).toList(), mapper);
    }

    @Override
    public <R extends BaseRecipe, T extends Display> void registerType(
            BaseRecipeType<R> recipeType,
            Function<R, T> mapper
    ) {
        register(recipeType.getRegistryView().values().stream().filter(Objects::nonNull).toList(), mapper);
    }

    @Override
    public DisplayConsumer getDisplayRegistry() {
        return this.registry;
    }
}
