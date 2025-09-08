package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.component.ModDataComponentTypes;
import cc.thonly.reverie_dreams.danmaku.DanmakuType;
import cc.thonly.reverie_dreams.danmaku.SpellCardTemplates;
import cc.thonly.reverie_dreams.item.ModItems;
import cc.thonly.reverie_dreams.item.danmaku.DanmakuItem;
import cc.thonly.reverie_dreams.item.template.SpellCardTemplateItem;
import cc.thonly.reverie_dreams.recipe.BaseRecipeType;
import cc.thonly.reverie_dreams.recipe.ItemStackWrapper;
import cc.thonly.reverie_dreams.recipe.entry.StrengthTableRecipe;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.component.ComponentType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
        Map<Identifier, Resource> resources = manager.findResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(Touhou.MOD_ID) && id.getPath().endsWith(".json");
        });
        for (Map.Entry<Identifier, Resource> entry : resources.entrySet()) {
            Identifier id = entry.getKey();
            Identifier registryKey = Identifier.of(id.getNamespace(), id.getPath().replaceFirst("^strength_table_recipe/", "").replaceAll("\\.json$", ""));
            Resource resource = entry.getValue();
            try (InputStream stream = resource.getInputStream()) {
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
        List<Item> danmakuItemView = RegistryManager.DANMAKU_TYPE
                .values().stream().map(DanmakuType::getItem).toList();
        List<ItemStack> danmakuItemStackView = danmakuItemView.stream().map(Item::getDefaultStack).toList();
        List<ItemStack> templateStackView = SpellCardTemplates.getRegistryItemStackView().values().stream().map(ItemStack::copy).toList();

        this.registerAutomaticDynamic(danmakuItemStackView, templateStackView, ModDataComponentTypes.Danmaku.TEMPLATE);
        this.registerAutomaticDynamic(danmakuItemStackView, List.of(ModItems.SPEED_FEATHER.getDefaultStack()), ModDataComponentTypes.Danmaku.SPEED);
        this.registerAutomaticDynamic(danmakuItemStackView, List.of(Items.SLIME_BLOCK.getDefaultStack()), ModDataComponentTypes.Danmaku.COUNT);
        this.registerAutomaticDynamic(danmakuItemStackView, List.of(Items.IRON_SWORD.getDefaultStack()), ModDataComponentTypes.Danmaku.DAMAGE);

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
    private void registerAutomaticDynamic(List<ItemStack> main, List<ItemStack> off, ComponentType componentType) {
        ItemStack[] mainItems = main.toArray(new ItemStack[0]);
        ItemStack[] offItems = off.toArray(new ItemStack[0]);

        for (ItemStack mainItem : mainItems) {
            for (ItemStack offItem : offItems) {
                String value = null;
                try {
                    String mainItemIdStr = Registries.ITEM.getId(mainItem.getItem()).getPath();
                    String offItemIdStr = Registries.ITEM.getId(offItem.getItem()).getPath();
                    String builder = mainItemIdStr + offItemIdStr;
                    Integer num = this.automaticRecipeIdCounter.computeIfAbsent(builder, (x) -> 0);
                    String builderByCounter = builder + "_" + num;
                    this.automaticRecipeIdCounter.put(builder, ++num);
                    ItemStack outputStack = mainItem.copy();
                    Object object = offItem.get(componentType);
                    if (object != null) {
                        outputStack.set(componentType, object);
                    }
                    value = builderByCounter;
                    StrengthTableRecipe strengthTableRecipe = new StrengthTableRecipe(ItemStackWrapper.of(mainItem), ItemStackWrapper.of(offItem), ItemStackWrapper.of(outputStack));
                    strengthTableRecipe.setVirtual(true);
                    this.dynamicBuilder.put(Identifier.of(builderByCounter.toLowerCase()), strengthTableRecipe);
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
    public List<StrengthTableRecipe> getMatches(List<ItemStackWrapper> wrappers) {
        if (wrappers.size() < 2) {
            return List.of();
        }
        List<StrengthTableRecipe> recipe = new ArrayList<>();
        ItemStackWrapper main = wrappers.get(0);
        ItemStackWrapper off = wrappers.get(1);
        ItemStackWrapper output = this.tryGetOutput(main, off);
        if (output != null) {
            ItemStackWrapper mainClone = main.clone();
            ItemStackWrapper offClone = off.clone();
            ItemStackWrapper outputClone = output.clone();
            mainClone.getItemStack().setCount(1);
            offClone.getItemStack().setCount(1);
            recipe.add(new StrengthTableRecipe(mainClone, offClone, outputClone));
        }
        return recipe;
    }

    public ItemStackWrapper tryGetOutput(ItemStackWrapper main, ItemStackWrapper off) {
        ItemStack mainStack = main.getItemStack().copy();
        ItemStack offStack = off.getItemStack().copy();
        Item mainItem = mainStack.getItem();
        Item offItem = offStack.getItem();
        boolean isDanmakuItem = mainItem instanceof DanmakuItem;
        boolean isSpellCardTemplate = offItem instanceof SpellCardTemplateItem;
        boolean isSpeedItem = offItem == ModItems.SPEED_FEATHER;
        boolean isSlime = offItem == Items.SLIME_BLOCK;
        boolean isIronSword = offItem == Items.IRON_SWORD;
        if (isDanmakuItem && isSpellCardTemplate) {
            String templateId = offStack.get(ModDataComponentTypes.Danmaku.TEMPLATE);
            if (templateId != null) {
                mainStack.set(ModDataComponentTypes.Danmaku.TEMPLATE, templateId);
                return new ItemStackWrapper(mainStack);
            }
        }
        if (isDanmakuItem && isSpeedItem) {
            float speed = mainStack.getOrDefault(ModDataComponentTypes.Danmaku.SPEED, 1.0f);
            float sum = speed + 0.25f;
            if (sum <= MAX_SPEED) {
                mainStack.set(ModDataComponentTypes.Danmaku.SPEED, sum);
                return new ItemStackWrapper(mainStack);
            }
        }
        if (isDanmakuItem && isSlime) {
            int count = mainStack.getOrDefault(ModDataComponentTypes.Danmaku.COUNT, 1);
            int sum = count + 1;
            if (sum < MAX_COUNT) {
                mainStack.set(ModDataComponentTypes.Danmaku.COUNT, sum);
                return new ItemStackWrapper(mainStack);
            }
        }
        if (isDanmakuItem && isIronSword) {
            float damage = mainStack.getOrDefault(ModDataComponentTypes.Danmaku.DAMAGE, 2f);
            float sum = damage + 0.25f;
            if (sum < MAX_DAMAGE) {
                mainStack.set(ModDataComponentTypes.Danmaku.DAMAGE, sum);
                return new ItemStackWrapper(mainStack);
            }
        }
        return null;
    }

    @Override
    public Boolean isMatch(ItemStackWrapper input, ItemStackWrapper recipe) {
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
        return Touhou.id(this.getTypeId());
    }
}
