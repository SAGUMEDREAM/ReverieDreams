package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.api.FoodPropertyLoaderCallback;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.component.RDDataComponents;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.impl.RegistryHandler;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

@Setter
@Getter
@ToString
public class FoodProperty implements CodecStep<FoodProperty>, OwnerBinding<FoodProperty>, BuiltinObject, Translatable {
    public static final Codec<FoodProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ResourceLocation.CODEC.fieldOf("registry_key").forGetter(FoodProperty::getId),
            ITEMS_CODEC.fieldOf("properties").forGetter(FoodProperty::getItemList)
    ).apply(instance, FoodProperty::new));

    private ResourceLocation id;
    private final MobEffectInstance effectInstance;
    private Set<Item> items = new ObjectOpenHashSet<>();

    private RegistryHandler<FoodProperty> owner;

    public FoodProperty() {
        this.effectInstance = new MobEffectInstance(new MobEffectInstance(RDStatusEffects.EMPTY, 1));
    }

    public FoodProperty(MobEffectInstance effectInstance) {
        this.effectInstance = effectInstance;
    }

    public FoodProperty(ResourceLocation id, List<Item> list) {
        this();
        this.id = id;
        this.items.addAll(list);
    }

    public final void use(ServerLevel world, LivingEntity user) {
        MobEffectInstance effectInstance = new MobEffectInstance(this.effectInstance);
        user.addEffect(effectInstance);
        FoodPropertyLoaderCallback.EVENT.invoker().onUse(world, user, this);
        this.onUse(world, user);
    }

    public void onUse(ServerLevel world, LivingEntity user) {

    }

    public static String getDisplayPrefix(ItemStack itemStack, FoodProperty foodProperty) {
        List<FoodProperty> all = FoodProperty.getIngredientProperties(itemStack.getItem());
        for (CraftingConflict conflict : RegistryHandlers.CRAFTING_CONFLICT.values()) {
            if (conflict.test(itemStack, foodProperty)) {
                return "§c-";
            }
        }
        return "§b+";
    }

    public Boolean is(FoodProperty property) {
        return this == property || this.getId().equals(property.getId()) || this.hashCode() == property.hashCode();
    }

    public Component getTooltip() {
        return Component.translatable(this.id.toLanguageKey("food_property"));
    }

    @Override
    public String translateKey() {
        return this.id.toLanguageKey("food_property");
    }

    public static List<FoodProperty> getAllProperties(ItemStack itemStack) {
        Set<FoodProperty> set = new HashSet<>();
        set.addAll(getIngredientProperties(itemStack.getItem()));
        set.addAll(getFromItemStackComponent(itemStack));
        set.addAll(getFromItemStack(itemStack));
        return new ArrayList<>(set);
    }

    /**
     * 根据给定的 Item 查找所有包含该 Item 的 FoodProperty。
     * 遍历整个 FOOD_PROPERTY 注册表，检查每个 FoodProperty 是否包含该 Item。
     *
     * @param item 目标物品
     * @return 包含该 Item 的所有 FoodProperty 列表
     */
    public static List<FoodProperty> getIngredientProperties(Item item) {
        Map<ResourceLocation, FoodProperty> map = RegistryHandlers.FOOD_PROPERTY.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().location(),
                        Map.Entry::getValue
                ));
        List<FoodProperty> list = new ArrayList<>();
        Set<Map.Entry<ResourceLocation, FoodProperty>> entries = map.entrySet();
        for (Map.Entry<ResourceLocation, FoodProperty> entry : entries) {
            FoodProperty foodProperty = entry.getValue();
            Set<Item> tags = foodProperty.getItems();
            if (tags.contains(item)) {
                list.add(foodProperty);
            }
        }
        return list;
    }

    /**
     * 从 ItemStack 的自定义组件（MIDataComponentTypes.FOOD_PROPERTIES）中获取 FoodProperty 列表。
     * 该组件存储的是 FoodProperty 的 id 字符串列表，通过这些字符串再查询对应的 FoodProperty 对象。
     *
     * @param itemStack 目标物品栈
     * @return 对应的 FoodProperty 列表
     */
    public static List<FoodProperty> getFromItemStackComponent(ItemStack itemStack) {
        List<String> ids = itemStack.getOrDefault(RDDataComponents.FOOD_PROPERTIES, new ArrayList<>());
        return getFromStrings(ids);
    }

    /**
     * 直接从 ItemStack 的物品（Item）获取其所有对应的 FoodProperty。
     * 实质上是调用 getIngredientProperties 来获取所有包含该物品的 FoodProperty。
     *
     * @param itemStack 目标物品栈
     * @return 包含该物品的所有 FoodProperty 列表
     */
    public static List<FoodProperty> getFromItemStack(ItemStack itemStack) {
        Map<ResourceLocation, FoodProperty> map = RegistryHandlers.FOOD_PROPERTY.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().location(),
                        Map.Entry::getValue
                ));
        List<FoodProperty> list = new ArrayList<>();
        Item item = itemStack.getItem();
        Set<Map.Entry<ResourceLocation, FoodProperty>> entries = map.entrySet();
        for (Map.Entry<ResourceLocation, FoodProperty> entry : entries) {
            FoodProperty foodProperty = entry.getValue();
            Set<Item> tags = foodProperty.getItems();
            if (tags.contains(item)) {
                list.add(foodProperty);
            }
        }
        return list;
    }

    /**
     * 根据一组 FoodProperty 的 id 字符串列表，查询对应的 FoodProperty 对象列表。
     *
     * @param ids FoodProperty 的 id 字符串列表
     * @return 对应的 FoodProperty 对象列表，若 id 无对应 FoodProperty 则忽略
     */
    public static List<FoodProperty> getFromStrings(List<String> ids) {
        List<FoodProperty> list = new ArrayList<>();
        for (String id : ids) {
            ResourceLocation identifier = ResourceLocation.parse(id);
            FoodProperty foodProperty = RegistryHandlers.FOOD_PROPERTY.getValue(identifier);
            if (foodProperty != null) {
                list.add(foodProperty);
            }
        }
        return list;
    }

    public List<Item> getItemList() {
        return new ArrayList<>(this.items);
    }

    @Override
    public Codec<FoodProperty> getCodec() {
        return CODEC;
    }

}
