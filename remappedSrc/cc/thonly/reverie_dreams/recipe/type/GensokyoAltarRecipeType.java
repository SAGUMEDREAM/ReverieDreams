package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.template.RoleFollowerArchiveItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.GensokyoAltarRecipe;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.resources.ResourceLocation;
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
        Map<ResourceLocation, Resource> resources = manager.listResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(Touhou.MOD_ID) && id.getPath().endsWith(".json");
        });
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            ResourceLocation registryKey = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceFirst("^gensokyo_altar_recipe/", "").replaceAll("\\.json$", ""));
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
        this.add(Touhou.id("role_archive"), new GensokyoAltarRecipe(ItemStackWrapper.of(ModItems.ROLE_ARCHIVE), List.of(
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2)
        ), ItemStackWrapper.of(ModItems.ROLE_ARCHIVE)));
        this.add(Touhou.id("copy_spell_card_template"), new GensokyoAltarRecipe(ItemStackWrapper.of(ModItems.SPELL_CARD_TEMPLATE), List.of(
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2),
                ItemStackWrapper.of(Items.DIAMOND, 2)
        ), ItemStackWrapper.of(ModItems.SPELL_CARD_TEMPLATE, 2)));
    }

    @Override
    public void bootstrap() {

    }

    public List<GensokyoAltarRecipe> getModifierRecipe(List<ItemStackWrapper> wrappers) {
        List<GensokyoAltarRecipe> matches = new ArrayList<>();
        ItemStackWrapper coreWrapper = wrappers.get(8);

        Predicate<Integer> isAllMatch = (amount) -> {
            return IntStream.range(0, 8).allMatch(i ->
                    wrappers.get(i).test(new ItemStack(Items.DIAMOND, amount))
            );
        };

        if (coreWrapper.getItem() instanceof RoleFollowerArchiveItem && isAllMatch.test(2)) {
            ItemStack itemStack = coreWrapper.copy().getItemStack();
            itemStack.set(ModDataComponentTypes.ROLE_CAN_RESPAWN, true);

            matches.add(new GensokyoAltarRecipe(
                    coreWrapper,
                    Collections.nCopies(8, ItemStackWrapper.of(Items.DIAMOND, 2)),
                    ItemStackWrapper.of(itemStack)
            ));
        }

        if (coreWrapper.getItem() instanceof SpellCardTemplateItem && isAllMatch.test(2)) {
            ItemStack itemStack = coreWrapper.copy().getItemStack();
            itemStack.setCount(2);

            matches.add(new GensokyoAltarRecipe(
                    coreWrapper,
                    Collections.nCopies(8, ItemStackWrapper.of(Items.DIAMOND, 2)),
                    ItemStackWrapper.of(itemStack)
            ));
        }

        return matches;
    }


    @Override
    public List<GensokyoAltarRecipe> getMatches(List<ItemStackWrapper> wrappers) {
        if (wrappers.size() < 8) return List.of();
        List<GensokyoAltarRecipe> matches = this.getModifierRecipe(wrappers);

        if (matches.isEmpty()) {
            for (GensokyoAltarRecipe recipe : stream().toList()) {
                List<ItemStackWrapper> slots = recipe.getSlots();
                ItemStackWrapper slot0 = slots.get(0);
                ItemStackWrapper slot1 = slots.get(1);
                ItemStackWrapper slot2 = slots.get(2);
                ItemStackWrapper slot3 = slots.get(3);
                ItemStackWrapper slot4 = slots.get(4);
                ItemStackWrapper slot5 = slots.get(5);
                ItemStackWrapper slot6 = slots.get(6);
                ItemStackWrapper slot7 = slots.get(7);
                ItemStackWrapper slot8 = recipe.getCore();
                if (
                        wrappers.get(0).test(slot0.getItemStack()) &&
                                wrappers.get(1).test(slot1.getItemStack()) &&
                                wrappers.get(2).test(slot2.getItemStack()) &&
                                wrappers.get(3).test(slot3.getItemStack()) &&
                                wrappers.get(4).test(slot4.getItemStack()) &&
                                wrappers.get(5).test(slot5.getItemStack()) &&
                                wrappers.get(6).test(slot6.getItemStack()) &&
                                wrappers.get(7).test(slot7.getItemStack()) &&
                                wrappers.get(8).test(slot8.getItemStack())
                ) {
                    matches.add(recipe);
                }
            }
        }

        return matches;
    }

    @Override
    public Boolean isMatch(ItemStackWrapper input, ItemStackWrapper recipe) {
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
    public ResourceLocation getId() {
        return Touhou.id(this.getTypeId());
    }
}
