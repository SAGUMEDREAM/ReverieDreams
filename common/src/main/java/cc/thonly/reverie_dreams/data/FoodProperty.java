package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.item.callback.ItemEvent;
import cc.thonly.reverie_dreams.api.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.api.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.api.registry.SerializableProvider;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.api.registry.RegistryProviderLookup;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@Setter
@Getter
public class FoodProperty implements SerializableProvider<FoodProperty>, RegistryEntryOwnerBindable<FoodProperty>, BuiltinObject, RegistryEntryTranslatable {
    public static final Identifier UNDEFINED = ReverieDreams.id("undefined");
    public static final Codec<FoodProperty> BY_REGISTRY_CODEC = Codec.lazyInitialized(() -> Identifier.CODEC.xmap(BuiltInRegistryProviders.FOOD_PROPERTY::getValue, entry -> {
        Identifier key = BuiltInRegistryProviders.FOOD_PROPERTY.getKey(entry);
        if (key == null) {
            return UNDEFINED;
        }
        return key;
    }));
    public static final StreamCodec<RegistryFriendlyByteBuf, FoodProperty> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(BY_REGISTRY_CODEC);
    public static final Codec<List<FoodProperty>> BY_REGISTRY_LIST_CODEC = Codec.lazyInitialized(BY_REGISTRY_CODEC::listOf);

    private Identifier id;
    private final Supplier<MobEffectInstance> effectInstanceFactory;

    private RegistryProviderLookup<FoodProperty> owner;

    public FoodProperty() {
        this.effectInstanceFactory = () -> new MobEffectInstance(new MobEffectInstance(RDStatusEffects.EMPTY.builtInHolder(), 1));
    }

    public FoodProperty(MobEffectInstance mobEffectInstance) {
        this.effectInstanceFactory = () -> new MobEffectInstance(mobEffectInstance);
    }

    public FoodProperty(Supplier<MobEffectInstance> effectInstanceFactory) {
        this.effectInstanceFactory = effectInstanceFactory;
    }

    public final void use(ServerLevel world, LivingEntity user, ItemStack itemStack) {
        MobEffectInstance effectInstance = this.effectInstanceFactory.get();
        user.addEffect(effectInstance);
        ItemEvent.FOOD_PROPERTY_USE.invoker().onUse(world, user, itemStack, this);
        this.onUse(world, user);
    }

    public void onUse(ServerLevel world, LivingEntity user) {

    }

    public static String getDisplayPrefix(ItemStack itemStack, FoodProperty foodProperty) {
        for (CraftingConflict conflict : BuiltInRegistryProviders.CRAFTING_CONFLICT.values()) {
            if (conflict.test(itemStack, foodProperty)) {
                return "§c-";
            }
        }
        return "§b+";
    }

    public Boolean is(FoodProperty property) {
        return this == property || this.getId().equals(property.getId()) || this.hashCode() == property.hashCode();
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FoodProperty property)) {
            return false;
        }
        return this.is(property);
    }

    @Override
    public String toString() {
        return "FoodProperty{" +
                "id=" + id +
                ", effectInstance=" + effectInstanceFactory +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    public Component getTooltip() {
        return Component.translatable(this.id.toLanguageKey("food_property"));
    }

    @Override
    public String translateKey() {
        return this.id.toLanguageKey("food_property");
    }

    @Override
    public Codec<FoodProperty> getCodec() {
        return BY_REGISTRY_CODEC;
    }

    public record Data(Identifier id, List<Item> items, boolean replace) {
        public Data(Identifier id, List<Item> items) {
            this(id, items, false);
        }

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("registry_key").forGetter(Data::id),
                ITEMS_CODEC.fieldOf("properties").forGetter(Data::items),
                Codec.BOOL.optionalFieldOf("replace", false).forGetter(Data::replace)
        ).apply(instance, Data::new));

    }

}
