package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.SerializableProvider;
import cc.thonly.reverie_dreams.util.LazySupplier;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.Objects;

public class NPCSimpleRoleType implements SerializableProvider<NPCRoleType>, RoleType {
    public static final LazySupplier<NPCSimpleRoleType> UNIT_ROLE = LazySupplier.of(NPCSimpleRoleType::new);
    public static final Codec<NPCSimpleRoleType> BY_REGISTRY_CODEC = Codec.lazyInitialized(() -> {
        return Identifier.CODEC.xmap(id -> {
                    NPCSimpleRoleType value = BuiltInRegistryProviders.NPC_SIMPLE_ROLE_TYPE.getValue(id);
                    return Objects.requireNonNullElse(value, empty());
                },
                object -> {
                    if (!(object instanceof NPCSimpleRoleType role)) {
                        return ReverieDreams.id("undefined");
                    }
                    Identifier key = BuiltInRegistryProviders.NPC_SIMPLE_ROLE_TYPE.getKey(role);
                    if (key == null) {
                        key = ReverieDreams.id("undefined");
                    }
                    return key;
                }
        );
    });

    private Identifier id;
    private SkinType skinType;
    private boolean unit = false;

    private NPCSimpleRoleType() {

    }

    public NPCSimpleRoleType(Identifier id, SkinType skinType) {
        this.id = id;
        this.skinType = skinType;
    }

    public static NPCSimpleRoleType empty() {
        NPCSimpleRoleType type = UNIT_ROLE.get();
        type.unit = true;
        return type;
    }

    @Override
    public boolean isVirtual() {
        return false;
    }

    @Override
    public boolean isCustom() {
        return false;
    }

    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public Holder<EntityType<? extends BaseNPCLikeEntity>> get() {
        return null;
    }

    @Override
    public Identifier identify() {
        return null;
    }

    @Override
    public boolean isIdentify(RoleType other) {
        return false;
    }

    @Override
    public SkinType getSkinType() {
        return null;
    }

    @Override
    public Codec<NPCRoleType> getCodec() {
        return null;
    }

    @Override
    public boolean isUnit() {
        return this.unit;
    }
}
