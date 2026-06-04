package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
import cc.thonly.reverie_dreams.data.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.item.IngredientStack;
import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.danmaku.DanmakuTemplates;
import cc.thonly.reverie_dreams.registry.content.item.RDItems;
import cc.thonly.reverie_dreams.util.item.ItemStackTemplateHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@SuppressWarnings("unchecked")
public class StrengthTableRecipeType extends BaseRecipeType<StrengthTableRecipe> {
    private static StrengthTableRecipeType INSTANCE;
    private final Map<String, Integer> automaticRecipeIdCounter = new Object2ObjectOpenHashMap<>();
    private final LinkedHashMap<Identifier, StrengthTableRecipe> dynamicBuilder = new LinkedHashMap<>();
    private static final float MAX_SPEED = 2.5f;
    private static final int MAX_COUNT = 3;
    private static final float MAX_DAMAGE = 5.5f;

    public StrengthTableRecipeType() {
        INSTANCE = this;
    }

    public static synchronized StrengthTableRecipeType getInstance() {
        return INSTANCE;
    }

    @Override
    public void reload(ResourceManager manager) {
        this.dynamicBuilder.clear();
        this.automaticRecipeIdCounter.clear();
        Map<Identifier, Resource> resources = manager.listResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json");
        });
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            Identifier registryKey = Identifier.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceFirst("^strength_table_recipe/", "").replaceAll("\\.json$", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.open()) {
                JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);

                DataResult<StrengthTableRecipe> result = this.getCodec().parse(input);

                result.resultOrPartial(error -> log.error("Failed to load strength table recipe {}, {}", id, error))
                        .ifPresent(recipe -> {
                            this.add(registryKey, recipe);
                        });
            } catch (IOException e) {
                log.error("Failed to load strength table recipe {}, {}, {}", id, e.getMessage(), e);
            }
        }
        List<Item> danmakuItemView = RegistryImpls.DANMAKU_TYPE
                .values().stream().map(DanmakuType::getItemHolder).map(ItemLike::asItem).toList();
        List<ItemStackTemplate> danmakuItemStackView = danmakuItemView.stream().map(ItemStackTemplate::new).toList();
        List<ItemStackTemplate> templateStackView = DanmakuTemplates.getRegistryItemStackView().values().stream().toList();

        this.registerAutomaticDynamic(danmakuItemStackView, templateStackView, RDDataComponents.DANMAKU_PROPERTIES.value());
        this.registerAutomaticDynamic(danmakuItemStackView, List.of(new ItemStackTemplate(RDItems.SPEED_FEATHER.asItem())), RDDataComponents.DANMAKU_PROPERTIES.value());
        this.registerAutomaticDynamic(danmakuItemStackView, List.of(new ItemStackTemplate(Items.SLIME_BLOCK)), RDDataComponents.DANMAKU_PROPERTIES.value());
        this.registerAutomaticDynamic(danmakuItemStackView, List.of(new ItemStackTemplate(Items.IRON_SWORD)), RDDataComponents.DANMAKU_PROPERTIES.value());

        Map<Identifier, StrengthTableRecipe> sortedByKey = this.dynamicBuilder.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        Map::putAll
                );
        sortedByKey.forEach(this::add);
    }

    @SuppressWarnings("rawtypes")
    private void registerAutomaticDynamic(List<ItemStackTemplate> main, List<ItemStackTemplate> off, DataComponentType componentType) {
        ItemStackTemplate[] mainItems = main.toArray(new ItemStackTemplate[0]);
        ItemStackTemplate[] offItems = off.toArray(new ItemStackTemplate[0]);

        for (ItemStackTemplate mainItem : mainItems) {
            for (ItemStackTemplate offItem : offItems) {
                String value = null;
                try {
                    String mainItemIdStr = BuiltInRegistries.ITEM.getKey(mainItem.item().value()).getPath();
                    String offItemIdStr = BuiltInRegistries.ITEM.getKey(offItem.item().value()).getPath();
                    String builder = mainItemIdStr + offItemIdStr;
                    Integer num = this.automaticRecipeIdCounter.computeIfAbsent(builder, (x) -> 0);
                    String builderByCounter = builder + "_" + num;
                    this.automaticRecipeIdCounter.put(builder, ++num);
                    ItemStackTemplate outputStack = new ItemStackTemplate(mainItem.item(), mainItem.count(), mainItem.components());
                    Object object = ItemStackTemplateHelper.get(offItem, componentType);
                    if (object != null) {
                        ItemStackTemplateHelper.modify(outputStack, (template, modifier) -> {
                            modifier.set(componentType, object);
                        });
                    }
                    value = builderByCounter;
                    StrengthTableRecipe strengthTableRecipe = new StrengthTableRecipe(IngredientStack.of(mainItem), IngredientStack.of(offItem), IngredientStack.of(outputStack));
                    strengthTableRecipe.setVirtual(true);
                    this.dynamicBuilder.put(Identifier.parse(builderByCounter.toLowerCase()), strengthTableRecipe);
                } catch (Exception e) {
                    log.error("Can't register dynamic recipe, id: {} , {}", value, e);
                }
            }
        }
    }

    @Override
    public void bootstrap() {

    }

    @Override
    public List<StrengthTableRecipe> getMatches(List<IngredientStack> stackList) {
        if (stackList.size() < 2) {
            return List.of();
        }
        List<StrengthTableRecipe> recipe = new ArrayList<>();
        IngredientStack main = stackList.get(0);
        IngredientStack off = stackList.get(1);
        IngredientStack output = this.tryGetOutput(main, off);
        if (output != null) {
            IngredientStack mainClone = main.clone();
            IngredientStack offClone = off.clone();
            IngredientStack outputClone = output.clone();
            mainClone.build().setCount(1);
            offClone.build().setCount(1);
            recipe.add(new StrengthTableRecipe(mainClone, offClone, outputClone));
        }
        return recipe;
    }

    public IngredientStack tryGetOutput(IngredientStack main, IngredientStack off) {
        ItemStack mainStack = main.getLazyStack().copy();
        ItemStack offStack = off.getLazyStack().copy();
        Item mainItem = mainStack.getItem();
        Item offItem = offStack.getItem();
        boolean isDanmakuItem = mainItem instanceof DanmakuItem;
        boolean isSpellCardTemplate = offItem instanceof SpellCardTemplateItem;
        boolean isSpeedItem = offItem == RDItems.SPEED_FEATHER;
        boolean isSlime = offItem == Items.SLIME_BLOCK;
        boolean isIronSword = offItem == Items.IRON_SWORD;
        if (isDanmakuItem && isSpellCardTemplate) {
            DanmakuProperties component = mainStack.getOrDefault(RDDataComponents.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());
            DanmakuProperties properties = offStack.get(RDDataComponents.DANMAKU_PROPERTIES.value());
            if (properties != null) {
                mainStack.set(RDDataComponents.DANMAKU_PROPERTIES.value(), component.withTemplateId(properties.templateId()));
                return new IngredientStack(mainStack);
            }
        }
        if (isDanmakuItem && isSpeedItem) {
            DanmakuProperties component = mainStack.getOrDefault(RDDataComponents.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());
            float speed = component.speed();
            float sum = speed + 0.25f;
            if (sum <= MAX_SPEED) {
                mainStack.set(RDDataComponents.DANMAKU_PROPERTIES.value(), component.withSpeed(sum));
                return new IngredientStack(mainStack);
            }
        }
        if (isDanmakuItem && isSlime) {
            DanmakuProperties component = mainStack.getOrDefault(RDDataComponents.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());
            int count = component.count();
            int sum = count + 1;
            if (sum < MAX_COUNT) {
                mainStack.set(RDDataComponents.DANMAKU_PROPERTIES.value(), component.withCount(sum));
                return new IngredientStack(mainStack);
            }
        }
        if (isDanmakuItem && isIronSword) {
            DanmakuProperties component = mainStack.getOrDefault(RDDataComponents.DANMAKU_PROPERTIES.value(), DanmakuProperties.ofDefault());
            float sum = component.damage() + 0.25f;
            if (sum < MAX_DAMAGE) {
                mainStack.set(RDDataComponents.DANMAKU_PROPERTIES.value(), component.withDamage(sum));
                return new IngredientStack(mainStack);
            }
        }
        return null;
    }

    @Override
    public Boolean isMatch(IngredientStack input, IngredientStack recipe) {
        return false;
    }

    @Override
    public Codec<StrengthTableRecipe> getCodec() {
        return StrengthTableRecipe.CODEC;
    }

    @Override
    public String getTypeId() {
        return "strength_table";
    }

    @Override
    public Identifier getId() {
        return ReverieDreams.id(this.getTypeId());
    }
}
