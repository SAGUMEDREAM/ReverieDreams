package cc.thonly.reverie_dreams.compat.page;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.block.ModBlocks;
import cc.thonly.reverie_dreams.item.ModGuiItems;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import eu.pb4.polydex.api.v1.recipe.*;
import eu.pb4.sgui.api.elements.GuiElementBuilder;
import lombok.Getter;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DanmakuPage implements PolydexPage {
    public static final Identifier id = Touhou.id("recipe/danmaku_table");
    public static final PolydexCategory CATEGORY = PolydexCategory.of(id);
    private static final Text TEXTURE = Text.empty();
    public static final ItemStack ICON = new GuiElementBuilder(ModBlocks.DANMAKU_CRAFTING_TABLE.asItem()).setName(Text.translatable(id.toTranslationKey())).asStack();
    public final Identifier key;
    public final DanmakuRecipe value;
    private final List<PolydexIngredient<?>> ingredients;
    private final PolydexStack<?> output;

    public DanmakuPage(Identifier key, DanmakuRecipe value) {
        this.key = key.withPrefixedPath("recipe/");
        this.value = value;
        List<PolydexIngredient<?>> list = new ArrayList<>();
        for (var x : List.of(value.getDye(), value.getCore(), value.getPower(), value.getPoint(), value.getMaterial())) {
            if (x.getItem() == Items.AIR) {
                list.add(PolydexIngredient.of(Ingredient.ofItem(Items.BARRIER), 1));
                continue;
            }
            list.add(PolydexIngredient.of(Ingredient.ofItem(x.getItem()), x.getCount()));
        }
        this.ingredients = list;
        this.output = PolydexStack.of(this.value.getOutput().getItemStack());
    }

    @Override
    public Identifier identifier() {
        return key;
    }

    @Override
    public ItemStack typeIcon(ServerPlayerEntity serverPlayerEntity) {
        return ICON;
    }

    @Override
    public ItemStack entryIcon(@Nullable PolydexEntry polydexEntry, ServerPlayerEntity serverPlayerEntity) {
        return this.value.getOutput().getItemStack();
    }

    @Override
    public void createPage(@Nullable PolydexEntry polydexEntry, ServerPlayerEntity serverPlayerEntity, PageBuilder layout) {
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
            return ModGuiItems.EMPTY_SLOT.getDefaultStack();
        } else if (s.equals("A")) {
            return this.value.getDye().getItemStack().copy();
        } else if (s.equals("S")) {
            return this.value.getCore().getItemStack().copy();
        } else if (s.equals("D")) {
            return this.value.getPower().getItemStack().copy();
        } else if (s.equals("F")) {
            return this.value.getPoint().getItemStack().copy();
        } else if (s.equals("G")) {
            return this.value.getMaterial().getItemStack().copy();
        } else if (s.equals("T")) {
            return ModGuiItems.PROGRESS_TO_RESULT_DOWN.getDefaultStack();
        } else if (s.equals("O")) {
            return this.value.getOutput().getItemStack().copy();
        }
        return Items.AIR.getDefaultStack();
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
