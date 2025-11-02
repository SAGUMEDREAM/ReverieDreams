package cc.thonly.reverie_dreams.recipe.type;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.component.DanmakuProperties;
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
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
    private final LinkedHashMap<ResourceLocation, StrengthTableRecipe> dynamicBuilder = new LinkedHashMap<>();
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
        Map<ResourceLocation, Resource> resources = manager.listResources((this.getTypeId() + "_recipe"), id -> {
            return id.getNamespace().equals(ReverieDreams.MOD_ID) && id.getPath().endsWith(".json");
        });
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            ResourceLocation registryKey = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replaceFirst("^strength_table_recipe/", "").replaceAll("\\.json$", ""));
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
        List<Item> danmakuItemView = RegistryManager.DANMAKU_TYPE
                .values().stream().map(DanmakuType::getItem).toList();
        List<ItemStack> danmakuItemStackView = danmakuItemView.stream().map(Item::getDefaultInstance).toList();
        List<ItemStack> templateStackView = SpellCardTemplates.getRegistryItemStackView().values().stream().map(ItemStack::copy).toList();

        this.registerAutomaticDynamic(danmakuItemStackView, templateStackView, ModDataComponentTypes.DANMAKU_PROPERTIES);
        this.registerAutomaticDynamic(danmakuItemStackView, List.of(ModItems.SPEED_FEATHER.getDefaultInstance()), ModDataComponentTypes.DANMAKU_PROPERTIES);
        this.registerAutomaticDynamic(danmakuItemStackView, List.of(Items.SLIME_BLOCK.getDefaultInstance()), ModDataComponentTypes.DANMAKU_PROPERTIES);
        this.registerAutomaticDynamic(danmakuItemStackView, List.of(Items.IRON_SWORD.getDefaultInstance()), ModDataComponentTypes.DANMAKU_PROPERTIES);

        Map<ResourceLocation, StrengthTableRecipe> sortedByKey = this.dynamicBuilder.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .collect(
                        LinkedHashMap::new,
                        (m, e) -> m.put(e.getKey(), e.getValue()),
                        Map::putAll
                );
        sortedByKey.forEach(this::add);
    }

    @SuppressWarnings("rawtypes")
    private void registerAutomaticDynamic(List<ItemStack> main, List<ItemStack> off, DataComponentType componentType) {
        ItemStack[] mainItems = main.toArray(new ItemStack[0]);
        ItemStack[] offItems = off.toArray(new ItemStack[0]);

        for (ItemStack mainItem : mainItems) {
            for (ItemStack offItem : offItems) {
                String value = null;
                try {
                    String mainItemIdStr = BuiltInRegistries.ITEM.getKey(mainItem.getItem()).getPath();
                    String offItemIdStr = BuiltInRegistries.ITEM.getKey(offItem.getItem()).getPath();
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
                    this.dynamicBuilder.put(ResourceLocation.parse(builderByCounter.toLowerCase()), strengthTableRecipe);
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
            DanmakuProperties component = mainStack.getOrDefault(ModDataComponentTypes.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault());
            DanmakuProperties properties = offStack.get(ModDataComponentTypes.DANMAKU_PROPERTIES);
            if (properties != null) {
                mainStack.set(ModDataComponentTypes.DANMAKU_PROPERTIES, component.withTemplateId(properties.getTemplateId()));
                return new ItemStackWrapper(mainStack);
            }
        }
        if (isDanmakuItem && isSpeedItem) {
            DanmakuProperties component = mainStack.getOrDefault(ModDataComponentTypes.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault());
            float speed = component.getSpeed();
            float sum = speed + 0.25f;
            if (sum <= MAX_SPEED) {
                mainStack.set(ModDataComponentTypes.DANMAKU_PROPERTIES, component.withSpeed(sum));
                return new ItemStackWrapper(mainStack);
            }
        }
        if (isDanmakuItem && isSlime) {
            DanmakuProperties component = mainStack.getOrDefault(ModDataComponentTypes.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault());
            int count = component.getCount();
            int sum = count + 1;
            if (sum < MAX_COUNT) {
                mainStack.set(ModDataComponentTypes.DANMAKU_PROPERTIES, component.withCount(sum));
                return new ItemStackWrapper(mainStack);
            }
        }
        if (isDanmakuItem && isIronSword) {
            DanmakuProperties component = mainStack.getOrDefault(ModDataComponentTypes.DANMAKU_PROPERTIES, DanmakuProperties.ofDefault());
            float sum = component.getDamage() + 0.25f;
            if (sum < MAX_DAMAGE) {
                mainStack.set(ModDataComponentTypes.DANMAKU_PROPERTIES, component.withDamage(sum));
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
    public ResourceLocation getId() {
        return ReverieDreams.id(this.getTypeId());
    }
}
