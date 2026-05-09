package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.item.DrinkPropertyItemUseCallback;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.registry.interfaces.BuiltinObject;
import cc.thonly.reverie_dreams.registry.interfaces.CodecStep;
import cc.thonly.reverie_dreams.registry.interfaces.OwnerBinding;
import cc.thonly.reverie_dreams.registry.interfaces.Translatable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;

import java.util.List;

@Setter
@Getter
@ToString
@EqualsAndHashCode
public class DrinkProperty implements CodecStep<DrinkProperty>, OwnerBinding<DrinkProperty>, BuiltinObject, Translatable {
    public static final Identifier UNDEFINED = ReverieDreams.id("undefined");
    public static final Codec<DrinkProperty> COMPONENT_CODEC = Codec.lazyInitialized(() -> Identifier.CODEC.xmap(RegistryImpls.DRINK_PROPERTY::getValue, entry -> {
        Identifier key = RegistryImpls.DRINK_PROPERTY.getKey(entry);
        if (key == null) {
            return UNDEFINED;
        }
        return key;
    }));
    public static final Codec<List<DrinkProperty>> LIST_COMPONENT_CODEC = Codec.lazyInitialized(COMPONENT_CODEC::listOf);

    private Identifier id;
    private final MobEffectInstance effectInstance;
    private RegistryImpl<DrinkProperty> owner;

    public DrinkProperty() {
        this(new MobEffectInstance(new MobEffectInstance(RDStatusEffects.EMPTY, 1)));
    }

    public DrinkProperty(MobEffectInstance effectInstance) {
        this.effectInstance = effectInstance;
    }

    public final void use(ServerLevel world, LivingEntity user) {
        MobEffectInstance effectInstance = new MobEffectInstance(this.effectInstance);
        user.addEffect(effectInstance);
        DrinkPropertyItemUseCallback.EVENT.invoker().onUse(world, user, this);
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

    @Override
    public Codec<DrinkProperty> getCodec() {
        return COMPONENT_CODEC;
    }

    public record Data(Identifier id, List<Item> items) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("registry_key").forGetter(Data::id),
                ITEMS_CODEC.fieldOf("properties").forGetter(Data::items)
        ).apply(instance, Data::new));
    }
}
