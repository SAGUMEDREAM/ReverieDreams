package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.keine.api.KeineRegistries;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleFastEntity;
import cc.thonly.reverie_dreams.item.base.ColoredSpawnEggItem;
import cc.thonly.reverie_dreams.registry.BuiltinObject;
import cc.thonly.reverie_dreams.registry.CodecStep;
import cc.thonly.reverie_dreams.registry.RegistryEntryOwnerBindable;
import cc.thonly.reverie_dreams.registry.RegistryEntryTranslatable;
import cc.thonly.reverie_dreams.registry.impl.RegistryImpl;
import cc.thonly.reverie_dreams.util.UnitCodec;
import com.mojang.serialization.Codec;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistrar;
import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistration;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.BalmItemRegistration;
import net.blay09.mods.balm.world.item.DeferredItem;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Setter
@Getter
public class NPCRole implements CodecStep<NPCRole>, RegistryEntryOwnerBindable<NPCRole>, BuiltinObject, RegistryEntryTranslatable {
    public static final Codec<NPCRole> CODEC = UnitCodec.unit(NPCRole::new);
    public static final List<Holder<EntityType<NPCRoleFastEntity>>> ENTITIES = new ArrayList<>();
    public static final List<DeferredItem> NPC_SPAWN_EGG_ITEM_LIST = new ArrayList<>();

    private Identifier id;
    private SkinType skinType;
    // 构建后属性
    private Holder<EntityType<NPCRoleFastEntity>> entityType;
    private DeferredItem spawnEgg;
    private boolean hasBuilt = false;

    private RegistryImpl<NPCRole> owner;

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

    public Holder<EntityType<NPCRoleFastEntity>> get() {
        return this.entityType;
    }

    public DeferredItem getEgg() {
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
            KeineRegistries keineRegistries = ReverieDreams.getKeineRegistries();
            Supplier<EntityType.Builder<NPCRoleFastEntity>> builderSupplier = () -> EntityType.Builder.of(
                    (type, world) -> new NPCRoleFastEntity(type, world, this.skinType),
                    MobCategory.MISC);
            BalmEntityTypeRegistration<NPCRoleFastEntity> entityTypeRegistration = registerEntity(this.id.getPath(), builderSupplier);
            entityTypeRegistration.withDefaultAttributes(BaseNPCLikeEntity::createLivingAttributes);
            Holder<EntityType<NPCRoleFastEntity>> entityTypeHolder = entityTypeRegistration.asHolder();

            String spawnEggId = this.id.getPath() + "_spawn_egg";
            DeferredItem spawnEgg = registerNPCSpawnEggItem(spawnEggId, (props) -> new ColoredSpawnEggItem(spawnEggId, entityTypeHolder.value(), new Item.Properties()));
            this.entityType = entityTypeHolder;
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

    protected static BalmEntityTypeRegistration<NPCRoleFastEntity> registerEntity(
            String name,
            Supplier<EntityType.Builder<NPCRoleFastEntity>> builderSupplier
    ) {
        BalmEntityTypeRegistrar entityTypeRegistrar = ReverieDreams.getEntityTypeRegistrar();
        BalmEntityTypeRegistration<NPCRoleFastEntity> entityTypeRegistration = entityTypeRegistrar.register(name, builderSupplier);
        Holder<EntityType<NPCRoleFastEntity>> holder = entityTypeRegistration.asHolder();
        ENTITIES.add(holder);
        return entityTypeRegistration;
    }

    protected static DeferredItem registerNPCSpawnEggItem(String name, Function<Item.Properties, Item> function) {
        BalmItemRegistrar itemRegistrar = ReverieDreams.getItemRegistrar();
        BalmItemRegistration registration = itemRegistrar.register(name, function);
        DeferredItem item = registration.asDeferredItem();
        NPC_SPAWN_EGG_ITEM_LIST.add(item);
        return registration.asDeferredItem();
    }

    private static ResourceKey<EntityType<?>> of(Identifier id) {
        return ResourceKey.create(Registries.ENTITY_TYPE, id);
    }
}
