package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.item.callback.FoodPropertyItemUseCallback;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

@Setter
@Getter
@ToString
public class FoodProperty implements CodecStep<FoodProperty>, RegistryEntryOwnerBindable<FoodProperty>, BuiltinObject, RegistryEntryTranslatable {
    public static final Identifier UNDEFINED = ReverieDreams.id("undefined");
    public static final Codec<FoodProperty> COMPONENT_CODEC = Codec.lazyInitialized(() -> Identifier.CODEC.xmap(RegistryImpls.FOOD_PROPERTY::getValue, entry -> {
        Identifier key = RegistryImpls.FOOD_PROPERTY.getKey(entry);
        if (key == null) {
            return UNDEFINED;
        }
        return key;
    }));
    public static final Codec<List<FoodProperty>> LIST_COMPONENT_CODEC = Codec.lazyInitialized(COMPONENT_CODEC::listOf);

    private Identifier id;
    private final MobEffectInstance effectInstance;

    private RegistryImpl<FoodProperty> owner;

    public FoodProperty() {
        this.effectInstance = new MobEffectInstance(new MobEffectInstance(RDStatusEffects.EMPTY, 1));
    }

    public FoodProperty(MobEffectInstance effectInstance) {
        this.effectInstance = effectInstance;
    }

    public final void use(ServerLevel world, LivingEntity user) {
        MobEffectInstance effectInstance = new MobEffectInstance(this.effectInstance);
        user.addEffect(effectInstance);
        FoodPropertyItemUseCallback.EVENT.invoker().onUse(world, user, this);
        this.onUse(world, user);
    }

    public void onUse(ServerLevel world, LivingEntity user) {

    }

    public static String getDisplayPrefix(ItemStack itemStack, FoodProperty foodProperty) {
        for (CraftingConflict conflict : RegistryImpls.CRAFTING_CONFLICT.values()) {
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
        return COMPONENT_CODEC;
    }

    public record Data(Identifier id, List<Item> items) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("registry_key").forGetter(Data::id),
                ITEMS_CODEC.fieldOf("properties").forGetter(Data::items)
        ).apply(instance, Data::new));

    }

}
