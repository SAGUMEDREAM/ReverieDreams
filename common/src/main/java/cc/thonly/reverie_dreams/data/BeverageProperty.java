package cc.thonly.reverie_dreams.data;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.item.callback.BeveragePropertyItemUseCallback;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.effect.RDStatusEffects;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import lombok.EqualsAndHashCode;
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

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@EqualsAndHashCode
public class BeverageProperty implements SerializableProvider<BeverageProperty>, RegistryEntryOwnerBindable<BeverageProperty>, BuiltinObject, RegistryEntryTranslatable {
    public static final Identifier UNDEFINED = ReverieDreams.id("undefined");
    public static final Codec<BeverageProperty> COMPONENT_CODEC = Codec.lazyInitialized(() -> Identifier.CODEC.xmap(BuiltInRegistryProviders.BEVERAGE_PROPERTY::getValue, entry -> {
        Identifier key = BuiltInRegistryProviders.BEVERAGE_PROPERTY.getKey(entry);
        if (key == null) {
            return UNDEFINED;
        }
        return key;
    }));
    public static final StreamCodec<RegistryFriendlyByteBuf, BeverageProperty> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(COMPONENT_CODEC);
    public static final Codec<List<BeverageProperty>> BY_REGISTRY_LIST_CODEC = Codec.lazyInitialized(COMPONENT_CODEC::listOf);

    private Identifier id;
    private final MobEffectInstance effectInstance;
    private RegistryProvider<BeverageProperty> owner;

    public BeverageProperty() {
        this(new MobEffectInstance(new MobEffectInstance(RDStatusEffects.EMPTY.builtInHolder(), 1)));
    }

    public BeverageProperty(MobEffectInstance effectInstance) {
        this.effectInstance = effectInstance;
    }

    public final void use(ServerLevel world, LivingEntity user, ItemStack itemStack) {
        MobEffectInstance effectInstance = new MobEffectInstance(this.effectInstance);
        user.addEffect(effectInstance);
        List<MobEffectInstance> effectInstances = new ArrayList<>();
        List<MobEffectInstance> negativeEffectInstances = new ArrayList<>();
        BeveragePropertyItemUseCallback.EVENT.invoker().onUse(world, user, itemStack, this, effectInstances, negativeEffectInstances);
        effectInstances.forEach(user::addEffect);
        if (!user.hasEffect(RDStatusEffects.ANTI_ALCOHOL.builtInHolder())) {
            negativeEffectInstances.forEach(user::addEffect);
        }
        this.onUse(world, user);
    }

    public void onUse(ServerLevel world, LivingEntity user) {

    }

    public Boolean is(BeverageProperty property) {
        return this == property || this.getId().equals(property.getId()) || this.hashCode() == property.hashCode();
    }

    public Component getTooltip() {
        return Component.translatable(this.id.toLanguageKey("beverage_property"));
    }

    @Override
    public String toString() {
        return "BeverageProperty{" +
                "effectInstance=" + effectInstance +
                ", id=" + id +
                '}';
    }

    @Override
    public String translateKey() {
        return this.id.toLanguageKey("beverage_property");
    }

    @Override
    public Codec<BeverageProperty> getCodec() {
        return COMPONENT_CODEC;
    }

    public record Data(Identifier id, List<Item> items) {
        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.fieldOf("registry_key").forGetter(Data::id),
                ITEMS_CODEC.fieldOf("properties").forGetter(Data::items)
        ).apply(instance, Data::new));
    }
}
