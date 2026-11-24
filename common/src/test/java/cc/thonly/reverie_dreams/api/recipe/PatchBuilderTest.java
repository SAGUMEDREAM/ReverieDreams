package cc.thonly.reverie_dreams.api.recipe;

import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.recipe.BaseRecipe;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.util.item.CompatItemTuple;
import com.mojang.serialization.Codec;
import net.minecraft.SharedConstants;
import net.minecraft.resources.Identifier;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import static org.junit.jupiter.api.Assertions.*;

class PatchBuilderTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        for (var item : List.of(Items.AIR, Items.PAPER, Items.STICK)) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                        .set(DataComponents.MAX_STACK_SIZE, 64).build());
            }
        }
    }

    public static class Recipe extends BaseRecipe {
        public IngredientStack ingredient;
        private final boolean broken;
        public Recipe(int count, boolean broken) { ingredient = new IngredientStack(Items.PAPER.builtInRegistryHolder(), count, DataComponentPatch.EMPTY); this.broken = broken; }
        @Override public IngredientStack getOutput() { return ingredient; }
        public Builder toBuilder() {
            if (broken) throw new IllegalStateException("fixture broken builder");
            return new Builder(ingredient.getCount());
        }
    }
    public static class Builder {
        private final int count;
        public Builder(int count) { this.count = count; }
        public Recipe build() { return new Recipe(count, false); }
    }
    static class Type extends BaseRecipeType<Recipe> {
        @Override public void reload(ResourceManager manager) { }
        @Override public void bootstrap() { }
        @Override public List<Recipe> getMatches(List<IngredientStack> inputs) { return List.of(); }
        @Override public Boolean isMatch(IngredientStack a, IngredientStack b) { return false; }
        @Override public Codec<Recipe> getCodec() { return Codec.intRange(1, 64).xmap(value -> new Recipe(value, false), value -> value.ingredient.getCount()); }
        @Override public String getTypeId() { return "test"; }
        @Override public Identifier getId() { return Identifier.parse("test:test"); }
        @Override public Map<Identifier, Recipe> getRegistryView() { return new LinkedHashMap<>(registries); }
    }

    @Test void brokenBuilderAndInvalidPatchDoNotBlockValidNeighbors() {
        var type = new Type();
        type.add(Identifier.parse("test:a"), new Recipe(1, false));
        type.add(Identifier.parse("test:b"), new Recipe(1, true));
        type.add(Identifier.parse("test:c"), new Recipe(100, false));
        type.add(Identifier.parse("test:d"), new Recipe(2, false));
        var patches = new PatchBuilder<>(type);
        assertDoesNotThrow(() -> patches.add(new CompatItemTuple(Items.PAPER, Items.STICK)));
        assertEquals(2, patches.getRegistries().size());
        assertTrue(patches.getRegistries().containsKey(Identifier.parse("test:a_minecraft_stick")));
        assertTrue(patches.getRegistries().containsKey(Identifier.parse("test:d_minecraft_stick")));
        for (var patch : patches.getRegistries().values()) assertEquals(Items.STICK, patch.getOutput().getItem());
        assertEquals(Items.PAPER, type.getRegistryView().get(Identifier.parse("test:a")).getOutput().getItem());
    }
}
