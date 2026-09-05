package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.fabric.compat.polydex.page.*;
import cc.thonly.reverie_dreams.recipe.type.*;
import eu.pb4.polydex.api.v1.recipe.PolydexEntry;
import eu.pb4.polydex.api.v1.recipe.PolydexPage;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;

@SuppressWarnings("unused")
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
        createRecipeView(BrewingBarrelRecipeType.getInstance().getRegistryView(), BrewingBarrelPage::new, pageConsumer);
    }

    private static <T, R extends PolydexPage> void createRecipeView(
            Map<Identifier, T> view,
            BiFunction<Identifier, T, R> pageFactory,
            Consumer<PolydexPage> consumer) {
        view.forEach((id, recipe) -> {
            consumer.accept(pageFactory.apply(id, recipe));
        });
    }


    private static PolydexEntry ofEntry(Identifier id, ItemStack stack) {
        return PolydexEntry.of(id, stack);
    }
}
