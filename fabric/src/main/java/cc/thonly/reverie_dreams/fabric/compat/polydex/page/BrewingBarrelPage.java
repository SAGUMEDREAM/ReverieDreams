package cc.thonly.reverie_dreams.fabric.compat.polydex.page;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.recipe.entry.BrewingBarrelRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiPlaceholderItems;
import eu.pb4.polydex.api.v1.recipe.PolydexCategory;
import eu.pb4.polydex.api.v1.recipe.PolydexEntry;
import eu.pb4.polydex.api.v1.recipe.PolydexIngredient;
import eu.pb4.polydex.api.v1.recipe.PolydexPage;
import eu.pb4.polydex.api.v1.recipe.PolydexStack;
import eu.pb4.polydex.api.v1.recipe.PageBuilder;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Getter
public class BrewingBarrelPage implements PolydexPage {
    public static final Identifier id = ReverieDreams.id("recipe/brewing_barrel");
    public static final PolydexCategory CATEGORY = PolydexCategory.of(id);

    public static final ItemStack ICON = new GuiElementBuilder(
            RDBlocks.BREWING_BARREL.asItem()
    ).setName(
            Component.translatable(id.toLanguageKey())
    ).asStack();

    public final Identifier key;
    public final BrewingBarrelRecipe value;
    private final List<PolydexIngredient<?>> ingredients;
    private final PolydexStack<?> output;

    public BrewingBarrelPage(Identifier key, BrewingBarrelRecipe value) {
        this.key = key.withPrefix("recipe/");
        this.value = value;

        List<PolydexIngredient<?>> list = new ArrayList<>();

        for (var material : value.getMaterials()) {
            if (material.isEmpty()) {
                continue;
            }

            list.add(
                    PolydexIngredient.of(
                            Ingredient.of(material.getItem()),
                            material.getCount()
                    )
            );
        }

        this.ingredients = list;
        this.output = PolydexStack.of(value.getOutput().build());
    }

    @Override
    public Identifier identifier() {
        return this.key;
    }

    @Override
    public ItemStack typeIcon(ServerPlayer serverPlayerEntity) {
        return ICON;
    }

    @Override
    public ItemStack entryIcon(
            @Nullable PolydexEntry polydexEntry,
            ServerPlayer serverPlayerEntity
    ) {
        return this.value.getOutput().build();
    }

    @Override
    public void createPage(
            @Nullable PolydexEntry polydexEntry,
            ServerPlayer serverPlayerEntity,
            PageBuilder layout
    ) {
        int input = 0;

        // 第一行：9 个输入槽
        for (int col = 0; col < 9; col++) {
            ItemStack stack = RDGuiPlaceholderItems.EMPTY_SLOT.createStack();

            if (input < this.value.getMaterials().size()) {
                stack = this.value.getMaterials().get(input).build().copy();
            } else {
                stack = Items.AIR.getDefaultInstance();
            }

            layout.set(col, 0, stack);
            input++;
        }


        // 剩余位置
        for (int row = 1; row < 5; row++) {
            for (int col = 0; col < 9; col++) {
                layout.set(
                        col,
                        row,
                        RDGuiPlaceholderItems.EMPTY_SLOT.createStack()
                );
            }
        }

        // 第二行：箭头 + 输出
        layout.set(
                4,
                2,
                RDGuiPlaceholderItems.PROGRESS_TO_RESULT.createStack()
        );

        layout.set(
                6,
                2,
                this.value.getOutput().build().copy()
        );
    }

    @Override
    public List<PolydexIngredient<?>> ingredients() {
        return this.ingredients;
    }

    @Override
    public List<PolydexCategory> categories() {
        return List.of(CATEGORY);
    }

    @Override
    public boolean isOwner(
            MinecraftServer minecraftServer,
            PolydexEntry polydexEntry
    ) {
        return polydexEntry.isPartOf(this.output);
    }
}