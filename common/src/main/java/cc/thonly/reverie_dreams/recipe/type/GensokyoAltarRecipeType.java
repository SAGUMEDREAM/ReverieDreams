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
import cc.thonly.reverie_dreams.util.item.ItemUtils;
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
        Map<Identifier, Resource> resources = manager.listResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json");
        });
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            Identifier registryKey = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceFirst("^gensokyo_altar_recipe/", "").replaceAll("\\.json$", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<GensokyoAltarRecipe> result = this.getCodec().parse(input);

                result.resultOrPartial(error -> log.error("Failed to load gensokyo altar recipe {}, {}", id, error))
                      .ifPresent(recipe -> {
                          this.add(registryKey, recipe);
                      });
            } catch (IOException e) {
                log.error("Failed to load gensokyo altar recipe {}, {}, {}", id, e.getMessage(), e);
            }
        }
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
                    ItemComparatorView.of(wrappers.get(i)).test(ItemComparatorView.of(new ItemStack(Items.DIAMOND, amount)))
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


    @Override
    public List<GensokyoAltarRecipe> getMatches(List<IngredientStack> wrappers) {
        if (wrappers.size() < 8)
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
                        ItemComparatorView.of(wrappers.get(0)).test(ItemComparatorView.of(slot0)) &&
                                ItemComparatorView.of(wrappers.get(1)).map(ItemUtils::updateItemStackTag).test(ItemComparatorView.of(slot1).map(ItemUtils::updateItemStackTag)) &&
                                ItemComparatorView.of(wrappers.get(2)).map(ItemUtils::updateItemStackTag).test(ItemComparatorView.of(slot2).map(ItemUtils::updateItemStackTag)) &&
                                ItemComparatorView.of(wrappers.get(3)).map(ItemUtils::updateItemStackTag).test(ItemComparatorView.of(slot3).map(ItemUtils::updateItemStackTag)) &&
                                ItemComparatorView.of(wrappers.get(4)).map(ItemUtils::updateItemStackTag).test(ItemComparatorView.of(slot4).map(ItemUtils::updateItemStackTag)) &&
                                ItemComparatorView.of(wrappers.get(5)).map(ItemUtils::updateItemStackTag).test(ItemComparatorView.of(slot5).map(ItemUtils::updateItemStackTag)) &&
                                ItemComparatorView.of(wrappers.get(6)).map(ItemUtils::updateItemStackTag).test(ItemComparatorView.of(slot6).map(ItemUtils::updateItemStackTag)) &&
                                ItemComparatorView.of(wrappers.get(7)).map(ItemUtils::updateItemStackTag).test(ItemComparatorView.of(slot7).map(ItemUtils::updateItemStackTag)) &&
                                ItemComparatorView.of(wrappers.get(8)).map(ItemUtils::updateItemStackTag).test(ItemComparatorView.of(slot8).map(ItemUtils::updateItemStackTag))
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
