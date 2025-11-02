package cc.thonly.reverie_dreams.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.elemental.FireElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.WaterElementalEntity;
import cc.thonly.reverie_dreams.entity.misc.*;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.entity.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.entity.villager.FumoSellerVillager;
import cc.thonly.reverie_dreams.item.base.SpawnEggItem;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class ModEntities {
    public static final Map<EntityType<?>, Item> SPAWN_EGG_BIND = new Object2ObjectOpenHashMap<>();
    public static final List<Item> SPAWN_EGG_ITEM_LIST = new ArrayList<>();

    public static Item registerSpawnEggItem(IdentifierGetter item) {
        Registry.register(BuiltInRegistries.ITEM, item.getIdentifier(), (Item) item);
        SPAWN_EGG_ITEM_LIST.add((Item) item);
        return (Item) item;
    }

    public static List<Item> getSpawnEggItemView() {
        return List.copyOf(SPAWN_EGG_ITEM_LIST);
    }

    public static final EntityType<NPCRoleEntity> NPC_ROLE_ENTITY =
            registerEntity("base_character",
                    EntityType.Builder.of(NPCRoleEntity::new, MobCategory.MISC)
                            .build(of("base_character")),
                    BaseNPCLikeEntity::createAttributes
            );
    public static final EntityType<DanmakuEntity> DANMAKU_ENTITY_TYPE =
            registerEntity("danmaku_bullet",
                    EntityType.Builder.<DanmakuEntity>of(DanmakuEntity::new, MobCategory.MISC)
                            .build(of("danmaku_bullet")));
    public static final EntityType<BaguaFurnaceEntity> BAGUA_FURNACE_ENTITY =
            registerEntity("bagua_furnace",
                    EntityType.Builder.<BaguaFurnaceEntity>of(BaguaFurnaceEntity::new, MobCategory.MISC)
                            .build(of("bagua_furnace"))
            );
    public static final EntityType<KnifeEntity> KNIFE_ENTITY_TYPE =
            registerEntity("knife",
                    EntityType.Builder.<KnifeEntity>of(KnifeEntity::new, MobCategory.MISC)
                            .build(of("knife")));
    public static final EntityType<OreEspEntity> ORE_ESP_ENTITY_TYPE =
            registerEntity("ore_esp_entity",
                    EntityType.Builder.<OreEspEntity>of(OreEspEntity::new, MobCategory.MISC)
                            .build(of("ore_esp_entity")));
    public static final EntityType<FumoSellerVillager> FUMO_SELLER_VILLAGER =
            registerEntityWithSpawnEgg("fumo_seller_villager",
                    EntityType.Builder.<FumoSellerVillager>of(FumoSellerVillager::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f).eyeHeight(1.62f).clientTrackingRange(10)
                            .build(of("fumo_seller_villager")),
                    () -> FumoSellerVillager.createLivingAttributes().build()
            );
    public static final EntityType<KillerBeeEntity> KILLER_BEE_ENTITY_TYPE =
            registerEntityWithSpawnEgg("killer_bee",
                    EntityType.Builder.<KillerBeeEntity>of(KillerBeeEntity::new, MobCategory.MONSTER)
                            .build(of("killer_bee")),
                    () -> Animal.createAnimalAttributes()
                            .add(Attributes.MAX_HEALTH, 10.0)
                            .add(Attributes.FLYING_SPEED, 0.6f)
                            .add(Attributes.MOVEMENT_SPEED, 0.3f)
                            .add(Attributes.ATTACK_DAMAGE, 2.0)
                            .add(Attributes.SCALE, 1.5f)
                            .build());
    public static final EntityType<MoonRabbitEntity> MOON_RABBIT_ENTITY_TYPE =
            registerEntityWithSpawnEgg("moon_rabbit",
                    EntityType.Builder.<MoonRabbitEntity>of(MoonRabbitEntity::new, MobCategory.MONSTER)
                            .build(of("moon_rabbit")),
                    () -> LivingEntity.createLivingAttributes()
                            .add(Attributes.MAX_HEALTH, 18.0)
                            .add(Attributes.MOVEMENT_SPEED, 0.12)
                            .add(Attributes.ATTACK_DAMAGE, 3)
                            .add(Attributes.SCALE, 1.2f)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                            .add(Attributes.FOLLOW_RANGE, 8.0)
                            .add(Attributes.TEMPT_RANGE, 10.0)
                            .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
                            .build()
            );
    public static final EntityType<GhostEntity> GHOST_ENTITY_TYPE =
            registerEntityWithSpawnEgg("ghost",
                    EntityType.Builder.<GhostEntity>of(GhostEntity::new, MobCategory.MONSTER)
                            .build(of("ghost")),
                    () -> LivingEntity.createLivingAttributes()
                            .add(Attributes.MAX_HEALTH, 20.0)
                            .add(Attributes.MOVEMENT_SPEED, 0.1)
                            .add(Attributes.ATTACK_DAMAGE, 3)
                            .add(Attributes.SCALE, 0.8f)
                            .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                            .add(Attributes.FOLLOW_RANGE, 8.0)
                            .add(Attributes.TEMPT_RANGE, 10.0)
                            .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
                            .build());
    public static final EntityType<YouseiEntity> YOUSEI_ENTITY_TYPE = registerEntityWithSpawnEgg("yousei",
            EntityType.Builder.<YouseiEntity>of(YouseiEntity::new, MobCategory.MONSTER)
                    .build(of("yousei")),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 25.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.15f)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 1.8f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
                    .build());
    public static final EntityType<MaidYouseiEntity> MAID_YOUSEI_ENTITY_TYPE = registerEntityWithSpawnEgg("maid_yousei",
            EntityType.Builder.<MaidYouseiEntity>of(MaidYouseiEntity::new, MobCategory.MONSTER)
                    .build(of("yousei")),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 25.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.15f)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 1.8f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
                    .build()
            );
    public static final EntityType<SunflowerYouseiEntity> SUNFLOWER_YOUSEI_ENTITY_TYPE = registerEntityWithSpawnEgg("sunflower_yousei",
            EntityType.Builder.<SunflowerYouseiEntity>of((type, world) -> new SunflowerYouseiEntity(type, world, MobSkinTypes.SUNFLOWER_YOUSEI), MobCategory.MONSTER)
                    .build(of("sunflower_yousei")),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.15f)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 0.6f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
                    .build());
    public static final EntityType<GoblinEntity> GOBLIN_ENTITY_TYPE = registerEntityWithSpawnEgg("goblin",
            EntityType.Builder.<GoblinEntity>of(GoblinEntity::new, MobCategory.MONSTER)
                    .build(of("goblin")),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.2f)
                    .add(Attributes.ATTACK_DAMAGE, 1f)
                    .add(Attributes.SCALE, 1f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1f)
                    .add(Attributes.FOLLOW_RANGE, 16.0f)
                    .add(Attributes.TEMPT_RANGE, 10.0f)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
                    .build());
    public static final EntityType<WaterElementalEntity> WATER_ELEMENTAL_ENTITY_TYPE = registerEntityWithSpawnEgg("water_elemental",
            EntityType.Builder.<WaterElementalEntity>of(WaterElementalEntity::new, MobCategory.MONSTER)
                    .build(of("water_elemental")),
            WaterElementalEntity::createAttributes
    );
    public static final EntityType<FireElementalEntity> FIRE_ELEMENTAL_ENTITY_TYPE = registerEntityWithSpawnEgg("fire_elemental",
            EntityType.Builder.<FireElementalEntity>of(FireElementalEntity::new, MobCategory.MONSTER)
                    .build(of("fire_elemental")),
            FireElementalEntity::createAttributes
    );
    public static final EntityType<IceElementalEntity> ICE_ELEMENTAL_ENTITY_TYPE = registerEntityWithSpawnEgg("ice_elemental",
            EntityType.Builder.<IceElementalEntity>of(IceElementalEntity::new, MobCategory.MONSTER)
                    .build(of("ice_elemental")),
            IceElementalEntity::createAttributes
    );
    public static final EntityType<MagicBroomEntity> BROOM_ENTITY_TYPE = registerEntityWithSpawnEgg("broom",
            EntityType.Builder.<MagicBroomEntity>of(MagicBroomEntity::new, MobCategory.MISC)
                    .build(of("broom")),
            MagicBroomEntity::createAttributes);
    public static final EntityType<WheelchairEntity> WHEEL_CHAIR_ENTITY = registerEntityWithSpawnEgg("wheel_chair",
            EntityType.Builder.<WheelchairEntity>of(WheelchairEntity::new, MobCategory.MISC)
                    .build(of("wheel_chair")),
            WheelchairEntity::createAttributes);
    public static final EntityType<HairballEntity> HAIRBALL_ENTITY_TYPE = registerEntityWithSpawnEgg("hairball",
            EntityType.Builder.<HairballEntity>of(HairballEntity::new, MobCategory.MONSTER)
                    .build(of("hairball")),
            HairballEntity::createAttributes
    );
    public static final EntityType<MushroomMonsterEntity> MUSHROOM_MONSTER_ENTITY_TYPE = registerEntityWithSpawnEgg("mushroom_monster",
            EntityType.Builder.<MushroomMonsterEntity>of(MushroomMonsterEntity::new, MobCategory.MONSTER)
                    .build(of("mushroom_monster")),
            MushroomMonsterEntity::createAttributes
    );

    public static void registerEntities() {
    }

    public static ResourceKey<EntityType<?>> of(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, ReverieDreams.id(name));
    }

    public static <T extends Entity> EntityType<T> registerEntity(String path, EntityType<T> entityType) {
        EntityType<T> entityTypeRef = Registry.register(BuiltInRegistries.ENTITY_TYPE, ReverieDreams.id(path), entityType);
        PolymerEntityUtils.registerType(entityTypeRef);
        return entityTypeRef;
    }

    public static <T extends Entity> EntityType<T> registerEntity(String path, EntityType<T> entityType, CreateAttributesFunction createAttributesFunction) {
        registerEntity(path, entityType);
        FabricDefaultAttributeRegistry.register((EntityType<? extends Mob>) entityType, createAttributesFunction.apply());
        return entityType;
    }

    public static <T extends Entity> EntityType<T> registerEntityWithSpawnEgg(String path, EntityType<T> entityType, CreateAttributesFunction createAttributesFunction) {
        EntityType<T> entityTypeRef = Registry.register(BuiltInRegistries.ENTITY_TYPE, ReverieDreams.id(path), entityType);
        FabricDefaultAttributeRegistry.register((EntityType<? extends Mob>) entityTypeRef, createAttributesFunction.apply());
        Item item = registerSpawnEggItem(new SpawnEggItem(path + "_spawn_egg", (EntityType<? extends Mob>) entityTypeRef, new Item.Properties().modelId(ReverieDreams.id("spawn_egg"))));
        PolymerEntityUtils.registerType(entityTypeRef);
        SPAWN_EGG_ITEM_LIST.add(item);
        SPAWN_EGG_BIND.put(entityTypeRef, item);
        return entityTypeRef;
    }

    @FunctionalInterface
    public interface CreateAttributesFunction {
        AttributeSupplier apply();
    }
}
