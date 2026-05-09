package cc.thonly.reverie_dreams.fabric.compat.polydex.page;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.item.RDGuiItems;
import eu.pb4.polydex.api.v1.recipe.*;
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
public class GensokyoAltarPage implements PolydexPage {
    public static final Identifier id = ReverieDreams.id("recipe/gensokyo_altar");
    public static final PolydexCategory CATEGORY = PolydexCategory.of(id);
    private static final Component TEXTURE = Component.empty();
    public static final ItemStack ICON = new GuiElementBuilder(RDBlocks.GENSOKYO_ALTAR.asItem()).setName(Component.translatable(id.toLanguageKey())).asStack();
    public final Identifier key;
    public final GensokyoAltarRecipe value;
    private final List<PolydexIngredient<?>> ingredients;
    private final PolydexStack<?> output;

    public GensokyoAltarPage(Identifier key, GensokyoAltarRecipe value) {
        this.key = key.withPrefix("recipe/");
        this.value = value;
        List<PolydexIngredient<?>> list = new ArrayList<>();
        if (!value.getCore().isEmpty()) {
            list.add(PolydexIngredient.of(Ingredient.of(value.getCore().getItem()), value.getCore().getCount()));
        }
        for (var x : value.getSlots()) {
            if (x.isEmpty()) continue;
            list.add(PolydexIngredient.of(Ingredient.of(x.getItem()), x.getCount()));
        }
        this.ingredients = list;
        this.output = PolydexStack.of(this.value.getOutput().build());
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
        return this.value.getOutput().build();
    }

    @Override
    public void createPage(@Nullable PolydexEntry polydexEntry, ServerPlayer serverPlayerEntity, PageBuilder layout) {
        String[][] views = {
                {"X", "X", "I", "X", "I", "X", "I", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "I", "X", "C", "X", "I", "T", "O"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "I", "X", "I", "X", "I", "X", "X"},
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
            return RDGuiItems.EMPTY_SLOT.createStack();
        } else if (s.equals("C")) {
            return this.value.getCore().build().copy();
        } else if (s.equals("I")) {
            int i = input.get();
            input.incrementAndGet();
            if (i < this.value.getSlots().size()) {
                return this.value.getSlots().get(i).build().copy();
            }
        } else if (s.equals("O")) {
            return this.value.getOutput().build().copy();
        } else if (s.equals("T")) {
            return RDGuiItems.PROGRESS_TO_RESULT.createStack();
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
