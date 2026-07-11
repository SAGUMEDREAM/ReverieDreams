package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleFastEntity;
import cc.thonly.reverie_dreams.item.base.ColoredSpawnEggItem;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.registry.content.NPCRoles;
import cc.thonly.reverie_dreams.registry.impl.ItemDelegate;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.util.LazySupplier;
import com.mojang.serialization.Codec;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
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

@Slf4j
@Getter
public class NPCRole implements CodecStep<NPCRole>, RegistryEntryOwnerBindable<NPCRole>, BuiltinObject, RegistryEntryTranslatable {
    private static final LazySupplier<NPCRole> UNIT_ROLE = LazySupplier.of(NPCRole::new);
    public static final Codec<NPCRole> BY_REGISTRY = Codec.lazyInitialized(() -> {
        return Identifier.CODEC.xmap(id -> {
                    NPCRole value = RegistryImpls.NPC_ROLE.getValue(id);
                    return Objects.requireNonNullElse(value, empty());
                },
                role -> {
                    Identifier key = RegistryImpls.NPC_ROLE.getKey(role);
                    if (key == null) {
                        key = NPCRoles.REIMU.getId();
                    }
                    return key;
                }
        );
    });
    public static final StreamCodec<RegistryFriendlyByteBuf, NPCRole> TRUSTED_STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistriesTrusted(BY_REGISTRY);
    public static final EntityDataSerializer<NPCRole> SERIALIZER = EntityDataSerializer.forValueType(TRUSTED_STREAM_CODEC);
    public static final List<Holder<EntityType<NPCRoleFastEntity>>> ENTITIES = new ArrayList<>();
    public static final List<ItemDelegate> NPC_SPAWN_EGG_ITEM_LIST = new ArrayList<>();

    private Identifier id;
    private SkinType skinType;
    @Setter
    private Supplier<EntityType.Builder<NPCRoleFastEntity>> builder;
    // 构建后属性
    private RegistrySupplier<EntityType<NPCRoleFastEntity>> entityType;
    private ItemDelegate spawnEgg;
    private boolean hasBuilt = false;

    private RegistryImpl<NPCRole> owner;

    private NPCRole() {
    }

    public NPCRole(Identifier id, SkinType skinType, Supplier<EntityType.Builder<NPCRoleFastEntity>> builder) {
        this(id, skinType);
        this.builder = builder;
    }

    public NPCRole(Identifier id, SkinType skinType) {
        this.id = id;
        this.skinType = skinType;
    }

    public boolean isVirtual() {
        return Objects.equals(this, empty());
    }

    public boolean isCustom() {
        return this.builder != null;
    }

    public boolean isPresent() {
        return this.entityType != null;
    }

    public boolean isEmpty() {
        return this.entityType == null;
    }

    public Holder<EntityType<NPCRoleFastEntity>> get() {
        return this.entityType;
    }

    public ItemDelegate getEgg() {
        return this.spawnEgg;
    }

    @Override
    public String translateKey() {
        return this.entityType.value().getDescriptionId();
    }

    public NPCRole build() {
        if (hasBuilt) {
            return this;
        }
        try {
            Supplier<EntityType.Builder<NPCRoleFastEntity>> builderSupplier = this.isCustom() ? this.builder : () -> EntityType.Builder.of(
                    (type, world) -> new NPCRoleFastEntity(type, world, this.skinType),
                    MobCategory.MISC);
            RegistrySupplier<EntityType<NPCRoleFastEntity>> registrySupplier = registerEntity(this.id.getPath(), builderSupplier);
            EntityAttributeRegistry.register(registrySupplier, BaseNPCLikeEntity::createLivingAttributes);
            String spawnEggId = this.id.getPath() + "_spawn_egg";
            ItemDelegate spawnEgg = registerNPCSpawnEggItem(spawnEggId, (id) -> new ColoredSpawnEggItem(spawnEggId, registrySupplier.value(), new Item.Properties()));
            this.entityType = registrySupplier;
            this.spawnEgg = spawnEgg;
            this.hasBuilt = true;
        } catch (Exception e) {
            log.error("Can't register role entity type {}", this.id.toString());
        }
        return this;
    }

    @Override
    public Codec<NPCRole> getCodec() {
        return BY_REGISTRY;
    }

    @Override
    public void setOwner(RegistryImpl<NPCRole> owner) {
        this.owner = owner;
    }

    public static NPCRole empty() {
        return UNIT_ROLE.get();
    }

    protected static RegistrySupplier<EntityType<NPCRoleFastEntity>> registerEntity(
            String name,
            Supplier<EntityType.Builder<NPCRoleFastEntity>> builderSupplier
    ) {
        RegistrySupplier<EntityType<NPCRoleFastEntity>> entityTypeSupplier = ReverieDreamsRegistries.ENTITY_TYPE.register(name, () -> builderSupplier.get().build(ResourceKey.create(Registries.ENTITY_TYPE, ReverieDreams.id(name))));
        ENTITIES.add(entityTypeSupplier);
        return entityTypeSupplier;
    }

    protected static ItemDelegate registerNPCSpawnEggItem(String name, Function<Item.Properties, Item> function) {
        RegistrySupplier<Item> itemSupplier = ReverieDreamsRegistries.ITEM.register(name, () -> function.apply(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, ReverieDreams.id(name)))));
        ItemDelegate itemDelegate = ItemDelegate.of(itemSupplier);
        NPC_SPAWN_EGG_ITEM_LIST.add(itemDelegate);
        return itemDelegate;
    }

    private static ResourceKey<EntityType<?>> of(Identifier id) {
        return ResourceKey.create(Registries.ENTITY_TYPE, id);
    }
}
