package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.data.Customer;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.util.CodecMerger;
import cc.thonly.reverie_dreams.util.LazyList;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public interface RoleType extends RegistryEntryTranslatable {
    List<Codec<RoleType>> SUPPORT_TYPE_CODECS = new LazyList<>(() -> {
        return new ArrayList(
                List.of(NPCRoleType.BY_REGISTRY_CODEC, NPCSimpleRoleType.BY_REGISTRY_CODEC)
        );
    });
    Codec<RoleType> CODEC = CodecMerger.mergeLazyInitialized(() -> SUPPORT_TYPE_CODECS, roleType -> !roleType.isUnit());
    StreamCodec<RegistryFriendlyByteBuf, RoleType> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(CODEC);
    EntityDataSerializer<RoleType> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);

    boolean isVirtual();

    boolean isCustom();

    boolean isPresent();

    boolean isEmpty();

    Holder<EntityType<? extends BaseNPCLikeEntity>> get();

    Identifier identify();

    boolean isIdentify(RoleType other);

    default Customer getCustomer() {
        return Customer.UNDEFINED;
    }

    boolean isUnit();

    SkinType getSkinType();

}
