package cc.thonly.reverie_dreams.registry.content.entity;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.*;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.entity.aquatic.CuisineAquaticEntity;
import cc.thonly.reverie_dreams.entity.elemental.FireElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.IceElementalEntity;
import cc.thonly.reverie_dreams.entity.elemental.WaterElementalEntity;
import cc.thonly.reverie_dreams.entity.misc.*;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCCompanionEntity;
import cc.thonly.reverie_dreams.entity.projectile.BaguaFireball;
import cc.thonly.reverie_dreams.entity.projectile.ThrownExorcismPaper;
import cc.thonly.reverie_dreams.entity.projectile.ThrownIceBlock;
import cc.thonly.reverie_dreams.entity.projectile.ThrownIceSnowball;
import cc.thonly.reverie_dreams.entity.projectile.ThrownTouhouTrident;
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
import net.minecraft.world.entity.animal.cow.Cow;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.animal.pig.PigVariant;
import net.minecraft.world.entity.variant.ModelAndTexture;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import cc.thonly.reverie_dreams.entity.variant.HumanoidModelDimensions;
import cc.thonly.reverie_dreams.entity.variant.YoukaiSpecies;

@SuppressWarnings({"unchecked", "rawtypes"})
public class RDEntityTypes {
    public static final Map<RegistrySupplier<EntityType<?>>, ItemDelegate> SPAWN_EGG_BIND = new Object2ObjectOpenHashMap<>(128);
    public static final List<RegistrySupplier<EntityType<?>>> ENTITY_TYPES = new ArrayList<>(128);
    public static final List<ItemDelegate> SPAWN_EGG_ITEM_LIST = new ArrayList<>(128);
    private static final int DANMAKU_RANGE = 4;
    private static final int DANMAKU_RATE = 10;
    private static final int YOUKAI_RABBIT_EGG_COLOR = 0xD590A9;

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
    public static final RegistrySupplier<EntityType<DanmakuFamiliarEntity>> DANMAKU_FAMILIAR = registerEntity("danmaku_familiar",
            () -> EntityType.Builder.<DanmakuFamiliarEntity>of(DanmakuFamiliarEntity::new, MobCategory.MISC)
                    .noSave()
                    .noSummon()
                    .sized(0.6f, 0.6f)
    );
    public static final RegistrySupplier<EntityType<DanmakuLaserEntity>> DANMAKU_LASER = registerEntity("danmaku_laser",
            () -> EntityType.Builder.<DanmakuLaserEntity>of(DanmakuLaserEntity::new, MobCategory.MISC)
                    .noSave()
                    .noSummon()
                    .sized(0.5f, 0.5f)
    );
    public static final RegistrySupplier<EntityType<BaguaFurnaceEntity>> BAGUA_FURNACE = registerEntity("bagua_furnace",
            () -> EntityType.Builder.of(BaguaFurnaceEntity::new, MobCategory.MISC));
    public static final RegistrySupplier<EntityType<KnifeEntity>> KNIFE = registerEntity("knife",
            () -> EntityType.Builder.of(KnifeEntity::new, MobCategory.MISC));
    public static final RegistrySupplier<EntityType<OreEspEntity>> ORE_ESP = registerEntity("ore_esp_entity",
            () -> EntityType.Builder.of(OreEspEntity::new, MobCategory.MISC)
                    .sized(0.0F, 0.0F)
                    .noSave());
    public static final RegistrySupplier<EntityType<GapPortalEntity>> GAP_PORTAL = registerEntity("gap_portal",
            () -> EntityType.Builder.<GapPortalEntity>of(GapPortalEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .noSave()
                    .noSummon()
                    .fireImmune()
                    .sized(1.1F, 2.2F)
                    .clientTrackingRange(8)
                    .updateInterval(5),
            null
    );
    public static final RegistrySupplier<EntityType<DreamCocoonEntity>> DREAM_COCOON = registerEntity("dream_cocoon",
            () -> EntityType.Builder.<DreamCocoonEntity>of(DreamCocoonEntity::new, MobCategory.MISC)
                    .noLootTable().noSummon().sized(0.9F, 2.0F).clientTrackingRange(8).updateInterval(2));
    public static final RegistrySupplier<EntityType<CursedDecoyEntity>> CURSED_DECOY = registerEntity("cursed_decoy",
            () -> EntityType.Builder.<CursedDecoyEntity>of(CursedDecoyEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .noSave()
                    .noSummon()
                    .fireImmune()
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .updateInterval(5),
            () -> ArmorStand.createAttributes()
    );
    public static final RegistrySupplier<EntityType<SeatEntity>> SEAT = registerEntity("seat",
            () -> EntityType.Builder.<SeatEntity>of(SeatEntity::new, MobCategory.MISC)
                    .noLootTable()
                    .ridingOffset(0)
                    .sized(0.5f, 0.5f)
    );
    public static final RegistrySupplier<EntityType<CushionSeatEntity>> CUSHION_SEAT = registerEntity("cushion_seat",
            () -> EntityType.Builder.<CushionSeatEntity>of(CushionSeatEntity::new, MobCategory.MISC)
                    .noLootTable().sized(0.5f, 1.0f / 16.0f)
                    .passengerAttachments(0.0f).ridingOffset(0).clientTrackingRange(4).updateInterval(1)
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
            () -> EntityType.Builder.<Wheelchair>of(Wheelchair::new, MobCategory.MISC).sized(1, 1.8f).ridingOffset(0.4f),
            Wheelchair::createLivingAttributes);
    public static final RegistrySupplier<EntityType<Scarecrow>> SCARECROW = registerEntityWithSpawnEgg("scarecrow",
            () -> EntityType.Builder.<Scarecrow>of(Scarecrow::new, MobCategory.MISC),
            Scarecrow::createLivingAttributes
    );
    public static final RegistrySupplier<EntityType<KillerBee>> KILLER_BEE = registerEntityWithSpawnEgg("killer_bee", 0xD99529,
            () -> EntityType.Builder.<KillerBee>of(KillerBee::new, MobCategory.MONSTER).sized(0.7F, 0.6F).eyeHeight(0.3F).clientTrackingRange(8),
            () -> Animal.createAnimalAttributes()
                    .add(Attributes.MAX_HEALTH, 10.0)
                    .add(Attributes.FLYING_SPEED, 0.6f)
                    .add(Attributes.MOVEMENT_SPEED, 0.3f)
                    .add(Attributes.ATTACK_DAMAGE, 2.0)
                    .add(Attributes.SCALE, 1.5f)
    );
    public static final RegistrySupplier<EntityType<ForestYoukai>> YOUKAI_CAT = registerEntityWithSpawnEgg("youkai_cat", 0x536E87,
            () -> EntityType.Builder.of((EntityType<ForestYoukai> type, net.minecraft.world.level.Level level) ->
                    new ForestYoukai(type, level, YoukaiSpecies.CAT), MobCategory.MONSTER).sized(.6F, 1.8F).eyeHeight(1.62F),
            YoukaiSpecies.CAT::attributes);
    public static final RegistrySupplier<EntityType<ForestYoukai>> YOUKAI_TANUKI = registerEntityWithSpawnEgg("youkai_tanuki", 0x706449,
            () -> EntityType.Builder.of((EntityType<ForestYoukai> type, net.minecraft.world.level.Level level) ->
                    new ForestYoukai(type, level, YoukaiSpecies.TANUKI), MobCategory.MONSTER).sized(.6F, 1.8F).eyeHeight(1.62F),
            YoukaiSpecies.TANUKI::attributes);
    public static final RegistrySupplier<EntityType<ForestYoukai>> YOUKAI_FOX = registerEntityWithSpawnEgg("youkai_fox", 0xB28E54,
            () -> EntityType.Builder.of((EntityType<ForestYoukai> type, net.minecraft.world.level.Level level) ->
                    new ForestYoukai(type, level, YoukaiSpecies.FOX), MobCategory.MONSTER).sized(.6F, 1.8F).eyeHeight(1.62F),
            YoukaiSpecies.FOX::attributes);
    public static final RegistrySupplier<EntityType<YoukaiRabbit>> YOUKAI_RABBIT = registerEntityWithSpawnEgg("youkai_rabbit", YOUKAI_RABBIT_EGG_COLOR,
            () -> EntityType.Builder.of(YoukaiRabbit::new, MobCategory.CREATURE).sized(.65F, 1.9375F).eyeHeight(1.59375F),
            YoukaiRabbit::createAttributes);
    // Read old worlds without retaining a second playable species or a summon entry.
    public static final RegistrySupplier<EntityType<YoukaiRabbit>> LEGACY_MOON_RABBIT = registerEntity("moon_rabbit",
            () -> EntityType.Builder.of(YoukaiRabbit::new, MobCategory.MISC).sized(.65F, 1.9375F).eyeHeight(1.59375F).noSummon().noLootTable(),
            YoukaiRabbit::createAttributes);
    public static final ItemDelegate LEGACY_MOON_RABBIT_EGG = registerSpawnEggItem("moon_rabbit_spawn_egg", YOUKAI_RABBIT, YOUKAI_RABBIT_EGG_COLOR);
    static { SPAWN_EGG_ITEM_LIST.remove(LEGACY_MOON_RABBIT_EGG); }
    public static final RegistrySupplier<EntityType<Ghost>> GHOST = registerEntityWithSpawnEgg("ghost", 0xA699BE,
            () -> EntityType.Builder.of(Ghost::new, MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 20.0)
                    .add(Attributes.MOVEMENT_SPEED, 0.18)
                    .add(Attributes.ATTACK_DAMAGE, 3)
                    .add(Attributes.SCALE, 0.8f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.5)
    );
    public static final RegistrySupplier<EntityType<Yousei>> YOUSEI = registerEntityWithSpawnEgg("yousei", 0x597FC4,
            () -> HumanoidModelDimensions.FAIRY.applyTo(EntityType.Builder.of(Yousei::new, MobCategory.MONSTER)),
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
    public static final RegistrySupplier<EntityType<MaidYousei>> MAID_YOUSEI = registerEntityWithSpawnEgg("maid_yousei", 0x59546F,
            () -> HumanoidModelDimensions.FAIRY.applyTo(EntityType.Builder.of(MaidYousei::new, MobCategory.MONSTER)),
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
    public static final RegistrySupplier<EntityType<SunflowerYousei>> SUNFLOWER_YOUSEI = registerEntityWithSpawnEgg("sunflower_yousei", 0xF4DB57,
            () -> HumanoidModelDimensions.FAIRY.applyTo(EntityType.Builder.of((type, world) -> new SunflowerYousei(type, world, MobSkinTypes.SUNFLOWER_YOUSEI), MobCategory.MONSTER)),
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
    public static final RegistrySupplier<EntityType<IceFairy>> ICE_FAIRY = registerEntityWithSpawnEgg("ice_fairy", 0xA3C9F0,
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
    public static final RegistrySupplier<EntityType<Goblin>> GOBLIN = registerEntityWithSpawnEgg("goblin", 0x63964F,
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
    public static final RegistrySupplier<EntityType<RabbitUnit>> RABBIT_UNIT = registerEntityWithSpawnEgg("rabbit_unit", 0x8892B1,
            () -> HumanoidModelDimensions.LUNAR_SOLDIER.applyTo(EntityType.Builder.of(RabbitUnit::new, MobCategory.MONSTER)),
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
    public static final RegistrySupplier<EntityType<WaterElementalEntity>> WATER_ELEMENTAL = registerEntityWithSpawnEgg("water_elemental", 0x2595C9,
            () -> EntityType.Builder.of(WaterElementalEntity::new, MobCategory.MONSTER),
            WaterElementalEntity::createLivingAttributes
    );
    public static final RegistrySupplier<EntityType<FireElementalEntity>> FIRE_ELEMENTAL = registerEntityWithSpawnEgg("fire_elemental", 0xEC7E32,
            () -> EntityType.Builder.of(FireElementalEntity::new, MobCategory.MONSTER),
            FireElementalEntity::createLivingAttributes
    );
    public static final RegistrySupplier<EntityType<IceElementalEntity>> ICE_ELEMENTAL = registerEntityWithSpawnEgg("ice_elemental", 0x52D8DD,
            () -> EntityType.Builder.of(IceElementalEntity::new, MobCategory.MONSTER),
            IceElementalEntity::createLivingAttributes
    );
    public static final RegistrySupplier<EntityType<Hairball>> HAIRBALL = registerEntityWithSpawnEgg("hairball", 0xDEC5F2,
            () -> EntityType.Builder.of(Hairball::new, MobCategory.MONSTER).sized(1, 1),
            Hairball::createAttributes
    );
    public static final RegistrySupplier<EntityType<UFO>> UFO = registerEntityWithSpawnEgg("ufo", 0x55C87F,
            () -> EntityType.Builder.<UFO>of(UFO::new, MobCategory.MONSTER).sized(1.5f, 1.5f).eyeHeight(1.5f * 0.5f),
            cc.thonly.reverie_dreams.entity.UFO::createAttributes
    );
    public static final RegistrySupplier<EntityType<MushroomMonster>> MUSHROOM_MONSTER = registerEntityWithSpawnEgg("mushroom_monster", 0xD3474C,
            () -> EntityType.Builder.of(MushroomMonster::new, MobCategory.MONSTER),
            MushroomMonster::createAttributes
    );
    public static RegistrySupplier<EntityType<WildPig>> WILD_PIG = registerEntityWithSpawnEgg("wild_pig", 0x795238,
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
    public static final RegistrySupplier<EntityType<Wagyu>> WAGYU = registerEntityWithSpawnEgg("wagyu", 0x42302B,
            () -> EntityType.Builder.of(Wagyu::new, MobCategory.CREATURE)
                    .sized(0.9F, 1.4F).eyeHeight(1.3F).clientTrackingRange(10),
            Cow::createAttributes);
    public static final RegistrySupplier<EntityType<HumanEntity>> HUMAN = registerEntityWithSpawnEgg("human", 0x967154,
            () -> EntityType.Builder.of(HumanEntity::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F).eyeHeight(1.62F).clientTrackingRange(10).noLootTable(),
            BaseNPCLikeEntity::createLivingAttributes);
    public static RegistrySupplier<EntityType<TavernVillager>> TAVERN_VILLAGER = registerEntityWithSpawnEgg("tavern_villager",
            () -> EntityType.Builder.<TavernVillager>of(TavernVillager::new, MobCategory.MISC)
                    .sized(0.6f, 1.95f)
                    .eyeHeight(1.62f)
                    .clientTrackingRange(10),
            TavernVillager::createLivingAttributes
    );
    public static RegistrySupplier<EntityType<Oni>> ONI = registerEntityWithSpawnEgg("oni", 0xA9472C,
            () -> EntityType.Builder.of(Oni::new, MobCategory.MONSTER),
            () -> LivingEntity.createLivingAttributes()
                    .add(Attributes.MAX_HEALTH, 25.0)
                    .add(Attributes.FLYING_SPEED, 0.8f)
                    .add(Attributes.MOVEMENT_SPEED, Oni.MOVEMENT_SPEED)
                    .add(Attributes.ATTACK_DAMAGE, 0.5)
                    .add(Attributes.SCALE, 1.85f)
                    .add(Attributes.KNOCKBACK_RESISTANCE, 0.1)
                    .add(Attributes.FOLLOW_RANGE, 16.0)
                    .add(Attributes.TEMPT_RANGE, 10.0)
                    .add(Attributes.ENTITY_INTERACTION_RANGE, 2.2)
    );
    public static final RegistrySupplier<EntityType<WhiteWolfTengu>> WHITE_WOLF_TENGU = registerEntityWithSpawnEgg("white_wolf_tengu", 0xBCBDAE,
            () -> EntityType.Builder.of(WhiteWolfTengu::new, MobCategory.MONSTER),
            () -> BaseNPCLikeEntity.createLivingAttributes().add(Attributes.MAX_HEALTH,24).add(Attributes.MOVEMENT_SPEED,.28)
                    .add(Attributes.ATTACK_DAMAGE,2).add(Attributes.ARMOR,3)
                    .add(Attributes.KNOCKBACK_RESISTANCE,.35).add(Attributes.SCALE,.9));
    public static final RegistrySupplier<EntityType<CrowTengu>> CROW_TENGU = registerEntityWithSpawnEgg("crow_tengu", 0x393B48,
            () -> EntityType.Builder.of(CrowTengu::new, MobCategory.MONSTER),
            () -> BaseNPCLikeEntity.createLivingAttributes().add(Attributes.MAX_HEALTH,20).add(Attributes.MOVEMENT_SPEED,.22)
                    .add(Attributes.FLYING_SPEED,.30).add(Attributes.SCALE,.9));
    public static final RegistrySupplier<EntityType<ThrownCuisineItem>> THROWN_CUISINE_ITEM = registerEntity("thrown_cuisine_item",
            () -> EntityType.Builder.<ThrownCuisineItem>of(ThrownCuisineItem::new, MobCategory.MISC)
                    .noLootTable().sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10),
            null
    );
    public static final RegistrySupplier<EntityType<ThrownExorcismPaper>> THROWN_EXORCISM_PAPER = registerEntity("thrown_exorcism_paper",
            () -> EntityType.Builder.<ThrownExorcismPaper>of(ThrownExorcismPaper::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10),
            null
    );
    public static final RegistrySupplier<EntityType<BaguaFireball>> BAGUA_FIREBALL = registerEntity("bagua_fireball",
            () -> EntityType.Builder.<BaguaFireball>of(BaguaFireball::new, MobCategory.MISC)
                    .noLootTable()
                    .noSave()
                    .noSummon()
                    .fireImmune()
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(10),
            null
    );
    public static final RegistrySupplier<EntityType<ThrownTouhouTrident>> THROWN_NUE_TRIDENT = registerEntity("thrown_nue_trident",
            () -> EntityType.Builder.<ThrownTouhouTrident>of(ThrownTouhouTrident::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20),
            null
    );
    public static final RegistrySupplier<EntityType<ThrownTouhouTrident>> THROWN_GUNGNIR = registerEntity("thrown_gungnir",
            () -> EntityType.Builder.<ThrownTouhouTrident>of(ThrownTouhouTrident::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20),
            null
    );
    public static final RegistrySupplier<EntityType<ThrownIceBlock>> THROWN_ICE_BLOCK = registerEntity("thrown_ice_block",
            () -> EntityType.Builder.<ThrownIceBlock>of(ThrownIceBlock::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.98F, 0.98F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
    );
    public static final RegistrySupplier<EntityType<ThrownIceSnowball>> THROWN_ICE_SNOWBALL = registerEntity("thrown_ice_snowball",
            () -> EntityType.Builder.<ThrownIceSnowball>of(ThrownIceSnowball::new, MobCategory.MISC)
                    .noLootTable()
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(4)
                    .updateInterval(10)
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

    public static final RegistrySupplier<EntityType<CuisineAquaticEntity>> TROUT = registerAquatic(AquaticSpecies.TROUT, 0x7F9147);
    public static final RegistrySupplier<EntityType<CuisineAquaticEntity>> LAMPREY = registerAquatic(AquaticSpecies.LAMPREY, 0x84754D);
    public static final RegistrySupplier<EntityType<CuisineAquaticEntity>> SALMON = registerAquatic(AquaticSpecies.SALMON, 0xE9A088);
    public static final RegistrySupplier<EntityType<CuisineAquaticEntity>> TUNA = registerAquatic(AquaticSpecies.TUNA, 0x48838B);
    public static final RegistrySupplier<EntityType<CuisineAquaticEntity>> SUPREME_TUNA = registerAquatic(AquaticSpecies.SUPREME_TUNA, 0x384F77);
    public static final RegistrySupplier<EntityType<CuisineAquaticEntity>> SHRIMP = registerAquatic(AquaticSpecies.SHRIMP, 0xA0B485);
    public static final RegistrySupplier<EntityType<CuisineAquaticEntity>> CRAB = registerAquatic(AquaticSpecies.CRAB, 0x6D9890);
    public static final RegistrySupplier<EntityType<CuisineAquaticEntity>> OCTOPUS = registerAquatic(AquaticSpecies.OCTOPUS, 0xA57161);
    public static final RegistrySupplier<EntityType<CuisineAquaticEntity>> SEA_URCHIN = registerAquatic(AquaticSpecies.SEA_URCHIN, 0x654978);

    private static RegistrySupplier<EntityType<CuisineAquaticEntity>> registerAquatic(AquaticSpecies species, int eggColor) {
        return registerEntityWithSpawnEgg(species.key(), eggColor,
                () -> EntityType.Builder.<CuisineAquaticEntity>of(
                                (type, level) -> new CuisineAquaticEntity(type, level, species), MobCategory.WATER_AMBIENT)
                        .sized(species.width(), species.height()).clientTrackingRange(6).updateInterval(3),
                () -> CuisineAquaticEntity.createAttributes(species));
    }

    public static void initialize() {
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
        return registerEntityWithSpawnEgg(name, ColoredSpawnEggItem.DEFAULT_COLOR.rgb(), entityType, function);
    }

    public static <T extends Entity> @NotNull RegistrySupplier<EntityType<T>> registerEntityWithSpawnEgg(String name, int eggColor, Supplier<EntityType.Builder<T>> entityType, CreateAttributesBuilderFunction function) {
        RegistrySupplier<EntityType<T>> supplier = MCBuiltInRegistries.ENTITY_TYPE.register(name, () -> entityType.get().build(of(name)));
        String eggName = name + "_spawn_egg";
        ItemDelegate item = registerSpawnEggItem(eggName, supplier, eggColor);
        RegistrySupplier<EntityType<?>> cast = (RegistrySupplier<EntityType<?>>) ((Object) supplier);
        EntityAttributeRegistry.register((Supplier) supplier, function::apply);
        SPAWN_EGG_BIND.put(cast, item);
        return supplier;
    }

    public static <T extends Entity> ItemDelegate registerSpawnEggItem(String eggName, RegistrySupplier<EntityType<T>> supplier) {
        return registerSpawnEggItem(eggName, supplier, ColoredSpawnEggItem.DEFAULT_COLOR.rgb());
    }

    private static <T extends Entity> ItemDelegate registerSpawnEggItem(String eggName, RegistrySupplier<EntityType<T>> supplier, int eggColor) {
        RegistrySupplier<Item> item = MCBuiltInRegistries.ITEM.register(eggName,
                () -> new ColoredSpawnEggItem(eggName, (EntityType<? extends Mob>) supplier.value(), new Item.Properties(), eggColor));
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
