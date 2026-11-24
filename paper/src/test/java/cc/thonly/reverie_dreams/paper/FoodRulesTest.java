package cc.thonly.reverie_dreams.paper;

import cc.thonly.reverie_dreams.paper.food.FoodRules;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class FoodRulesTest {
    private FoodRules rules() throws Exception {
        try (var reader = new InputStreamReader(getClass().getResourceAsStream("/migration/food-domain.json"), StandardCharsets.UTF_8)) {
            return new FoodRules(JsonParser.parseReader(reader).getAsJsonObject());
        }
    }

    @Test
    void defaultCuisineBonusesAndEmptyOverrideUseOriginalProperties() throws Exception {
        var plan = rules().plan("reverie_dreams:cuisine/roasted_mushrooms", List.of(), List.of(), true, false, new Random(1));
        assertEquals(6, plan.nutrition());
        assertEquals(9f, plan.saturation());
        assertTrue(plan.effects().isEmpty());
    }

    @Test
    void explicitStackPropertiesReplaceDefaultsAndIngredientsSkipBonuses() throws Exception {
        var plan = rules().plan("reverie_dreams:cuisine/roasted_mushrooms", List.of("reverie_dreams:spicy"), List.of(), true, false, new Random(1));
        assertEquals(1, plan.nutrition());
        assertEquals(60, plan.fireTicks());
        assertEquals(List.of("minecraft:strength", "minecraft:resistance"), plan.effects().stream().map(FoodRules.Effect::id).toList());
        var ingredient = rules().plan("minecraft:beef", List.of("reverie_dreams:spicy"), List.of(), true, false, new Random(1));
        assertEquals(0, ingredient.nutrition());
        assertTrue(ingredient.effects().isEmpty());
    }

    @Test
    void milkProtectionFiltersOnlyNegativeBeverageEffects() throws Exception {
        var milk = rules().plan("reverie_dreams:beverage/milk", List.of(), List.of(), true, false, new Random(1));
        assertEquals(1500, milk.alcoholProtectionTicks());
        var unprotected = rules().plan("minecraft:bread", List.of(), List.of("reverie_dreams:high_alcohol"), true, false, new Random(1));
        var protectedPlan = rules().plan("minecraft:bread", List.of(), List.of("reverie_dreams:high_alcohol"), true, true, new Random(1));
        assertEquals(4, unprotected.effects().size());
        assertEquals(List.of("minecraft:strength", "minecraft:resistance"), protectedPlan.effects().stream().map(FoodRules.Effect::id).toList());
    }

    @Test
    void bizarreChoosesOneRandomBranchInsteadOfCombiningBoth() throws Exception {
        var domainRules = rules();
        var random = new Random(41);
        var outcomes = new HashSet<List<FoodRules.Effect>>();
        for (int i = 0; i < 64; i++) {
            var plan = domainRules.plan("reverie_dreams:cuisine/roasted_mushrooms", List.of("reverie_dreams:bizarre"), List.of(), true, false, random);
            assertEquals(1, plan.nutrition());
            assertTrue(plan.effects().stream().anyMatch(effect -> effect.id().equals("minecraft:nausea")));
            outcomes.add(plan.effects());
        }
        assertEquals(2, outcomes.size());
    }
}
