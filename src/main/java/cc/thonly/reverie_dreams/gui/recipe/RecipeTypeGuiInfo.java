package cc.thonly.reverie_dreams.gui.recipe;

import cc.thonly.reverie_dreams.gui.BasePageGui;
import cc.thonly.reverie_dreams.gui.recipe.display.DisplayView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.lang.reflect.Constructor;

@AllArgsConstructor
@Getter
@Slf4j
public class RecipeTypeGuiInfo<T extends BasePageGui> {
    private final ItemStack icon;
    private final Identifier id;
    private final Class<T> clazz;
    private final Class<? extends DisplayView> viewClazz;
    private final RecipeTypeGetter registryGetter;
    private final GuiStackGetter getter;

    public T create(ServerPlayerEntity player, GuiOpeningPrevCallback callback) {
        T result = null;
        try {
            Constructor<T> constructor = this.clazz.getConstructor(ServerPlayerEntity.class, RecipeTypeGuiInfo.class, RecipeTypeInfo.class, GuiOpeningPrevCallback.class);
            T gui = constructor.newInstance(player, this, new RecipeTypeInfo(this.id.toTranslationKey(), this.registryGetter.get(), this.getter), callback);
            gui.open();
            result = gui;
        } catch (Exception exception) {
            log.error("Can't open gui", exception);
        }
        return result;
    }

    public T newInstance(ServerPlayerEntity player, GuiOpeningPrevCallback callback) {
        T result = null;
        try {
            Constructor<T> constructor = this.clazz.getConstructor(ServerPlayerEntity.class, RecipeTypeGuiInfo.class, RecipeTypeInfo.class, GuiOpeningPrevCallback.class);
            result = constructor.newInstance(player, this, new RecipeTypeInfo(this.id.toTranslationKey(), this.registryGetter.get(), this.getter), callback);
        } catch (Exception exception) {
            log.error("Can't open gui", exception);
        }
        return result;
    }

}
