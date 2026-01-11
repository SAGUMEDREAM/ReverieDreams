package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.api.DrinkPropertyLoaderCallback;
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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

@Setter
@Getter
@ToString
public class DrinkProperty implements CodecStep<DrinkProperty>, OwnerBinding<DrinkProperty>, BuiltinObject, Translatable {
    public static final Codec<DrinkProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("registry_key").forGetter(DrinkProperty::getId),
            ITEMS_CODEC.fieldOf("properties").forGetter(DrinkProperty::getItemList)
    ).apply(instance, DrinkProperty::new));

    private Identifier id;
    private final MobEffectInstance effectInstance;
    private Set<Item> items = new ObjectOpenHashSet<>();
    private RegistryHandler<DrinkProperty> owner;

    public DrinkProperty() {
        this(new MobEffectInstance(new MobEffectInstance(RDStatusEffects.EMPTY, 1)));
    }

    public DrinkProperty(MobEffectInstance effectInstance) {
        this.effectInstance = effectInstance;
    }

    public DrinkProperty(Identifier id, List<Item> items) {
        this();
        this.id = id;
        this.items.addAll(items);
    }

    public final void use(ServerLevel world, LivingEntity user) {
        MobEffectInstance effectInstance = new MobEffectInstance(this.effectInstance);
        user.addEffect(effectInstance);
        DrinkPropertyLoaderCallback.EVENT.invoker().onUse(world, user, this);
        this.onUse(world, user);
    }

    public void onUse(ServerLevel world, LivingEntity user) {

    }

    public Boolean is(DrinkProperty property) {
        return this == property || this.getId().equals(property.getId()) || this.hashCode() == property.hashCode();
    }

    public Component getTooltip() {
        return Component.translatable(this.id.toLanguageKey("drink_property"));
    }

    @Override
    public String translateKey() {
        return this.id.toLanguageKey("drink_property");
    }

    public List<Item> getItemList() {
        return new ArrayList<>(this.items);
    }

    public static List<DrinkProperty> getAllProperties(ItemStack itemStack) {
        Set<DrinkProperty> set = new HashSet<>();
        set.addAll(getDrinkProperties(itemStack.getItem()));
        set.addAll(getFromItemStackComponent(itemStack));
        set.addAll(getFromItemStack(itemStack));
        return new ArrayList<>(set);
    }

    public static List<DrinkProperty> getDrinkProperties(Item item) {
        List<DrinkProperty> list = new ArrayList<>();
        Set<Map.Entry<Identifier, DrinkProperty>> entries = RegistryHandlers.DRINK_PROPERTY.idEntrySet();
        for (Map.Entry<Identifier, DrinkProperty> entry : entries) {
            DrinkProperty foodProperty = entry.getValue();
            Set<Item> tags = foodProperty.getItems();
            if (tags.contains(item)) {
                list.add(foodProperty);
            }
        }
        return list;
    }

    public static List<DrinkProperty> getFromStrings(List<String> ids) {
        List<DrinkProperty> list = new ArrayList<>();
        for (String id : ids) {
            Identifier identifier = Identifier.parse(id);
            DrinkProperty foodProperty = RegistryHandlers.DRINK_PROPERTY.getValue(identifier);
            if (foodProperty != null) {
                list.add(foodProperty);
            }
        }
        return list;
    }

    public static List<DrinkProperty> getFromItemStackComponent(ItemStack itemStack) {
        List<String> ids = itemStack.getOrDefault(RDDataComponents.DRINK_PROPERTIES, new ArrayList<>());
        return getFromStrings(ids);
    }

    public static List<DrinkProperty> getFromItemStack(ItemStack itemStack) {
        List<DrinkProperty> list = new ArrayList<>();
        Item item = itemStack.getItem();
        Set<Map.Entry<ResourceKey<DrinkProperty>, DrinkProperty>> entries = RegistryHandlers.DRINK_PROPERTY.entrySet();
        for (Map.Entry<ResourceKey<DrinkProperty>, DrinkProperty> entry : entries) {
            DrinkProperty drinkProperty = entry.getValue();
            Set<Item> tags = drinkProperty.getItems();
            if (tags.contains(item)) {
                list.add(drinkProperty);
            }
        }
        return list;
    }

    @Override
    public Codec<DrinkProperty> getCodec() {
        return CODEC;
    }

}
