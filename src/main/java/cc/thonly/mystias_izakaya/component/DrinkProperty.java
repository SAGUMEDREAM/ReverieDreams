package cc.thonly.mystias_izakaya.component;

import cc.thonly.mystias_izakaya.api.DrinkPropertyLoaderCallback;
import cc.thonly.mystias_izakaya.registry.MIRegistryManager;
import cc.thonly.reverie_dreams.effect.ModStatusEffects;
import cc.thonly.reverie_dreams.registry.RegistrableObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

@Setter
@Getter
@ToString
public class DrinkProperty implements RegistrableObject<DrinkProperty> {
    public static final Codec<DrinkProperty> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("registry_key").forGetter(DrinkProperty::getId),
            ITEMS_CODEC.fieldOf("properties").forGetter(DrinkProperty::getItemList)
    ).apply(instance, DrinkProperty::new));

    private Identifier id;
    private final StatusEffectInstance effectInstance;
    private Set<Item> items = new ObjectOpenHashSet<>();

    public DrinkProperty() {
        this(new StatusEffectInstance(new StatusEffectInstance(ModStatusEffects.EMPTY, 1)));
    }

    public DrinkProperty(StatusEffectInstance effectInstance) {
        this.effectInstance = effectInstance;
    }

    public DrinkProperty(Identifier id, List<Item> items) {
        this();
        this.id = id;
        this.items.addAll(items);
    }

    public final void use(ServerWorld world, LivingEntity user) {
        StatusEffectInstance effectInstance = new StatusEffectInstance(this.effectInstance);
        user.addStatusEffect(effectInstance);
        DrinkPropertyLoaderCallback.EVENT.invoker().onUse(world, user, this);
        this.onUse(world, user);
    }

    public void onUse(ServerWorld world, LivingEntity user) {

    }

    public Text getTooltip() {
        return Text.translatable(this.id.toTranslationKey("drink_property"));
    }

    public String translateKey() {
        return this.id.toTranslationKey("drink_property");
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
        Set<Map.Entry<Identifier, DrinkProperty>> entries = MIRegistryManager.DRINK_PROPERTY.entrySet();
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
            Identifier identifier = Identifier.of(id);
            DrinkProperty foodProperty = MIRegistryManager.DRINK_PROPERTY.get(identifier);
            if (foodProperty != null) {
                list.add(foodProperty);
            }
        }
        return list;
    }

    public static List<DrinkProperty> getFromItemStackComponent(ItemStack itemStack) {
        List<String> ids = itemStack.getOrDefault(MIDataComponentTypes.DRINK_PROPERTIES, new ArrayList<>());
        return getFromStrings(ids);
    }

    public static List<DrinkProperty> getFromItemStack(ItemStack itemStack) {
        List<DrinkProperty> list = new ArrayList<>();
        Item item = itemStack.getItem();
        Set<Map.Entry<Identifier, DrinkProperty>> entries = MIRegistryManager.DRINK_PROPERTY.entrySet();
        for (Map.Entry<Identifier, DrinkProperty> entry : entries) {
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

    @Override
    public Boolean isDirect() {
        return true;
    }
}
