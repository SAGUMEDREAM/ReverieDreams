package cc.thonly.reverie_dreams.fabric.compat.polydex.page;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
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

@Getter
public class DanmakuPage implements PolydexPage {
    public static final Identifier id = ReverieDreams.id("recipe/danmaku_table");
    public static final PolydexCategory CATEGORY = PolydexCategory.of(id);
    private static final Component TEXTURE = Component.empty();
    public static final ItemStack ICON = new GuiElementBuilder(RDBlocks.DANMAKU_CRAFTING_TABLE.asItem()).setName(Component.translatable(id.toLanguageKey())).asStack();
    public final Identifier key;
    public final DanmakuRecipe value;
    private final List<PolydexIngredient<?>> ingredients;
    private final PolydexStack<?> output;

    public DanmakuPage(Identifier key, DanmakuRecipe value) {
        this.key = key.withPrefix("recipe/");
        this.value = value;
        List<PolydexIngredient<?>> list = new ArrayList<>();
        for (var x : List.of(value.getDye(), value.getCore(), value.getPower(), value.getPoint(), value.getMaterial())) {
            if (x.getItem() == Items.AIR) {
                list.add(PolydexIngredient.of(Ingredient.of(Items.BARRIER), 1));
                continue;
            }
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
                {"A", "X", "S", "X", "D", "X", "F", "X", "G"},
                {"X", "X", "X", "X", "T", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "O", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
                {"X", "X", "X", "X", "X", "X", "X", "X", "X"},
        };
        for (int row = 0; row < views.length; row++) {
            for (int col = 0; col < views[row].length; col++) {
                layout.set(col, row, getViewStack(views[row][col]));
            }
        }
    }

    private ItemStack getViewStack(String s) {
        if (s.equals("X")) {
            return RDGuiItems.EMPTY_SLOT.createStack();
        } else if (s.equals("A")) {
            return this.value.getDye().build().copy();
        } else if (s.equals("S")) {
            return this.value.getCore().build().copy();
        } else if (s.equals("D")) {
            return this.value.getPower().build().copy();
        } else if (s.equals("F")) {
            return this.value.getPoint().build().copy();
        } else if (s.equals("G")) {
            return this.value.getMaterial().build().copy();
        } else if (s.equals("T")) {
            return RDGuiItems.PROGRESS_TO_RESULT_DOWN.createStack();
        } else if (s.equals("O")) {
            return this.value.getOutput().build().copy();
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
