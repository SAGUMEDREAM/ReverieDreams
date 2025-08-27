package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.entity.base.NPCEntity;
import cc.thonly.reverie_dreams.entity.skin.RoleSkin;
import cc.thonly.reverie_dreams.item.base.BasicPolymerSpawnEggItem;
import cc.thonly.reverie_dreams.registry.RegistrableObject;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import cc.thonly.reverie_dreams.registry.StandaloneRegistry;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import com.mojang.authlib.properties.Property;
import com.mojang.serialization.Codec;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Setter
@Getter
public class NPCRole implements RegistrableObject<NPCRole> {
    public static final Codec<NPCRole> CODEC = Codec.unit(NPCRole::new);
    public static final List<Item> NPC_SPAWN_EGG_ITEM_LIST = new LinkedList<>();

    private Identifier id;
    private Property property;
    // 构建后属性
    private EntityType<NPCEntity> entityType;
    private Item spawnEgg;
    private Class<? extends NPCEntityImpl> clazz;
    private boolean hasBuilt = false;

    private NPCRole() {
    }

    public NPCRole(Identifier id, Property property) {
        this(id, property, NPCRoleEntityImpl.class);
    }

    public NPCRole(Identifier id, RoleSkin skin) {
        this(id, skin.get(), NPCRoleEntityImpl.class);
    }

    public NPCRole(Identifier id, Property property, Class<NPCRoleEntityImpl> clazz) {
        this.id = id;
        this.property = property;
        this.clazz = clazz;
    }

    public boolean isPresent() {
        return this.entityType != null;
    }

    public boolean isEmpty() {
        return this.entityType == null;
    }

    public EntityType<NPCEntity> get() {
        return this.entityType;
    }

    public Item getEgg() {
        return this.spawnEgg;
    }

    public NPCRole build() {
        if (hasBuilt) {
            return this;
        }
        try {
            EntityType<NPCEntity> entityType = registerEntity(this.id,
                    EntityType.Builder.<NPCEntity>create(
                                    (type, world) -> {
                                        try {
                                            return this.clazz.getConstructor(EntityType.class, World.class, Property.class)
                                                    .newInstance(type, world, property);
                                        } catch (Exception e) {
                                            log.error("Failed to instantiate NPCEntityImpl for type {}, {}", id, e);
                                            return null;
                                        }
                                    },
                                    SpawnGroup.MISC)
                            .build(of(this.id)));
            FabricDefaultAttributeRegistry.register(entityType, NPCEntityImpl.createAttributes());
            Identifier spawnEggId = Identifier.of(this.id.getNamespace(), this.id.getPath() + "_spawn_egg");
            Item spawnEgg = registerNPCSpawnEggItem(new BasicPolymerSpawnEggItem(spawnEggId, entityType, new Item.Settings().modelId(Touhou.id("spawn_egg"))));
            this.entityType = entityType;
            this.spawnEgg = spawnEgg;
            this.hasBuilt = true;
        } catch (Exception e) {
            log.error("Can't register role entity type {}", this.id.toString());
        }
        return this;
    }

    @Override
    public StandaloneRegistry<NPCRole> getRegistryRef() {
        return RegistryManager.NPC_ROLE;
    }

    @Override
    public Codec<NPCRole> getCodec() {
        return CODEC;
    }

    protected static <T extends Entity> EntityType<T> registerEntity(Identifier id, EntityType<T> entityType) {
        EntityType<T> entityTypeRef = Registry.register(Registries.ENTITY_TYPE, id, entityType);
        PolymerEntityUtils.registerType(entityTypeRef);
        return entityTypeRef;
    }

    protected static Item registerNPCSpawnEggItem(IdentifierGetter item) {
        Registry.register(Registries.ITEM, item.getIdentifier(), (Item) item);
        NPC_SPAWN_EGG_ITEM_LIST.add((Item) item);
        return (Item) item;
    }

    private static RegistryKey<EntityType<?>> of(Identifier id) {
        return RegistryKey.of(RegistryKeys.ENTITY_TYPE, id);
    }
}
