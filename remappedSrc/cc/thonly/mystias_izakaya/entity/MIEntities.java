package cc.thonly.mystias_izakaya.entity;

import cc.thonly.mystias_izakaya.MystiasIzakaya;
import cc.thonly.mystias_izakaya.entity.villager.TavernVillager;
import cc.thonly.registry_modifier.api.DynamicRegistryManagerCallback;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.item.base.SpawnEggItem;
import cc.thonly.reverie_dreams.util.IdentifierGetter;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalBiomeTags;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.PigVariant;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.entity.variant.SpawnPrioritySelectors;
import net.minecraft.world.item.Item;

@SuppressWarnings("unchecked")
public class MIEntities {
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
                            .build());
    public static final EntityType<TavernVillager> TAVERN_VILLAGER =
            ModEntities.registerEntityWithSpawnEgg("tavern_villager",
                    EntityType.Builder.<TavernVillager>of(TavernVillager::new, MobCategory.MISC)
                            .sized(0.6f, 1.95f).eyeHeight(1.62f).clientTrackingRange(10)
                            .build(ModEntities.of("tavern_villager")),
                    () -> TavernVillager.createLivingAttributes().build()
            );

    public static void init() {
        DynamicRegistryManagerCallback.Builder<PigVariant> pigVariantBuilder = DynamicRegistryManagerCallback.createBuilder(Registries.PIG_VARIANT);
        pigVariantBuilder.register(MystiasIzakaya.id("wild_pig"), new PigVariant(
                new ModelAndTexture<>(PigVariant.ModelType.NORMAL, MystiasIzakaya.id("entity/pig/wild_pig")),
                SpawnPrioritySelectors.EMPTY
        ));
        BiomeModifications.addSpawn(
                BiomeSelectors.tag(ConventionalBiomeTags.IS_FOREST),
                MobCategory.MONSTER,
                MIEntities.WILD_PIG,
                1,
                1,
                3
        );
    }

    private static ResourceKey<EntityType<?>> of(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, MystiasIzakaya.id(name));
    }

    private static <T extends Entity> EntityType<T> registerEntity(String path, EntityType<T> entityType) {
        EntityType<T> entityTypeRef = Registry.register(BuiltInRegistries.ENTITY_TYPE, MystiasIzakaya.id(path), entityType);
        PolymerEntityUtils.registerType(entityTypeRef);
        return entityTypeRef;
    }

    private static <T extends Entity> EntityType<T> registerEntityWithSpawnEgg(String path, EntityType<T> entityType, ModEntities.CreateAttributesFunction createAttributesFunction) {
        EntityType<T> entityTypeRef = Registry.register(BuiltInRegistries.ENTITY_TYPE, MystiasIzakaya.id(path), entityType);
        FabricDefaultAttributeRegistry.register((EntityType<? extends Mob>) entityTypeRef, createAttributesFunction.apply());
        Item item = registerSpawnEggItem(new SpawnEggItem(path + "_spawn_egg", (EntityType<? extends Mob>) entityTypeRef, new Item.Properties().modelId(MystiasIzakaya.id("spawn_egg"))));
        PolymerEntityUtils.registerType(entityTypeRef);
        ModEntities.SPAWN_EGG_ITEM_LIST.add(item);
        ModEntities.SPAWN_EGG_BIND.put(entityTypeRef, item);
        return entityTypeRef;
    }

    public static Item registerSpawnEggItem(IdentifierGetter item) {
        Registry.register(BuiltInRegistries.ITEM, item.getIdentifier(), (Item) item);
        ModEntities.SPAWN_EGG_ITEM_LIST.add((Item) item);
        return (Item) item;
    }

    @FunctionalInterface
    public interface CreateAttributesFunction {
        AttributeSupplier apply();
    }
}
