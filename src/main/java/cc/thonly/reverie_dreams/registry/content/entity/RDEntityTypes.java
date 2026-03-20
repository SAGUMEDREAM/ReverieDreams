package cc.thonly.reverie_dreams.registry.content.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.*;
import cc.thonly.reverie_dreams.entity.elemental.FireElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.WaterElementalEntity;
import cc.thonly.reverie_dreams.entity.misc.*;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.entity.villager.FumoSellerVillager;
import cc.thonly.reverie_dreams.entity.villager.TavernVillager;
import cc.thonly.reverie_dreams.item.base.SpawnEggItem;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.server.ServerContentRegistry;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.event.registry.DynamicRegistryView;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings({"unchecked", "DataFlowIssue"})
public class RDEntityTypes {
    public static final Map<EntityType<?>, Item> SPAWN_EGG_BIND = new Object2ObjectOpenHashMap<>(128);
    public static final List<Item> SPAWN_EGG_ITEM_LIST = new ArrayList<>(128);
    public static final List<EntityType<?>> ENTITY_TYPES = new ArrayList<>(128);

    public static Item registerSpawnEggItem(IdentifierGetter item) {
        Registry.register(BuiltInRegistries.ITEM, item.getIdentifier(), (Item) item);
        SPAWN_EGG_ITEM_LIST.add((Item) item);
        return (Item) item;
    }

    public static List<Item> getSpawnEggItemView() {
        return List.copyOf(SPAWN_EGG_ITEM_LIST);
    }

    public static final EntityType<NPCRoleEntity> NPC_ROLE =
            registerEntity("base_character",
                    EntityType.Builder.of(NPCRoleEntity::new, MobCategory.MISC)
                            .build(of("base_character")),
                    BaseNPCLikeEntity::createAttributes
            );
    public static final EntityType<DanmakuEntity> DANMAKU =
            registerEntity("danmaku_bullet",
                    EntityType.Builder.<DanmakuEntity>of(DanmakuEntity::new, MobCategory.MISC)
                            .build(of("danmaku_bullet")));
    public static final EntityType<BaguaFurnaceEntity> BAGUA_FURNACE =
            registerEntity("bagua_furnace",
                    EntityType.Builder.<BaguaFurnaceEntity>of(BaguaFurnaceEntity::new, MobCategory.MISC)
                            .build(of("bagua_furnace"))
            );
    public static final EntityType<KnifeEntity> KNIFE =
            registerEntity("knife",
                    EntityType.Builder.<KnifeEntity>of(KnifeEntity::new, MobCategory.MISC)
                            .build(of("knife")));
    public static final EntityType<OreEspEntity> ORE_ESP =
            registerEntity("ore_esp_entity",
                    EntityType.Builder.<OreEspEntity>of(OreEspEntity::new, MobCategory.MISC)
                            .build(of("ore_esp_entity")));
    public static final EntityType<FumoSellerVillager> FUMO_SELLER_VILLAGER =
            registerEntityWithSpawnEgg("fumo_seller_villager",
                    EntityType.Builder.<FumoSellerVillager>of(FumoSellerVillager::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f).eyeHeight(1.62f).clientTrackingRange(10)
                            .build(of("fumo_seller_villager")),
                    FumoSellerVillager::createLivingAttributes
            );
    public static final EntityType<MagicBroomEntity> MAGIC_BROOM = registerEntityWithSpawnEgg("broom",
            EntityType.Builder.<MagicBroomEntity>of(MagicBroomEntity::new, MobCategory.MISC)
                    .build(of("broom")),
            MagicBroomEntity::createAttributes);
    public static final EntityType<WheelchairEntity> WHEEL_CHAIR = registerEntityWithSpawnEgg("wheel_chair",
            EntityType.Builder.<WheelchairEntity>of(WheelchairEntity::new, MobCategory.MISC)
                    .build(of("wheel_chair")),
            WheelchairEntity::createAttributes);
    public static final EntityType<ScarecrowEntity> SCARECROW = registerEntityWithSpawnEgg("scarecrow",
            EntityType.Builder.<ScarecrowEntity>of(ScarecrowEntity::new, MobCategory.MISC)
                    .build(of("scarecrow")),
            ScarecrowEntity::createLivingAttributes
    );
    public static final EntityType<KillerBeeEntity> KILLER_BEE = registerEntityWithSpawnEgg("killer_bee",
            EntityType.Builder.<KillerBeeEntity>of(KillerBeeEntity::new, MobCategory.MONSTER)
                    .build(of("killer_bee")),
            () -> Animal.createAnimalAttributes()
                    .add(Attributes.MAX_HEALTH, 10.0)
                    .add(Attributes.FLYING_SPEED, 0.6f)
                    .add(Attributes.MOVEMENT_SPEED, 0.3f)
                    .add(Attributes.ATTACK_DAMAGE, 2.0)
                    .add(Attributes.SCALE, 1.5f)
    );
    public static final EntityType<MoonRabbitEntity> MOON_RABBIT = registerEntityWithSpawnEgg("moon_rabbit",
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
    );
    public static final EntityType<GhostEntity> GHOST = registerEntityWithSpawnEgg("ghost",
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
    );
    public static final EntityType<YouseiEntity> YOUSEI = registerEntityWithSpawnEgg("yousei",
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
    );
    public static final EntityType<MaidYouseiEntity> MAID_YOUSEI = registerEntityWithSpawnEgg("maid_yousei",
            EntityType.Builder.<MaidYouseiEntity>of(MaidYouseiEntity::new, MobCategory.MONSTER)
                    .build(of("maid_yousei")),
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
    );
    public static final EntityType<SunflowerYouseiEntity> SUNFLOWER_YOUSEI = registerEntityWithSpawnEgg("sunflower_yousei",
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
    );
    public static final EntityType<GoblinEntity> GOBLIN = registerEntityWithSpawnEgg("goblin",
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
    );
    public static final EntityType<RabbitUnitEntity> RABBIT_UNIT = registerEntityWithSpawnEgg("rabbit_unit",
            EntityType.Builder.<RabbitUnitEntity>of(RabbitUnitEntity::new, MobCategory.MONSTER)
                    .build(of("rabbit_unit")),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.2f)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 1.85f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
    );
    public static final EntityType<WaterElementalEntity> WATER_ELEMENTAL = registerEntityWithSpawnEgg("water_elemental",
            EntityType.Builder.<WaterElementalEntity>of(WaterElementalEntity::new, MobCategory.MONSTER)
                    .build(of("water_elemental")),
            WaterElementalEntity::createAttributes
    );
    public static final EntityType<FireElementalEntity> FIRE_ELEMENTAL = registerEntityWithSpawnEgg("fire_elemental",
            EntityType.Builder.<FireElementalEntity>of(FireElementalEntity::new, MobCategory.MONSTER)
                    .build(of("fire_elemental")),
            FireElementalEntity::createAttributes
    );
    public static final EntityType<IceElementalEntity> ICE_ELEMENTAL = registerEntityWithSpawnEgg("ice_elemental",
            EntityType.Builder.<IceElementalEntity>of(IceElementalEntity::new, MobCategory.MONSTER)
                    .build(of("ice_elemental")),
            IceElementalEntity::createAttributes
    );
    public static final EntityType<HairballEntity> HAIRBALL = registerEntityWithSpawnEgg("hairball",
            EntityType.Builder.<HairballEntity>of(HairballEntity::new, MobCategory.MONSTER)
                    .build(of("hairball")),
            HairballEntity::createAttributes
    );
    public static final EntityType<UfoEntity> UFO = registerEntityWithSpawnEgg("ufo",
            EntityType.Builder.<UfoEntity>of(UfoEntity::new, MobCategory.MONSTER)
                    .sized(1.5f, 1.5f)
                    .build(of("ufo")),
            UfoEntity::createMobAttributes
    );
    public static final EntityType<MushroomMonsterEntity> MUSHROOM_MONSTER = registerEntityWithSpawnEgg("mushroom_monster",
            EntityType.Builder.<MushroomMonsterEntity>of(MushroomMonsterEntity::new, MobCategory.MONSTER)
                    .build(of("mushroom_monster")),
            MushroomMonsterEntity::createAttributes
    );
    public static final EntityType<WildPigEntity> WILD_PIG =
            registerEntityWithSpawnEgg("wild_pig",
                    EntityType.Builder.<WildPigEntity>of(WildPigEntity::new, MobCategory.MONSTER)
                            .build(of("wild_pig")),
                    () -> Animal.createAnimalAttributes()
                            .add(Attributes.MAX_HEALTH, 20.0)
                            .add(Attributes.FLYING_SPEED, 0.6f)
                            .add(Attributes.MOVEMENT_SPEED, 0.3f)
                            .add(Attributes.ATTACK_DAMAGE, 2.0)
                            .add(Attributes.SCALE, 1.5f)
            );
    public static final EntityType<TavernVillager> TAVERN_VILLAGER =
            registerEntityWithSpawnEgg("tavern_villager",
                    EntityType.Builder.<TavernVillager>of(TavernVillager::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f).eyeHeight(1.62f).clientTrackingRange(10)
                            .build(of("tavern_villager")),
                    TavernVillager::createLivingAttributes
            );

    public static void registerEntityTypes() {
        DynamicRegistrySetupCallback.EVENT.register(dynamicRegistryView -> {
            Optional<Registry<PigVariant>> pigVariantRegistry = dynamicRegistryView.getOptional(Registries.PIG_VARIANT);
            if (pigVariantRegistry.isEmpty()) {
                return;
            }
            Registry<PigVariant> pigVariants = pigVariantRegistry.get();
            Registry.register(pigVariants, ReverieDreams.id("wild_pig"), new PigVariant(
                    new ModelAndTexture<>(PigVariant.ModelType.NORMAL, ReverieDreams.id("entity/pig/wild_pig")),
                    SpawnPrioritySelectors.EMPTY
            ));
        });
//        ServerContentRegistry.IMPL.register(Registries.PIG_VARIANT,
//                ReverieDreams.id("wild_pig"),
//                new PigVariant(
//                        new ModelAndTexture<>(PigVariant.ModelType.NORMAL, ReverieDreams.id("entity/pig/wild_pig")),
//                        SpawnPrioritySelectors.EMPTY
//                ));
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_FOREST),
                MobCategory.MONSTER,
                RDEntityTypes.WILD_PIG,
                1,
                1,
                3
        );
    }

    public static ResourceKey<EntityType<?>> of(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, ReverieDreams.id(name));
    }

    public static <T extends Entity> EntityType<T> registerEntity(String path, EntityType<T> entityType) {
        EntityType<T> entityTypeRef = Registry.register(BuiltInRegistries.ENTITY_TYPE, ReverieDreams.id(path), entityType);
        ENTITY_TYPES.add(entityType);
        return entityTypeRef;
    }

    public static <T extends Entity> EntityType<T> registerEntity(String path, @NotNull EntityType<T> entityType, CreateAttributesFunction createAttributesFunction) {
        registerEntity(path, entityType);
        FabricDefaultAttributeRegistry.register((EntityType<Mob>) entityType, createAttributesFunction.apply());
        return entityType;
    }

    public static <T extends Entity> @NotNull EntityType<T> registerEntityWithSpawnEgg(String path, EntityType<T> entityType, CreateAttributesFunction createAttributesFunction) {
        EntityType<T> entityTypeRef = Registry.register(BuiltInRegistries.ENTITY_TYPE, ReverieDreams.id(path), entityType);
        FabricDefaultAttributeRegistry.register((EntityType<? extends Mob>) entityTypeRef, createAttributesFunction.apply());
        Item item = registerSpawnEggItem(new SpawnEggItem(path + "_spawn_egg", (EntityType<? extends Mob>) entityTypeRef, new Item.Properties()));
        SPAWN_EGG_ITEM_LIST.add(item);
        SPAWN_EGG_BIND.put(entityTypeRef, item);
        return entityTypeRef;
    }

    public static <T extends Entity> EntityType<T> registerEntityWithSpawnEgg(String path, EntityType<T> entityType, CreateAttributesBuilderFunction createAttributesFunction) {
        return registerEntityWithSpawnEgg(path, entityType, () -> createAttributesFunction.apply().build());
    }

    @FunctionalInterface
    public interface CreateAttributesFunction {
        AttributeSupplier apply();
    }

    @FunctionalInterface
    public interface CreateAttributesBuilderFunction {
        AttributeSupplier.Builder apply();
    }
}
