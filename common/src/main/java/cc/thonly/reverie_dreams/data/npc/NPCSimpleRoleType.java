package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.api.registry.SerializableProvider;
import cc.thonly.reverie_dreams.util.lazy.LazySupplier;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

/**
 * A role definition without its own spawn entity.
 */
public class NPCSimpleRoleType implements SerializableProvider<NPCSimpleRoleType>, RoleType {
    public static final LazySupplier<NPCSimpleRoleType> UNIT_ROLE = LazySupplier.of(NPCSimpleRoleType::new);
    public static final Codec<NPCSimpleRoleType> BY_REGISTRY_CODEC = Codec.lazyInitialized(() -> {
        return Identifier.CODEC.xmap(id -> {
                    return BuiltInRegistryProviders.NPC_SIMPLE_ROLE_TYPE.findValue(id).orElseGet(NPCSimpleRoleType::empty);
                },
                role -> {
                    Identifier key = BuiltInRegistryProviders.NPC_SIMPLE_ROLE_TYPE.getKey(role);
                    if (key == null) {
                        key = ReverieDreams.id("undefined");
                    }
                    return key;
                }
        );
    });

    private final Identifier id;
    private final SkinType skinType;

    private NPCSimpleRoleType() {
        this.id = null;
        this.skinType = null;
    }

    public NPCSimpleRoleType(Identifier id, SkinType skinType) {
        this.id = Objects.requireNonNull(id);
        this.skinType = Objects.requireNonNull(skinType);
    }

    public static NPCSimpleRoleType empty() {
        return UNIT_ROLE.get();
    }

    @Override
    public boolean isVirtual() {
        return this.isUnit();
    }

    @Override
    public boolean isCustom() {
        return false;
    }

    @Override
    public boolean isPresent() {
        return !this.isUnit();
    }

    @Override
    public boolean isEmpty() {
        return this.isUnit();
    }

    @Override
    public @Nullable Holder<? extends EntityType<?>> get() {
        return null;
    }

    @Override
    public Identifier identify() {
        return this.id;
    }

    @Override
    public boolean isIdentify(RoleType other) {
        return this.id != null && other != null && this.id.equals(other.identify());
    }

    @Override
    public SkinType getSkinType() {
        return this.skinType;
    }

    @Override
    public Codec<NPCSimpleRoleType> getCodec() {
        return BY_REGISTRY_CODEC;
    }

    @Override
    public boolean isUnit() {
        return this.id == null;
    }

    @Override
    public String translateKey() {
        return this.id == null ? RoleType.super.translateKey() : this.id.toLanguageKey("npc_simple_role");
    }
}
