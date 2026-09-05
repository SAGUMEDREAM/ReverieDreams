package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.registry.EntityAttributeRegistry;
import cc.thonly.reverie_dreams.data.Customer;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleRedirectEntity;
import cc.thonly.reverie_dreams.item.base.ColoredSpawnEggItem;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.NPCRoleTypes;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import cc.thonly.reverie_dreams.registry.delegate.RegistryDelegate;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import cc.thonly.reverie_dreams.util.LazySupplier;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Getter
public class NPCRoleType implements SerializableProvider<NPCRoleType>,
        RegistryEntryOwnerBindable<NPCRoleType>,
        BuiltinObject,
        RoleType {
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
    private RegistryDelegate<EntityType<NPCSimpleRedirectEntity>> entityType;
    private ItemDelegate spawnEgg;
    private boolean initialized = false;

    private RegistryProvider<NPCRoleType> owner;

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

    @SuppressWarnings({"unchecked"})
    @Override
    public Holder<EntityType<? extends BaseNPCLikeEntity>> get() {
        return (Holder<EntityType<? extends BaseNPCLikeEntity>>) (Object) this.entityType;
    }

    public ItemDelegate getEgg() {
        return this.spawnEgg;
    }

    @Override
    public String translateKey() {
        return this.entityType.value().getDescriptionId();
    }

    @Override
    public Identifier identify() {
        return this.id;
    }

    @Override
    public boolean isIdentify(RoleType other) {
        return Objects.equals(other.identify(), this.id);
    }

    @Override
    public Customer getCustomer() {
        Customer customer = BuiltInRegistryProviders.CUSTOMER.getValue(this.identify());
//        System.out.println(customer);
        return customer == null ? RoleType.super.getCustomer() : customer;
    }

    @Override
    public SkinType getSkinType() {
        return this.skinType;
    }

    public NPCRoleType build() {
        if (this.initialized) {
            return this;
        }
        try {
            Supplier<EntityType.Builder<NPCSimpleRedirectEntity>> builderSupplier = this.isCustom() ? this.builder : () -> EntityType.Builder.of(
                    (type, world) -> new NPCSimpleRedirectEntity(type, world, this),
                    MobCategory.MISC);
            RegistryDelegate<EntityType<NPCSimpleRedirectEntity>> registrySupplier = registerEntity(this.id.getPath(), builderSupplier);
            EntityAttributeRegistry.register(registrySupplier, BaseNPCLikeEntity::createLivingAttributes);
            String spawnEggId = this.id.getPath() + "_spawn_egg";
            ItemDelegate spawnEgg = registerNPCSpawnEggItem(spawnEggId, (id) -> new ColoredSpawnEggItem(spawnEggId, registrySupplier.value(), new Item.Properties()));
            this.entityType = registrySupplier;
            this.spawnEgg = spawnEgg;
            this.initialized = true;
        } catch (Exception e) {
            log.error("Can't register role entity type {}", this.id.toString(), e);
        }
        return this;
    }

    @Override
    public Codec<NPCRoleType> getCodec() {
        return BY_REGISTRY_CODEC;
    }

    @Override
    public void setOwner(RegistryProvider<NPCRoleType> owner) {
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

    protected static RegistryDelegate<EntityType<NPCSimpleRedirectEntity>> registerEntity(
            String name,
            Supplier<EntityType.Builder<NPCSimpleRedirectEntity>> builderSupplier
    ) {
        RegistryDelegate<EntityType<NPCSimpleRedirectEntity>> entityTypeSupplier = MCBuiltInRegistries.ENTITY_TYPE.register(name, () -> builderSupplier.get().build(ResourceKey.create(Registries.ENTITY_TYPE, ReverieDreams.id(name))));
        ENTITIES.add(entityTypeSupplier);
        return entityTypeSupplier;
    }

    protected static ItemDelegate registerNPCSpawnEggItem(String name, Function<Item.Properties, Item> function) {
        RegistryDelegate<Item> itemSupplier = MCBuiltInRegistries.ITEM.register(name, () -> function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ReverieDreams.id(name)))));
        ItemDelegate itemDelegate = ItemDelegate.of(itemSupplier);
        NPC_SPAWN_EGG_ITEM_LIST.add(itemDelegate);
        return itemDelegate;
    }

    private static ResourceKey<EntityType<?>> of(Identifier id) {
        return ResourceKey.create(Registries.ENTITY_TYPE, id);
    }
}
