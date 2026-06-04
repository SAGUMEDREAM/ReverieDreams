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
import cc.thonly.reverie_dreams.entity.npc.NPCRoleEntity;
import cc.thonly.reverie_dreams.entity.villager.FumoSeller;
import cc.thonly.reverie_dreams.entity.villager.TavernVillager;
import cc.thonly.reverie_dreams.item.base.ColoredSpawnEggItem;
import cc.thonly.reverie_dreams.mixin.accessor.PigVariantAccessor;
import cc.thonly.reverie_dreams.registry.content.skin.MobSkinTypes;
import cc.thonly.reverie_dreams.util.PlatformContext;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistrar;
import net.blay09.mods.balm.world.entity.BalmEntityTypeRegistration;
import net.blay09.mods.balm.world.item.BalmItemRegistrar;
import net.blay09.mods.balm.world.item.BalmItemRegistration;
import net.blay09.mods.balm.world.item.DeferredItem;
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
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings({"unchecked"})
public class RDEntityTypes {
    public static final Map<BalmEntityTypeRegistration<?>, DeferredItem> SPAWN_EGG_BIND = new Object2ObjectOpenHashMap<>(128);
    public static final List<DeferredItem> SPAWN_EGG_ITEM_LIST = new ArrayList<>(128);
    public static final List<BalmEntityTypeRegistration<?>> ENTITY_TYPES = new ArrayList<>(128);
    private static final int DANMAKU_RANGE = 4;
    private static final int DANMAKU_RATE = 10;

    public static Set<DeferredItem> getSpawnEggItemView() {
        return new LinkedHashSet<>(SPAWN_EGG_ITEM_LIST);
    }

    public static BalmEntityTypeRegistration<NPCRoleEntity> NPC_ROLE;
    public static BalmEntityTypeRegistration<DanmakuEntity> DANMAKU;
    public static BalmEntityTypeRegistration<BaguaFurnaceEntity> BAGUA_FURNACE;
    public static BalmEntityTypeRegistration<KnifeEntity> KNIFE;
    public static BalmEntityTypeRegistration<OreEspEntity> ORE_ESP;
    public static BalmEntityTypeRegistration<FumoSeller> FUMO_SELLER_VILLAGER;
    public static BalmEntityTypeRegistration<MagicBroom> MAGIC_BROOM;
    public static BalmEntityTypeRegistration<Wheelchair> WHEEL_CHAIR;
    public static BalmEntityTypeRegistration<Scarecrow> SCARECROW;
    public static BalmEntityTypeRegistration<KillerBee> KILLER_BEE;
    public static BalmEntityTypeRegistration<MoonRabbit> MOON_RABBIT;
    public static BalmEntityTypeRegistration<Ghost> GHOST;
    public static BalmEntityTypeRegistration<Yousei> YOUSEI;
    public static BalmEntityTypeRegistration<MaidYousei> MAID_YOUSEI;
    public static BalmEntityTypeRegistration<SunflowerYousei> SUNFLOWER_YOUSEI;
    public static BalmEntityTypeRegistration<Goblin> GOBLIN;
    public static BalmEntityTypeRegistration<RabbitUnit> RABBIT_UNIT;
    public static BalmEntityTypeRegistration<WaterElementalEntity> WATER_ELEMENTAL;
    public static BalmEntityTypeRegistration<FireElementalEntity> FIRE_ELEMENTAL;
    public static BalmEntityTypeRegistration<IceElementalEntity> ICE_ELEMENTAL;
    public static BalmEntityTypeRegistration<Hairball> HAIRBALL;
    public static BalmEntityTypeRegistration<UFO> UFO;
    public static BalmEntityTypeRegistration<MushroomMonster> MUSHROOM_MONSTER;
    public static BalmEntityTypeRegistration<WildPig> WILD_PIG;
    public static BalmEntityTypeRegistration<TavernVillager> TAVERN_VILLAGER;
    public static BalmEntityTypeRegistration<Oni> ONI;

    public static void initialize(BalmEntityTypeRegistrar registrar) {
        NPC_ROLE = registerEntity(registrar, "base_character",
                () -> EntityType.Builder.of(NPCRoleEntity::new, MobCategory.MISC),
                BaseNPCLikeEntity::createLivingAttributes);
        DANMAKU = registerEntity(registrar, "danmaku_bullet",
                () -> EntityType.Builder.<DanmakuEntity>of(DanmakuEntity::new, MobCategory.MISC)
                                        .sized(0.5f, 0.5f)
//                        .clientTrackingRange(DANMAKU_RANGE)
//                        .updateInterval(DANMAKU_RATE)
        );
        BAGUA_FURNACE = registerEntity(registrar, "bagua_furnace",
                () -> EntityType.Builder.of(BaguaFurnaceEntity::new, MobCategory.MISC));
        KNIFE = registerEntity(registrar, "knife",
                () -> EntityType.Builder.of(KnifeEntity::new, MobCategory.MISC));
        ORE_ESP = registerEntity(registrar, "ore_esp_entity",
                () -> EntityType.Builder.of(OreEspEntity::new, MobCategory.MISC));
        FUMO_SELLER_VILLAGER = registerEntityWithSpawnEgg(registrar, "fumo_seller_villager",
                () -> EntityType.Builder.<FumoSeller>of(FumoSeller::new, MobCategory.MISC)
                                        .sized(0.6f, 1.95f).eyeHeight(1.62f).clientTrackingRange(10),
                FumoSeller::createLivingAttributes
        );
        MAGIC_BROOM = registerEntityWithSpawnEgg(registrar, "broom",
                () -> EntityType.Builder.<MagicBroom>of(MagicBroom::new, MobCategory.MISC).sized(0.8f, 1f).ridingOffset(1f),
                MagicBroom::createLivingAttributes);
        WHEEL_CHAIR = registerEntityWithSpawnEgg(registrar, "wheel_chair",
                () -> EntityType.Builder.<Wheelchair>of(Wheelchair::new, MobCategory.MISC).ridingOffset(0.4f),
                Wheelchair::createLivingAttributes);
        SCARECROW = registerEntityWithSpawnEgg(registrar, "scarecrow",
                () -> EntityType.Builder.<Scarecrow>of(Scarecrow::new, MobCategory.MISC),
                Scarecrow::createLivingAttributes
        );
        KILLER_BEE = registerEntityWithSpawnEgg(registrar, "killer_bee",
                () -> EntityType.Builder.of(KillerBee::new, MobCategory.MONSTER),
                () -> Animal.createAnimalAttributes()
                            .add(Attributes.MAX_HEALTH, 10.0)
                            .add(Attributes.FLYING_SPEED, 0.6f)
                            .add(Attributes.MOVEMENT_SPEED, 0.3f)
                            .add(Attributes.ATTACK_DAMAGE, 2.0)
                            .add(Attributes.SCALE, 1.5f)
        );
        MOON_RABBIT = registerEntityWithSpawnEgg(registrar, "moon_rabbit",
                () -> EntityType.Builder.of(MoonRabbit::new, MobCategory.MONSTER),
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
        GHOST = registerEntityWithSpawnEgg(registrar, "ghost",
                () -> EntityType.Builder.of(Ghost::new, MobCategory.MONSTER),
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
        YOUSEI = registerEntityWithSpawnEgg(registrar, "yousei",
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
                                  .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
        );
        MAID_YOUSEI = registerEntityWithSpawnEgg(registrar, "maid_yousei",
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
                                  .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
        );
        SUNFLOWER_YOUSEI = registerEntityWithSpawnEgg(registrar, "sunflower_yousei",
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
                                  .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
        );
        GOBLIN = registerEntityWithSpawnEgg(registrar, "goblin",
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
                                  .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
        );
        RABBIT_UNIT = registerEntityWithSpawnEgg(registrar, "rabbit_unit",
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
                                  .add(Attributes.ENTITY_INTERACTION_RANGE, 3)
        );
        WATER_ELEMENTAL = registerEntityWithSpawnEgg(registrar, "water_elemental",
                () -> EntityType.Builder.of(WaterElementalEntity::new, MobCategory.MONSTER),
                WaterElementalEntity::createLivingAttributes
        );
        FIRE_ELEMENTAL = registerEntityWithSpawnEgg(registrar, "fire_elemental",
                () -> EntityType.Builder.of(FireElementalEntity::new, MobCategory.MONSTER),
                FireElementalEntity::createLivingAttributes
        );
        ICE_ELEMENTAL = registerEntityWithSpawnEgg(registrar, "ice_elemental",
                () -> EntityType.Builder.of(IceElementalEntity::new, MobCategory.MONSTER),
                IceElementalEntity::createLivingAttributes
        );
        HAIRBALL = registerEntityWithSpawnEgg(registrar, "hairball",
                () -> EntityType.Builder.of(Hairball::new, MobCategory.MONSTER).sized(1, 1),
                Hairball::createAttributes
        );
        UFO = registerEntityWithSpawnEgg(registrar, "ufo",
                () -> EntityType.Builder.<UFO>of(cc.thonly.reverie_dreams.entity.UFO::new, MobCategory.MONSTER).sized(1.5f, 1.5f).eyeHeight(1.5f * 0.5f),
                cc.thonly.reverie_dreams.entity.UFO::createAttributes
        );
        MUSHROOM_MONSTER = registerEntityWithSpawnEgg(registrar, "mushroom_monster",
                () -> EntityType.Builder.of(MushroomMonster::new, MobCategory.MONSTER),
                MushroomMonster::createAttributes
        );
        WILD_PIG = registerEntityWithSpawnEgg(registrar, "wild_pig",
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
        TAVERN_VILLAGER = registerEntityWithSpawnEgg(registrar, "tavern_villager",
                () -> EntityType.Builder.<TavernVillager>of(TavernVillager::new, MobCategory.MISC)
                                        .sized(0.6f, 1.95f)
                                        .eyeHeight(1.62f)
                                        .clientTrackingRange(10),
                TavernVillager::createLivingAttributes
        );
        ONI = registerEntityWithSpawnEgg(registrar, "oni",
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

    public static <T extends Entity> BalmEntityTypeRegistration<T> registerEntity(BalmEntityTypeRegistrar registrar, String name, Supplier<EntityType.Builder<T>> entityType) {
        BalmEntityTypeRegistration<T> entityTypeRegistration = registrar.register(name, entityType);
        ENTITY_TYPES.add(entityTypeRegistration);
        return entityTypeRegistration;
    }

    public static <T extends Entity> BalmEntityTypeRegistration<T> registerEntity(BalmEntityTypeRegistrar registrar, String path, @NotNull Supplier<EntityType.Builder<T>> entityType, CreateAttributesBuilderFunction function) {
        return registerEntity(registrar, path, entityType).withDefaultAttributes(function::apply);
    }

    public static <T extends Entity> @NotNull BalmEntityTypeRegistration<T> registerEntityWithSpawnEgg(BalmEntityTypeRegistrar registrar, String name, Supplier<EntityType.Builder<T>> entityType, CreateAttributesBuilderFunction function) {
        BalmEntityTypeRegistration<T> entityTypeRegistration = registrar.register(name, entityType).withDefaultAttributes(function::apply);
        String eggName = name + "_spawn_egg";
        BalmItemRegistration item = registerSpawnEggItem(eggName, entityTypeRegistration);
        SPAWN_EGG_ITEM_LIST.add(item.asDeferredItem());
        SPAWN_EGG_BIND.put(entityTypeRegistration, item.asDeferredItem());
        return entityTypeRegistration;
    }

    public static <T extends Entity> BalmItemRegistration registerSpawnEggItem(String eggName, BalmEntityTypeRegistration<T> entityTypeRegistration) {
        BalmItemRegistrar itemRegistrar = ReverieDreams.getItemRegistrar();
        BalmItemRegistration itemRegistration = itemRegistrar.register(eggName, props -> new ColoredSpawnEggItem(eggName, (EntityType<? extends Mob>) entityTypeRegistration.asHolder().value(), props));
        SPAWN_EGG_ITEM_LIST.add(itemRegistration.asDeferredItem());
        return itemRegistration;
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
