package cc.thonly.reverie_dreams.entity.npc;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.entity.skin.SkinType;
import cc.thonly.reverie_dreams.item.base.SpawnEggItem;
import cc.thonly.reverie_dreams.registry.*;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
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

import java.util.LinkedList;
import java.util.List;

@Slf4j
@Setter
@Getter
public class NPCRole implements CodecStep<NPCRole>, OwnerBinding<NPCRole>, BuiltinObject, Translatable {
    public static final Codec<NPCRole> CODEC = Codec.unit(NPCRole::new);
    public static final List<Item> NPC_SPAWN_EGG_ITEM_LIST = new LinkedList<>();

    private Identifier id;
    private SkinType skinType;
    // 构建后属性
    private EntityType<NPCRoleFastEntity> entityType;
    private Item spawnEgg;
//    private Class<? extends BaseNPCLikeEntity> clazz;
    private boolean hasBuilt = false;

    private IntrinsicalRegister<NPCRole> owner;;

    private NPCRole() {
    }

    public NPCRole(Identifier id, SkinType skinType) {
        this.id = id;
        this.skinType = skinType;
    }

    public boolean isPresent() {
        return this.entityType != null;
    }

    public boolean isEmpty() {
        return this.entityType == null;
    }

    public EntityType<NPCRoleFastEntity> get() {
        return this.entityType;
    }

    public Item getEgg() {
        return this.spawnEgg;
    }

    @Override
    public String translateKey() {
        return this.entityType.getTranslationKey();
    }

    public NPCRole build() {
        if (hasBuilt) {
            return this;
        }
        try {
            EntityType<NPCRoleFastEntity> build = EntityType.Builder.<NPCRoleFastEntity>create(
                            (type, world) -> new NPCRoleFastEntity(type, world, this.skinType),
                            SpawnGroup.MISC)
//                    .disableSummon()
                    .build(of(this.id));
            EntityType<NPCRoleFastEntity> entityType = registerEntity(this.id, build);;
            FabricDefaultAttributeRegistry.register(entityType, BaseNPCLikeEntity.createAttributes());
            Identifier spawnEggId = Identifier.of(this.id.getNamespace(), this.id.getPath() + "_spawn_egg");
            Item spawnEgg = registerNPCSpawnEggItem(new SpawnEggItem(spawnEggId, build, new Item.Settings().modelId(Touhou.id("spawn_egg"))));
            this.entityType = build;
            this.spawnEgg = spawnEgg;
            this.hasBuilt = true;
        } catch (Exception e) {
            log.error("Can't register role entity type {}", this.id.toString());
        }
        return this;
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
