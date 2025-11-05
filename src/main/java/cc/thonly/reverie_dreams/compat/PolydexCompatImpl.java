package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.recipe.type.KitchenRecipeType;
import cc.thonly.reverie_dreams.compat.page.*;
import cc.thonly.reverie_dreams.recipe.type.DanmakuRecipeType;
import cc.thonly.reverie_dreams.recipe.type.DanmakuShapeDrawRecipeType;
import cc.thonly.reverie_dreams.recipe.type.GensokyoAltarRecipeType;
import cc.thonly.reverie_dreams.recipe.type.StrengthTableRecipeType;
import eu.pb4.polydex.api.v1.recipe.PolydexEntry;
import eu.pb4.polydex.api.v1.recipe.PolydexPage;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PolydexCompatImpl {
    public static void bootstrap(
    ) {
        PolydexPage.register(PolydexCompatImpl::createPages);
    }

    private static void createPages(MinecraftServer minecraftServer, Consumer<PolydexPage> pageConsumer) {
        createRecipeView(DanmakuRecipeType.getInstance().getRegistryView(), DanmakuPage::new, pageConsumer);
        createRecipeView(DanmakuShapeDrawRecipeType.getInstance().getRegistryView(), DanmakuShapePage::new, pageConsumer);
        createRecipeView(GensokyoAltarRecipeType.getInstance().getRegistryView(), GensokyoAltarPage::new, pageConsumer);
        createRecipeView(StrengthTableRecipeType.getInstance().getRegistryView(), StrengthTablePage::new, pageConsumer);
        createRecipeView(KitchenRecipeType.getInstance().getRegistryView(), KitchenPage::new, pageConsumer);
    }

    private static <T, R extends PolydexPage> void createRecipeView(
            Map<ResourceLocation, T> view,
            BiFunction<ResourceLocation, T, R> pageFactory,
            Consumer<PolydexPage> consumer) {
        view.forEach((id, recipe) -> {
            consumer.accept(pageFactory.apply(id, recipe));
        });
    }


    private static PolydexEntry ofEntry(ResourceLocation id, ItemStack stack) {
        return PolydexEntry.of(id, stack);
    }
}
