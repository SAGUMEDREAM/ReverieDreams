package cc.thonly.reverie_dreams.compat.page;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import eu.pb4.polydex.api.v1.recipe.*;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import lombok.Getter;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
public class StrengthTablePage implements PolydexPage {
    public static final ResourceLocation id = ReverieDreams.id("recipe/strength_table");
    public static final PolydexCategory CATEGORY = PolydexCategory.of(id);
    private static final Component TEXTURE = Component.empty();
    public static final ItemStack ICON = new GuiElementBuilder(RDBlocks.STRENGTH_TABLE.asItem()).setName(Component.translatable(id.toLanguageKey())).asStack();
    public final ResourceLocation key;
    public final StrengthTableRecipe value;
    private final List<PolydexIngredient<?>> ingredients;
    private final PolydexStack<?> output;

    public StrengthTablePage(ResourceLocation key, StrengthTableRecipe value) {
        this.key = key.withPrefix("recipe/");
        this.value = value;
        List<PolydexIngredient<?>> list = new ArrayList<>();

        for (var x : List.of(value.getMainItem(), value.getOffItem())) {
            if (x.isEmpty()) continue;
            list.add(PolydexIngredient.of(Ingredient.of(x.getItem()), x.getCount()));
        }
        this.ingredients = list;
        this.output = PolydexStack.of(this.value.getOutput().getItemStack());
    }

    @Override
    public ResourceLocation identifier() {
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
        String[][] views = {
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "I", "I", "X", "T", "X", "I", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
        };

        AtomicInteger input = new AtomicInteger(0);
        for (int row = 0; row < views.length; row++) {
            for (int col = 0; col < views[row].length; col++) {
                layout.set(col, row, getViewStack(input, views[row][col]));
            }
        }
    }

    private ItemStack getViewStack(AtomicInteger input, String s) {
        if (s.equals("X")) {
            return RDGuiItems.EMPTY_SLOT.getDefaultInstance();
        } else if (s.equals("I")) {
            int i = input.get();
            input.incrementAndGet();
            if (i == 0) {
                return this.value.getMainItem().getItemStack().copy();
            } else if (i == 1) {
                return this.value.getOffItem().getItemStack().copy();
            } else if (i == 2) {
                return this.value.getOutput().getItemStack().copy();
            }
        } else if (s.equals("T")) {
            return RDGuiItems.PROGRESS_TO_RESULT.getDefaultInstance();
        }
        return Items.AIR.getDefaultInstance();
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
