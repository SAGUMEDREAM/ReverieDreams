package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.Customer;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleRedirectEntity;
import cc.thonly.reverie_dreams.item.base.ColoredSpawnEggItem;
import cc.thonly.reverie_dreams.api.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.api.registry.SerializableProvider;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.NPCRoleTypes;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import cc.thonly.reverie_dreams.api.registry.RegistryProviderLookup;
import cc.thonly.reverie_dreams.util.lazy.LazySupplier;
import com.mojang.serialization.Codec;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

@SuppressWarnings("LombokGetterMayBeUsed")
@Getter
public class NPCRoleType implements SerializableProvider<NPCRoleType>,
        RegistryEntryOwnerBindable<NPCRoleType>,
        BuiltinObject,
        RoleType {
    private static final String REDIRECTING_PREFIX = "redirecting/";
    private static final LazySupplier<NPCRoleType> UNIT_ROLE = LazySupplier.of(NPCRoleType::new);
    public static final Codec<NPCRoleType> BY_REGISTRY_CODEC = Codec.lazyInitialized(() -> {
        return Identifier.CODEC.xmap(id -> {
                    NPCRoleType value = BuiltInRegistryProviders.NPC_ROLE_TYPE.getValue(id);
                    return Objects.requireNonNullElse(value, empty());
                },
                role -> {
                    Identifier key = BuiltInRegistryProviders.NPC_ROLE_TYPE.getKey(role);
                    if (key == null) {
                        key = NPCRoleTypes.REIMU.getId();
                    }
                    return key;
                }
        );
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, NPCRoleType> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(BY_REGISTRY_CODEC);
    public static final EntityDataSerializer<NPCRoleType> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);
    public static final List<Holder<EntityType<NPCSimpleRedirectEntity>>> ENTITIES = new ArrayList<>();
    public static final List<ItemDelegate> NPC_SPAWN_EGG_ITEM_LIST = new ArrayList<>();

    private Identifier id;
    private SkinType skinType;
    @Setter
    private Supplier<EntityType.Builder<NPCSimpleRedirectEntity>> builder;
    // 构建后属性
    private RegistrySupplier<EntityType<NPCSimpleRedirectEntity>> entityType;
    private ItemDelegate spawnEgg;
    private boolean initialized = false;

    private RegistryProviderLookup<NPCRoleType> owner;

    private boolean unit = false;

    private NPCRoleType() {
    }

    public NPCRoleType(Identifier id, SkinType skinType, Supplier<EntityType.Builder<NPCSimpleRedirectEntity>> builder) {
        this(id, skinType);
        this.builder = builder;
    }

    public NPCRoleType(Identifier id, SkinType skinType) {
        this.id = id;
        this.skinType = skinType;
    }

    @Override
    public boolean isVirtual() {
        return Objects.equals(this, empty());
    }

    @Override
    public boolean isCustom() {
        return this.builder != null;
    }

    @Override
    public boolean isPresent() {
        return this.entityType != null;
    }

    @Override
    public boolean isEmpty() {
        return this.entityType == null;
    }

    @Override
    public Holder<EntityType<NPCSimpleRedirectEntity>> get() {
        return this.entityType;
    }

    public ItemDelegate getEgg() {
        return this.spawnEgg;
    }

    @Override
    public String translateKey() {
        var type = this.entityType;
        return type == null ? ReverieDreams.id("base_character").toLanguageKey("entity") : type.value().getDescriptionId();
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
    public Customer getCustomer() {
        // Exact/alias lookup only: a registry-wide default is not this role's dedicated profile.
        if (this.identify() == null)
            return RoleType.super.getCustomer();
        return BuiltInRegistryProviders.CUSTOMER.get(this.identify()).map(Holder::value)
                .orElseGet(() -> RoleType.super.getCustomer());
    }

    @Override
    public SkinType getSkinType() {
        return this.skinType;
    }

    public NPCRoleType build() {
        if (this.initialized) {
            return this;
        }
        if (this.id == null) {
            throw new IllegalStateException("Cannot register the empty NPC role");
        }
        try {
            boolean custom = this.isCustom();
            String entityPath = (custom ? "" : REDIRECTING_PREFIX) + this.id.getPath();
            Supplier<EntityType.Builder<NPCSimpleRedirectEntity>> builderSupplier = custom ? this.builder : () -> EntityType.Builder.of(
                    (type, world) -> new NPCSimpleRedirectEntity(type, world, this),
                    MobCategory.MISC);
            RegistrySupplier<EntityType<NPCSimpleRedirectEntity>> registrySupplier = registerEntity(entityPath, builderSupplier);
            EntityAttributeRegistry.register(registrySupplier, BaseNPCLikeEntity::createLivingAttributes);
            String spawnEggId = entityPath + "_spawn_egg";
            ItemDelegate spawnEgg = registerNPCSpawnEggItem(spawnEggId, properties -> new ColoredSpawnEggItem(spawnEggId, registrySupplier.value(), properties));
            this.entityType = registrySupplier;
            this.spawnEgg = spawnEgg;
            this.initialized = true;
        } catch (RuntimeException e) {
            throw new IllegalStateException("Failed to register the entity and spawn egg for NPC role " + this.id, e);
        }
        return this;
    }

    @Override
    public Codec<NPCRoleType> getCodec() {
        return BY_REGISTRY_CODEC;
    }

    @Override
    public void setOwner(RegistryProviderLookup<NPCRoleType> owner) {
        this.owner = owner;
    }

    public boolean isUnit() {
        return this.unit;
    }

    public static NPCRoleType empty() {
        NPCRoleType type = UNIT_ROLE.get();
        type.unit = true;
        return type;
    }

    protected static RegistrySupplier<EntityType<NPCSimpleRedirectEntity>> registerEntity(
            String name,
            Supplier<EntityType.Builder<NPCSimpleRedirectEntity>> builderSupplier
    ) {
        RegistrySupplier<EntityType<NPCSimpleRedirectEntity>> entityTypeSupplier = MCBuiltInRegistries.ENTITY_TYPE.register(name, () -> builderSupplier.get().build(ResourceKey.create(Registries.ENTITY_TYPE, ReverieDreams.id(name))));
        ENTITIES.add(entityTypeSupplier);
        return entityTypeSupplier;
    }

    protected static ItemDelegate registerNPCSpawnEggItem(String name, Function<Item.Properties, Item> function) {
        RegistrySupplier<Item> itemSupplier = MCBuiltInRegistries.ITEM.register(name, () -> function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ReverieDreams.id(name)))));
        ItemDelegate itemDelegate = ItemDelegate.of(itemSupplier);
        NPC_SPAWN_EGG_ITEM_LIST.add(itemDelegate);
        return itemDelegate;
    }

    private static ResourceKey<EntityType<?>> of(Identifier id) {
        return ResourceKey.create(Registries.ENTITY_TYPE, id);
    }
}
