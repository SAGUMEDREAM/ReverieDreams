package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.Touhou;
import cc.thonly.reverie_dreams.entity.elemental.FireElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.WaterElementalEntity;
import cc.thonly.reverie_dreams.entity.misc.*;
import cc.thonly.reverie_dreams.entity.skin.MobSkins;
import cc.thonly.reverie_dreams.entity.villager.FumoSellerVillager;
import cc.thonly.reverie_dreams.item.base.SpawnEggItem;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ModEntities {
    public static final Map<EntityType<?>, Item> SPAWN_EGG_BIND = new Object2ObjectOpenHashMap<>();
    public static final List<Item> SPAWN_EGG_ITEM_LIST = new ArrayList<>();

    public static Item registerSpawnEggItem(IdentifierGetter item) {
        Registry.register(Registries.ITEM, item.getIdentifier(), (Item) item);
        SPAWN_EGG_ITEM_LIST.add((Item) item);
        return (Item) item;
    }

    public static List<Item> getSpawnEggItemView() {
        return List.copyOf(SPAWN_EGG_ITEM_LIST);
    }

    public static final EntityType<DanmakuEntity> DANMAKU_ENTITY_TYPE =
            registerEntity("danmaku_bullet",
                    EntityType.Builder.<DanmakuEntity>create(DanmakuEntity::new, SpawnGroup.MISC)
                            .build(of("danmaku_bullet")));
    public static final EntityType<KnifeEntity> KNIFE_ENTITY_TYPE =
            registerEntity("knife",
                    EntityType.Builder.<KnifeEntity>create(KnifeEntity::new, SpawnGroup.MISC)
                            .build(of("knife")));
    public static final EntityType<OreEspEntity> ORE_ESP_ENTITY_TYPE =
            registerEntity("ore_esp_entity",
                    EntityType.Builder.<OreEspEntity>create(OreEspEntity::new, SpawnGroup.MISC)
                            .build(of("ore_esp_entity")));
    public static final EntityType<FumoSellerVillager> FUMO_SELLER_VILLAGER =
            registerEntityWithSpawnEgg("fumo_seller_villager",
                    EntityType.Builder.<FumoSellerVillager>create(FumoSellerVillager::new, SpawnGroup.MISC)
                            .dimensions(0.6f, 1.95f).eyeHeight(1.62f).maxTrackingRange(10)
                            .build(of("fumo_seller_villager")),
                    () -> FumoSellerVillager.createLivingAttributes().build()
            );
    public static final EntityType<KillerBeeEntity> KILLER_BEE_ENTITY_TYPE =
            registerEntityWithSpawnEgg("killer_bee",
                    EntityType.Builder.<KillerBeeEntity>create(KillerBeeEntity::new, SpawnGroup.MONSTER)
                            .build(of("killer_bee")),
                    () -> AnimalEntity.createAnimalAttributes()
                            .add(EntityAttributes.MAX_HEALTH, 10.0)
                            .add(EntityAttributes.FLYING_SPEED, 0.6f)
                            .add(EntityAttributes.MOVEMENT_SPEED, 0.3f)
                            .add(EntityAttributes.ATTACK_DAMAGE, 2.0)
                            .add(EntityAttributes.SCALE, 1.5f)
                            .build());
    public static final EntityType<MoonRabbitEntity> MOON_RABBIT_ENTITY_TYPE =
            registerEntityWithSpawnEgg("moon_rabbit",
                    EntityType.Builder.<MoonRabbitEntity>create(MoonRabbitEntity::new, SpawnGroup.MONSTER)
                            .build(of("moon_rabbit")),
                    () -> LivingEntity.createLivingAttributes()
                            .add(EntityAttributes.MAX_HEALTH, 18.0)
                            .add(EntityAttributes.MOVEMENT_SPEED, 0.12)
                            .add(EntityAttributes.ATTACK_DAMAGE, 3)
                            .add(EntityAttributes.SCALE, 1.2f)
                            .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.1)
                            .add(EntityAttributes.FOLLOW_RANGE, 8.0)
                            .add(EntityAttributes.TEMPT_RANGE, 10.0)
                            .add(EntityAttributes.ENTITY_INTERACTION_RANGE, 3)
                            .build()
            );
    public static final EntityType<GhostEntity> GHOST_ENTITY_TYPE =
            registerEntityWithSpawnEgg("ghost",
                    EntityType.Builder.<GhostEntity>create(GhostEntity::new, SpawnGroup.MONSTER)
                            .build(of("ghost")),
                    () -> LivingEntity.createLivingAttributes()
                            .add(EntityAttributes.MAX_HEALTH, 20.0)
                            .add(EntityAttributes.MOVEMENT_SPEED, 0.1)
                            .add(EntityAttributes.ATTACK_DAMAGE, 3)
                            .add(EntityAttributes.SCALE, 0.8f)
                            .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.1)
                            .add(EntityAttributes.FOLLOW_RANGE, 8.0)
                            .add(EntityAttributes.TEMPT_RANGE, 10.0)
                            .add(EntityAttributes.ENTITY_INTERACTION_RANGE, 3)
                            .build());
    public static final EntityType<YouseiEntity> YOUSEI_ENTITY_TYPE = registerEntityWithSpawnEgg("yousei",
            EntityType.Builder.<YouseiEntity>create(YouseiEntity::new, SpawnGroup.MONSTER)
                    .build(of("yousei")),
            () -> LivingEntity.createLivingAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 25.0)
                    .add(EntityAttributes.FLYING_SPEED, 0.8f)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.15f)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5)
                    .add(EntityAttributes.SCALE, 1.8f)
                    .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(EntityAttributes.FOLLOW_RANGE, 16.0)
                    .add(EntityAttributes.TEMPT_RANGE, 10.0)
                    .add(EntityAttributes.ENTITY_INTERACTION_RANGE, 3)
                    .build());
    public static final EntityType<SunflowerYouseiEntity> SUNFLOWER_YOUSEI_ENTITY_TYPE = registerEntityWithSpawnEgg("sunflower_yousei",
            EntityType.Builder.<SunflowerYouseiEntity>create((type, world) -> new SunflowerYouseiEntity(type, world, MobSkins.SUNFLOWER_YOUSEI.get()), SpawnGroup.MONSTER)
                    .build(of("sunflower_yousei")),
            () -> LivingEntity.createLivingAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 30.0)
                    .add(EntityAttributes.FLYING_SPEED, 0.8f)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.15f)
                    .add(EntityAttributes.ATTACK_DAMAGE, 0.5)
                    .add(EntityAttributes.SCALE, 0.6f)
                    .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(EntityAttributes.FOLLOW_RANGE, 16.0)
                    .add(EntityAttributes.TEMPT_RANGE, 10.0)
                    .add(EntityAttributes.ENTITY_INTERACTION_RANGE, 3)
                    .build());
    public static final EntityType<GoblinEntity> GOBLIN_ENTITY_TYPE = registerEntityWithSpawnEgg("goblin",
            EntityType.Builder.<GoblinEntity>create(GoblinEntity::new, SpawnGroup.MONSTER)
                    .build(of("goblin")),
            () -> LivingEntity.createLivingAttributes()
                    .add(EntityAttributes.MAX_HEALTH, 30.0)
                    .add(EntityAttributes.FLYING_SPEED, 0.8f)
                    .add(EntityAttributes.MOVEMENT_SPEED, 0.2f)
                    .add(EntityAttributes.ATTACK_DAMAGE, 1f)
                    .add(EntityAttributes.SCALE, 1f)
                    .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.1f)
                    .add(EntityAttributes.FOLLOW_RANGE, 16.0f)
                    .add(EntityAttributes.TEMPT_RANGE, 10.0f)
                    .add(EntityAttributes.ENTITY_INTERACTION_RANGE, 3)
                    .build());
    public static final EntityType<WaterElementalEntity> WATER_ELEMENTAL_ENTITY_TYPE = registerEntityWithSpawnEgg("water_elemental",
            EntityType.Builder.<WaterElementalEntity>create(WaterElementalEntity::new, SpawnGroup.MONSTER)
                    .build(of("water_elemental")),
            WaterElementalEntity::createAttributes
    );
    public static final EntityType<FireElementalEntity> FIRE_ELEMENTAL_ENTITY_TYPE = registerEntityWithSpawnEgg("fire_elemental",
            EntityType.Builder.<FireElementalEntity>create(FireElementalEntity::new, SpawnGroup.MONSTER)
                    .build(of("fire_elemental")),
            FireElementalEntity::createAttributes
    );
    public static final EntityType<IceElementalEntity> ICE_ELEMENTAL_ENTITY_TYPE = registerEntityWithSpawnEgg("ice_elemental",
            EntityType.Builder.<IceElementalEntity>create(IceElementalEntity::new, SpawnGroup.MONSTER)
                    .build(of("ice_elemental")),
            IceElementalEntity::createAttributes
    );
    public static final EntityType<MagicBroomEntity> BROOM_ENTITY_TYPE = registerEntityWithSpawnEgg("broom",
            EntityType.Builder.<MagicBroomEntity>create(MagicBroomEntity::new, SpawnGroup.MISC)
                    .build(of("broom")),
            MagicBroomEntity::createAttributes);
    public static final EntityType<WheelchairEntity> WHEEL_CHAIR_ENTITY = registerEntityWithSpawnEgg("wheel_chair",
            EntityType.Builder.<WheelchairEntity>create(WheelchairEntity::new, SpawnGroup.MISC)
                    .build(of("wheel_chair")),
            WheelchairEntity::createAttributes);
    public static final EntityType<HairballEntity> HAIRBALL_ENTITY_TYPE = registerEntityWithSpawnEgg("hairball",
            EntityType.Builder.<HairballEntity>create(HairballEntity::new, SpawnGroup.MONSTER)
                    .build(of("hairball")),
            HairballEntity::createAttributes
    );
    public static final EntityType<MushroomMonsterEntity> MUSHROOM_MONSTER_ENTITY_TYPE = registerEntityWithSpawnEgg("mushroom_monster",
            EntityType.Builder.<MushroomMonsterEntity>create(MushroomMonsterEntity::new, SpawnGroup.MONSTER)
                    .build(of("mushroom_monster")),
            MushroomMonsterEntity::createAttributes
    );

    public static void registerEntities() {
    }

    public static RegistryKey<EntityType<?>> of(String name) {
        return RegistryKey.of(RegistryKeys.ENTITY_TYPE, Touhou.id(name));
    }

    public static <T extends Entity> EntityType<T> registerEntity(String path, EntityType<T> entityType) {
        EntityType<T> entityTypeRef = Registry.register(Registries.ENTITY_TYPE, Touhou.id(path), entityType);
        PolymerEntityUtils.registerType(entityTypeRef);
        return entityTypeRef;
    }

    public static <T extends Entity> EntityType<T> registerEntityWithSpawnEgg(String path, EntityType<T> entityType, CreateAttributesFunction createAttributesFunction) {
        EntityType<T> entityTypeRef = Registry.register(Registries.ENTITY_TYPE, Touhou.id(path), entityType);
        FabricDefaultAttributeRegistry.register((EntityType<? extends MobEntity>) entityTypeRef, createAttributesFunction.apply());
        Item item = registerSpawnEggItem(new SpawnEggItem(path + "_spawn_egg", (EntityType<? extends MobEntity>) entityTypeRef, new Item.Settings().modelId(Touhou.id("spawn_egg"))));
        PolymerEntityUtils.registerType(entityTypeRef);
        SPAWN_EGG_ITEM_LIST.add(item);
        SPAWN_EGG_BIND.put(entityTypeRef, item);
        return entityTypeRef;
    }

    @FunctionalInterface
    public interface CreateAttributesFunction {
        DefaultAttributeContainer apply();
    }
}
