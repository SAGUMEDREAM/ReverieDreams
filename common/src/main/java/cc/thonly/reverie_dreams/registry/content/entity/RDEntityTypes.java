package cc.thonly.reverie_dreams.registry.content.entity;

import cc.thonly.keine.api.KeineAPI;
import cc.thonly.keine.api.callback.DynamicRegistrySetupCallback;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.*;
import cc.thonly.reverie_dreams.entity.elemental.FireElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.WaterElementalEntity;
import cc.thonly.reverie_dreams.entity.misc.*;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCCompanionEntity;
import cc.thonly.reverie_dreams.entity.villager.FumoSeller;
import cc.thonly.reverie_dreams.entity.villager.TavernVillager;
import cc.thonly.reverie_dreams.item.base.ColoredSpawnEggItem;
import cc.thonly.reverie_dreams.mixin.accessor.PigVariantAccessor;
import cc.thonly.reverie_dreams.registry.MCBuiltInRegistries;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.registry.delegate.ItemDelegate;
import cc.thonly.reverie_dreams.util.PlatformContext;
import dev.architectury.registry.level.entity.EntityAttributeRegistry;
import dev.architectury.registry.registries.RegistrySupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.ClientAsset;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
public class RDEntityTypes {
    public static final Map<RegistrySupplier<EntityType<?>>, ItemDelegate> SPAWN_EGG_BIND = new Object2ObjectOpenHashMap<>(128);
    public static final List<RegistrySupplier<EntityType<?>>> ENTITY_TYPES = new ArrayList<>(128);
    public static final List<ItemDelegate> SPAWN_EGG_ITEM_LIST = new ArrayList<>(128);
    private static final int DANMAKU_RANGE = 4;
    private static final int DANMAKU_RATE = 10;

    public static Set<ItemDelegate> getSpawnEggItemView() {
        return new LinkedHashSet<>(SPAWN_EGG_ITEM_LIST);
    }

    public static final RegistrySupplier<EntityType<NPCCompanionEntity>> NPC_SIMPLE_ENTITY = registerEntity("base_character",
            () -> EntityType.Builder.of(NPCCompanionEntity::new, MobCategory.MISC)
                    .ridingOffset(-0.6F),
            BaseNPCLikeEntity::createLivingAttributes);
    public static final RegistrySupplier<EntityType<DanmakuEntity>> DANMAKU = registerEntity("danmaku_bullet",
            () -> EntityType.Builder.<DanmakuEntity>of(DanmakuEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
//                        .clientTrackingRange(DANMAKU_RANGE)
//                        .updateInterval(DANMAKU_RATE)
    );
    public static final RegistrySupplier<EntityType<BaguaFurnaceEntity>> BAGUA_FURNACE = registerEntity("bagua_furnace",
            () -> EntityType.Builder.of(BaguaFurnaceEntity::new, MobCategory.MISC));
    public static final RegistrySupplier<EntityType<KnifeEntity>> KNIFE = registerEntity("knife",
            () -> EntityType.Builder.of(KnifeEntity::new, MobCategory.MISC));
    public static final RegistrySupplier<EntityType<OreEspEntity>> ORE_ESP = registerEntity("ore_esp_entity",
            () -> EntityType.Builder.of(OreEspEntity::new, MobCategory.MISC));
    public static final RegistrySupplier<EntityType<SeatEntity>> SEAT = registerEntity("seat",
            () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .ridingOffset(0)
                    .sized(0.5f, 0.5f)
    );
    public static final RegistrySupplier<EntityType<FumoSeller>> FUMO_SELLER_VILLAGER = registerEntityWithSpawnEgg("fumo_seller_villager",
            () -> EntityType.Builder.<FumoSeller>of(FumoSeller::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f).eyeHeight(1.62f).clientTrackingRange(10),
            FumoSeller::createLivingAttributes
    );
    public static final RegistrySupplier<EntityType<MagicBroom>> MAGIC_BROOM = registerEntityWithSpawnEgg("broom",
            () -> EntityType.Builder.<MagicBroom>of(MagicBroom::new, MobCategory.MISC).sized(0.8f, 1f).ridingOffset(1f),
            MagicBroom::createLivingAttributes);
    public static final RegistrySupplier<EntityType<Wheelchair>> WHEEL_CHAIR = registerEntityWithSpawnEgg("wheel_chair",
            () -> EntityType.Builder.<Wheelchair>of(Wheelchair::new, MobCategory.MISC).ridingOffset(0.4f),
            Wheelchair::createLivingAttributes);
    public static final RegistrySupplier<EntityType<Scarecrow>> SCARECROW = registerEntityWithSpawnEgg("scarecrow",
            () -> EntityType.Builder.<Scarecrow>of(Scarecrow::new, MobCategory.MISC),
            Scarecrow::createLivingAttributes
    );
    public static final RegistrySupplier<EntityType<KillerBee>> KILLER_BEE = registerEntityWithSpawnEgg("killer_bee",
            () -> EntityType.Builder.<KillerBee>of(KillerBee::new, MobCategory.MONSTER).sized(0.7F, 0.6F).eyeHeight(0.3F).clientTrackingRange(8),
            () -> Animal.createAnimalAttributes()
                    .add(Attributes.MAX_HEALTH, 10.0)
                    .add(Attributes.FLYING_SPEED, 0.6f)
                    .add(Attributes.MOVEMENT_SPEED, 0.3f)
                    .add(Attributes.ATTACK_DAMAGE, 2.0)
                    .add(Attributes.SCALE, 1.5f)
    );
    public static final RegistrySupplier<EntityType<MoonRabbit>> MOON_RABBIT = registerEntityWithSpawnEgg("moon_rabbit",
            () -> EntityType.Builder.<MoonRabbit>of(MoonRabbit::new, MobCategory.MONSTER).sized(1, 2),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 18.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.12)
                    .add(Attributes.ATTACK_DAMAGE, 3)
                    .add(Attributes.SCALE, 1.15f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 8.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5)
    );
    public static final RegistrySupplier<EntityType<Ghost>> GHOST = registerEntityWithSpawnEgg("ghost",
            () -> EntityType.Builder.of(Ghost::new, MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.1)
                    .add(Attributes.ATTACK_DAMAGE, 3)
                    .add(Attributes.SCALE, 0.8f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 8.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5)
    );
    public static final RegistrySupplier<EntityType<Yousei>> YOUSEI = registerEntityWithSpawnEgg("yousei",
            () -> EntityType.Builder.of(Yousei::new, MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 25.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.15f)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 1.8f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5)
    );
    public static final RegistrySupplier<EntityType<MaidYousei>> MAID_YOUSEI = registerEntityWithSpawnEgg("maid_yousei",
            () -> EntityType.Builder.of(MaidYousei::new, MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 25.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.15f)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 1.8f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5)
    );
    public static final RegistrySupplier<EntityType<SunflowerYousei>> SUNFLOWER_YOUSEI = registerEntityWithSpawnEgg("sunflower_yousei",
            () -> EntityType.Builder.of((type, world) -> new SunflowerYousei(type, world, MobSkinTypes.SUNFLOWER_YOUSEI), MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.15f)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 0.6f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5)
    );
    public static final RegistrySupplier<EntityType<IceFairy>> ICE_FAIRY = registerEntityWithSpawnEgg("ice_fairy",
            () -> EntityType.Builder.<IceFairy>of(IceFairy::new, MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.2f)
                    .add(Attributes.ATTACK_DAMAGE, 1f)
                    .add(Attributes.SCALE, 1f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1f)
                    .add(Attributes.FOLLOW_RANGE, 16.0f)
                    .add(Attributes.TEMPT_RANGE, 10.0f)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5)
    );
    public static final RegistrySupplier<EntityType<Goblin>> GOBLIN = registerEntityWithSpawnEgg("goblin",
            () -> EntityType.Builder.of(Goblin::new, MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.2f)
                    .add(Attributes.ATTACK_DAMAGE, 1f)
                    .add(Attributes.SCALE, 1f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1f)
                    .add(Attributes.FOLLOW_RANGE, 16.0f)
                    .add(Attributes.TEMPT_RANGE, 10.0f)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5)
    );
    public static final RegistrySupplier<EntityType<RabbitUnit>> RABBIT_UNIT = registerEntityWithSpawnEgg("rabbit_unit",
            () -> EntityType.Builder.of(RabbitUnit::new, MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 30.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.2f)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 1.85f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5)
    );
    public static final RegistrySupplier<EntityType<WaterElementalEntity>> WATER_ELEMENTAL = registerEntityWithSpawnEgg("water_elemental",
            () -> EntityType.Builder.of(WaterElementalEntity::new, MobCategory.MONSTER),
            WaterElementalEntity::createLivingAttributes
    );
    public static final RegistrySupplier<EntityType<FireElementalEntity>> FIRE_ELEMENTAL = registerEntityWithSpawnEgg("fire_elemental",
            () -> EntityType.Builder.of(FireElementalEntity::new, MobCategory.MONSTER),
            FireElementalEntity::createLivingAttributes
    );
    public static final RegistrySupplier<EntityType<IceElementalEntity>> ICE_ELEMENTAL = registerEntityWithSpawnEgg("ice_elemental",
            () -> EntityType.Builder.of(IceElementalEntity::new, MobCategory.MONSTER),
            IceElementalEntity::createLivingAttributes
    );
    public static final RegistrySupplier<EntityType<Hairball>> HAIRBALL = registerEntityWithSpawnEgg("hairball",
            () -> EntityType.Builder.of(Hairball::new, MobCategory.MONSTER).sized(1, 1),
            Hairball::createAttributes
    );
    public static final RegistrySupplier<EntityType<UFO>> UFO = registerEntityWithSpawnEgg("ufo",
            () -> EntityType.Builder.<UFO>of(cc.thonly.reverie_dreams.entity.UFO::new, MobCategory.MONSTER).sized(1.5f, 1.5f).eyeHeight(1.5f * 0.5f),
            cc.thonly.reverie_dreams.entity.UFO::createAttributes
    );
    public static final RegistrySupplier<EntityType<MushroomMonster>> MUSHROOM_MONSTER = registerEntityWithSpawnEgg("mushroom_monster",
            () -> EntityType.Builder.of(MushroomMonster::new, MobCategory.MONSTER),
            MushroomMonster::createAttributes
    );
    public static RegistrySupplier<EntityType<WildPig>> WILD_PIG = registerEntityWithSpawnEgg("wild_pig",
            () -> EntityType.Builder.of(WildPig::new, MobCategory.MONSTER)
                    .sized(0.9F, 0.9F),
            () -> {
                AttributeSupplier.Builder builder = Animal.createAnimalAttributes()
                        .add(Attributes.MAX_HEALTH, 20.0)
                        .add(Attributes.FLYING_SPEED, 0.6f)
                        .add(Attributes.MOVEMENT_SPEED, 0.3f)
                        .add(Attributes.ATTACK_DAMAGE, 2.0);
                if (PlatformContext.hasPolymer()) {
                    builder.add(Attributes.SCALE, 1.5f);
                }
                return builder;
            }
    );
    public static RegistrySupplier<EntityType<TavernVillager>> TAVERN_VILLAGER = registerEntityWithSpawnEgg("tavern_villager",
            () -> EntityType.Builder.<TavernVillager>of(TavernVillager::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .clientTrackingRange(10),
            TavernVillager::createLivingAttributes
    );
    public static RegistrySupplier<EntityType<Oni>> ONI = registerEntityWithSpawnEgg("oni",
            () -> EntityType.Builder.of(Oni::new, MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 25.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, 0.22f)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 1.85f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
    );
    public static final RegistrySupplier<EntityType<ThrownCuisineItem>> THROWN_CUISINE_ITEM = registerEntity("thrown_cuisine_item",
            () -> EntityType.Builder.<ThrownCuisineItem>of(ThrownCuisineItem::new, MobCategory.MISC)
                    .noLootTable().sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10),
            null
    );
    public static final RegistrySupplier<EntityType<NPCFishingHook>> FISHING_BOBBER = registerEntity("fishing_bobber",
            () -> EntityType.Builder.<NPCFishingHook>of(NPCFishingHook::new, MobCategory.MISC)
                    .noLootTable()
                    .noSave()
                    .noSummon()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(5),
            null
    );

    public static void initialize() {
        DynamicRegistrySetupCallback.EVENT.register(dynamicRegistryView -> {
            Optional<Registry<PigVariant>> pigVariantRegistry = dynamicRegistryView.getOptional(Registries.PIG_VARIANT);
            if (pigVariantRegistry.isEmpty()) {
                return;
            }
            Registry<PigVariant> pigVariants = pigVariantRegistry.get();
            if (PlatformContext.isForgeLike()) {
                KeineAPI.getApi().unfreeze(pigVariants);
            }
            Registry.register(pigVariants, ReverieDreams.id("wild_pig"), PigVariantAccessor.invokeStaticInit(
                    new ModelAndTexture<>(PigVariant.ModelType.NORMAL, ReverieDreams.id("entity/pig/wild_pig")),
                    new ClientAsset.ResourceTexture(ReverieDreams.id("entity/pig/wild_pig"), ReverieDreams.id("entity/pig/wild_pig")))
            );
        });
    }

    public static ResourceKey<EntityType<?>> of(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, ReverieDreams.id(name));
    }

    public static <T extends Entity> RegistrySupplier<EntityType<T>> registerEntity(String name, Supplier<EntityType.Builder<T>> entityType) {
        RegistrySupplier<EntityType<T>> supplier = MCBuiltInRegistries.ENTITY_TYPE.register(name, () -> entityType.get().build(ResourceKey.create(Registries.ENTITY_TYPE, ReverieDreams.id(name))));
        RegistrySupplier<EntityType<?>> cast = (RegistrySupplier<EntityType<?>>) ((Object) supplier);
        ENTITY_TYPES.add(cast);
        return supplier;
    }

    public static <T extends Entity> RegistrySupplier<EntityType<T>> registerEntity(String path, @NotNull Supplier<EntityType.Builder<T>> entityType, @Nullable CreateAttributesBuilderFunction function) {
        RegistrySupplier<EntityType<T>> supplier = registerEntity(path, entityType);
        if (function != null) {
            EntityAttributeRegistry.register((Supplier) supplier, function::apply);
        }
        return supplier;
    }

    public static <T extends Entity> @NotNull RegistrySupplier<EntityType<T>> registerEntityWithSpawnEgg(String name, Supplier<EntityType.Builder<T>> entityType, CreateAttributesBuilderFunction function) {
        RegistrySupplier<EntityType<T>> supplier = MCBuiltInRegistries.ENTITY_TYPE.register(name, () -> entityType.get().build(of(name)));
        String eggName = name + "_spawn_egg";
        ItemDelegate item = registerSpawnEggItem(eggName, supplier);
        RegistrySupplier<EntityType<?>> cast = (RegistrySupplier<EntityType<?>>) ((Object) supplier);
        EntityAttributeRegistry.register((Supplier) supplier, function::apply);
        SPAWN_EGG_BIND.put(cast, item);
        return supplier;
    }

    public static <T extends Entity> ItemDelegate registerSpawnEggItem(String eggName, RegistrySupplier<EntityType<T>> supplier) {
        RegistrySupplier<Item> item = MCBuiltInRegistries.ITEM.register(eggName, () -> new ColoredSpawnEggItem(eggName, (EntityType<? extends Mob>) supplier.value(), new Item.Properties()));
        ItemDelegate itemDelegate = ItemDelegate.of(item);
        SPAWN_EGG_ITEM_LIST.add(itemDelegate);
        return itemDelegate;
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
