package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.data.Customer;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.api.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

public interface RoleType extends RegistryEntryTranslatable {
    /**
     * Both role families use the same serialized identifier. Built-in roles keep lookup priority.
     */
    Codec<RoleType> CODEC = Codec.lazyInitialized(() -> RoleTypeCodec.byId(
            BuiltInRegistryProviders.NPC_ROLE_TYPE, BuiltInRegistryProviders.NPC_SIMPLE_ROLE_TYPE));
    StreamCodec<RegistryFriendlyByteBuf, RoleType> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    EntityDataSerializer<RoleType> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);

    boolean isVirtual();

    boolean isCustom();

    boolean isPresent();

    boolean isEmpty();

    /**
     * Optional entity registration for spawning this role. It may be a redirect entity.
     */
    @Nullable Holder<? extends EntityType<?>> get();

    Identifier identify();

    boolean isIdentify(RoleType other);

    default Customer getCustomer() {
        return Customer.UNDEFINED;
    }

    boolean isUnit();

    SkinType getSkinType();

}
