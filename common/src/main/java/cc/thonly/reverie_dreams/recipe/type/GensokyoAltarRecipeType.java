package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.ItemComparatorView;
import cc.thonly.reverie_dreams.item.template.RoleFollowerArchiveItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponentTypes;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import cc.thonly.reverie_dreams.recipe.RecipeResourceLoader;

@Slf4j
public class GensokyoAltarRecipeType extends BaseRecipeType<GensokyoAltarRecipe> {
    private static GensokyoAltarRecipeType INSTANCE;

    public GensokyoAltarRecipeType() {
        INSTANCE = this;
    }

    public static synchronized GensokyoAltarRecipeType getInstance() {
        return INSTANCE;
    }

    @Override
    public void reload(ResourceManager manager) {
        RecipeResourceLoader.load(manager, this.getTypeId() + "_recipe",
                id -> true, this.getCodec(), this::add);
        this.registerDynamicRecipe();
    }

    public void registerDynamicRecipe() {
        this.add(ReverieDreams.id("role_archive"), new GensokyoAltarRecipe(IngredientStack.of(RDItems.ROLE_ARCHIVE), List.of(
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2)
        ), IngredientStack.of(RDItems.ROLE_ARCHIVE)));
        this.add(ReverieDreams.id("copy_spell_card_template"), new GensokyoAltarRecipe(IngredientStack.of(RDItems.SPELL_CARD_TEMPLATE), List.of(
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2),
                IngredientStack.of(Items.DIAMOND, 2)
        ), IngredientStack.of(RDItems.SPELL_CARD_TEMPLATE, 2)));
    }

    @Override
    public void bootstrap() {

    }

    public List<GensokyoAltarRecipe> getModifierRecipe(List<IngredientStack> wrappers) {
        List<GensokyoAltarRecipe> matches = new ArrayList<>();
        IngredientStack coreWrapper = wrappers.get(8);

        Predicate<Integer> isAllMatch = (amount) -> {
            return IntStream.range(0, 8).allMatch(i ->
                    ItemComparatorView.of(new ItemStack(Items.DIAMOND, amount))
                            .greaterThan(ItemComparatorView.of(wrappers.get(i)))
            );
        };

        if (coreWrapper.getItem() instanceof RoleFollowerArchiveItem && isAllMatch.test(2)) {
            ItemStack itemStack = coreWrapper.build();
            itemStack.set(RDDataComponentTypes.ROLE_CAN_RESPAWN.value(), true);

            matches.add(new GensokyoAltarRecipe(
                    coreWrapper,
                    Collections.nCopies(8, IngredientStack.of(Items.DIAMOND, 2)),
                    IngredientStack.of(itemStack)
            ));
        }

        if (coreWrapper.getItem() instanceof SpellCardTemplateItem && isAllMatch.test(2)) {
            ItemStack itemStack = coreWrapper.build();
            itemStack.setCount(2);

            matches.add(new GensokyoAltarRecipe(
                    coreWrapper,
                    Collections.nCopies(8, IngredientStack.of(Items.DIAMOND, 2)),
                    IngredientStack.of(itemStack)
            ));
        }

        return matches;
    }


    private static boolean matchesSlot(IngredientStack required, IngredientStack input) {
        if (required.isEmpty()) {
            return input.isEmpty();
        }
        return ItemComparatorView.of(required).greaterThan(ItemComparatorView.of(input));
    }

    public SlotIssue findSingleSlotFix(List<IngredientStack> inputs) {
        if (inputs.size() != 9 || inputs.stream().allMatch(IngredientStack::isEmpty)) return null;
        SlotIssue suggestion = null;
        for (GensokyoAltarRecipe recipe : stream().toList()) {
            int mismatch = -1;
            int matchedMaterials = 0;
            for (int slot = 0; slot < 9; slot++) {
                IngredientStack required = slot == 8 ? recipe.getCore() : recipe.getSlots().get(slot);
                if (matchesSlot(required, inputs.get(slot))) {
                    if (!required.isEmpty()) matchedMaterials++;
                    continue;
                }
                if (mismatch >= 0) {
                    mismatch = -2;
                    break;
                }
                mismatch = slot;
            }
            if (mismatch < 0 || matchedMaterials == 0) continue;
            if (suggestion != null) return null;
            suggestion = new SlotIssue(mismatch, mismatch == 8 ? recipe.getCore() : recipe.getSlots().get(mismatch));
        }
        return suggestion;
    }

    public record SlotIssue(int index, IngredientStack required) { }

    @Override
    public List<GensokyoAltarRecipe> getMatches(List<IngredientStack> wrappers) {
        if (wrappers.size() < 9)
            return List.of();
        List<GensokyoAltarRecipe> matches = this.getModifierRecipe(wrappers);

        if (matches.isEmpty()) {
            for (GensokyoAltarRecipe recipe : stream().toList()) {
                List<IngredientStack> slots = recipe.getSlots();
                IngredientStack slot0 = slots.get(0);
                IngredientStack slot1 = slots.get(1);
                IngredientStack slot2 = slots.get(2);
                IngredientStack slot3 = slots.get(3);
                IngredientStack slot4 = slots.get(4);
                IngredientStack slot5 = slots.get(5);
                IngredientStack slot6 = slots.get(6);
                IngredientStack slot7 = slots.get(7);
                IngredientStack slot8 = recipe.getCore();
                if (
                        matchesSlot(slot0, wrappers.get(0)) &&
                                matchesSlot(slot1, wrappers.get(1)) &&
                                matchesSlot(slot2, wrappers.get(2)) &&
                                matchesSlot(slot3, wrappers.get(3)) &&
                                matchesSlot(slot4, wrappers.get(4)) &&
                                matchesSlot(slot5, wrappers.get(5)) &&
                                matchesSlot(slot6, wrappers.get(6)) &&
                                matchesSlot(slot7, wrappers.get(7)) &&
                                matchesSlot(slot8, wrappers.get(8))
                ) {
                    matches.add(recipe);
                }
            }
        }

        return matches;
    }

    @Override
    public Boolean isMatch(IngredientStack input, IngredientStack recipe) {
        return false;
    }

    @Override
    public Codec<GensokyoAltarRecipe> getCodec() {
        return GensokyoAltarRecipe.CODEC;
    }

    @Override
    public String getTypeId() {
        return "gensokyo_altar";
    }

    @Override
    public Identifier getId() {
        return ReverieDreams.id(this.getTypeId());
    }
}
