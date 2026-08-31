package cc.thonly.reverie_dreams.recipe;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.entry.DanmakuRecipe;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.registry.content.block.RDBlocks;
import cc.thonly.reverie_dreams.registry.content.block.entity.RDBlockEntityTypes;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

public class RecipeWorkbenchRegistry {
    private static final Map<String, RecipeWorkbench<?>> VALUES = new Object2ObjectLinkedOpenHashMap<>();

    public static void bootstrap() {
        register("gensokyo_altar", () -> new RecipeWorkbench<>("gensokyo_altar", RDBlocks.GENSOKYO_ALTAR.asBlock(), RecipeManager.GENSOKYO_ALTAR, (registryAccess, recipeId, self) -> {
            BaseRecipeType<GensokyoAltarRecipe> recipeType = self.getRecipeType();
            GensokyoAltarRecipe recipeById = recipeType.getRecipeById(recipeId);
            List<IngredientStack> slots = recipeById.getSlots();
            Block first = self.getBlock().getFirst();
            ItemStack itemStack = first.asItem().getDefaultInstance();
            SimpleContainer inventory = new SimpleContainer(9);
            inventory.setItem(0, slots.get(0).build().copy());
            inventory.setItem(1, slots.get(1).build().copy());
            inventory.setItem(2, slots.get(2).build().copy());

            inventory.setItem(3, slots.get(3).build().copy());
            inventory.setItem(4, slots.get(4).build().copy());
            inventory.setItem(5, slots.get(5).build().copy());

            inventory.setItem(6, slots.get(6).build().copy());
            inventory.setItem(7, slots.get(7).build().copy());
            inventory.setItem(8, recipeById.getCore().build().copy());
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(RDBlockEntityTypes.GENSOKYO_ALTAR::toString, LogUtils.getLogger())) {
                TagValueOutput output = TagValueOutput.createWithContext(logging, registryAccess);
                ContainerHelper.saveAllItems(output, inventory.getItems());
                itemStack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(RDBlockEntityTypes.GENSOKYO_ALTAR.value(), output.buildResult()));
            }
            return itemStack;
        }));
        register("strength_table", () -> new RecipeWorkbench<>("strength_table", RDBlocks.STRENGTH_TABLE.asBlock(), RecipeManager.STRENGTH_TABLE, (registryAccess, recipeId, self) -> {
            Block first = self.getBlock().getFirst();
            BaseRecipeType<StrengthTableRecipe> recipeType = self.getRecipeType();
            StrengthTableRecipe recipeById = recipeType.getRecipeById(recipeId);
            ItemStack itemStack = first.asItem().getDefaultInstance();
            SimpleContainer inventory = new SimpleContainer(2);
            inventory.setItem(0, recipeById.getMainItem().build().copy());
            inventory.setItem(1, recipeById.getOffItem().build().copy());
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(RDBlockEntityTypes.STRENGTH_TABLE::toString, LogUtils.getLogger())) {
                TagValueOutput output = TagValueOutput.createWithContext(logging, registryAccess);
                ContainerHelper.saveAllItems(output, inventory.getItems());
                itemStack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(RDBlockEntityTypes.STRENGTH_TABLE.value(), output.buildResult()));
            }
            return itemStack;
        }));
        register("danmaku_crafting_table", () -> new RecipeWorkbench<>("danmaku_crafting_table", RDBlocks.DANMAKU_CRAFTING_TABLE.asBlock(), RecipeManager.DANMAKU, (registryAccess, recipeId, self) -> {
            Block first = self.getBlock().getFirst();
            BaseRecipeType<DanmakuRecipe> recipeType = self.getRecipeType();
            DanmakuRecipe recipeById = recipeType.getRecipeById(recipeId);
            ItemStack itemStack = first.asItem().getDefaultInstance();
            SimpleContainer inventory = new SimpleContainer(5);
            inventory.setItem(0, recipeById.getDye().build().copy());
            inventory.setItem(1, recipeById.getCore().build().copy());
            inventory.setItem(2, recipeById.getPower().build().copy());
            inventory.setItem(3, recipeById.getPoint().build().copy());
            inventory.setItem(4, recipeById.getMaterial().build().copy());
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(RDBlockEntityTypes.DANMAKU_CRAFTING_TABLE::toString, LogUtils.getLogger())) {
                TagValueOutput output = TagValueOutput.createWithContext(logging, registryAccess);
                ContainerHelper.saveAllItems(output, inventory.getItems());
                itemStack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(RDBlockEntityTypes.DANMAKU_CRAFTING_TABLE.value(), output.buildResult()));
            }
            return itemStack;
        }));
    }

    public static <R extends BaseRecipe> void register(String name, Supplier<RecipeWorkbench<R>> value) {
        ReverieDreams.COMMON_LATE_INIT.add(() -> VALUES.put(name, value.get()));
    }

    public static Set<Map.Entry<String, RecipeWorkbench<?>>> entries() {
        return VALUES.entrySet();
    }

    public static <R extends BaseRecipe> String getKey(RecipeWorkbench<R> recipeWorkbench) {
        boolean b = VALUES.containsValue(recipeWorkbench);
        if (!b) {
            return null;
        }
        for (Map.Entry<String, RecipeWorkbench<?>> mapEntry : VALUES.entrySet()) {
            if (Objects.equals(mapEntry.getValue(), recipeWorkbench)) {
                return mapEntry.getKey();
            }
        }
        return null;
    }

    public static RecipeWorkbench<?> getValue(String name) {
        return VALUES.get(name);
    }
}
