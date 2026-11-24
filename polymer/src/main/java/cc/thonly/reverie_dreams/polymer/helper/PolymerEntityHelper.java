package cc.thonly.reverie_dreams.polymer.helper;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.aquatic.AquaticSpecies;
import cc.thonly.reverie_dreams.registry.content.entity.RDAquaticEntities;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairySkin;
import cc.thonly.reverie_dreams.entity.variant.GensokyoFairyAppearance;
import cc.thonly.reverie_dreams.entity.variant.OniAppearance;
import cc.thonly.reverie_dreams.entity.variant.TenguAppearance;
import cc.thonly.reverie_dreams.entity.variant.RabbitUnitAppearance;
import cc.thonly.reverie_dreams.entity.variant.YoukaiRabbitAppearance;
import cc.thonly.reverie_dreams.polymer.entity.*;
import cc.thonly.reverie_dreams.data.npc.NPCRoleType;
import cc.thonly.reverie_dreams.polymer.entity.bridge.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.bridge.TickHolderEntity;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.fabric.util.ModelUtil;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.*;
import java.util.function.Function;

public class PolymerEntityHelper {
    public static final Map<EntityType<? extends Entity>, Function<? extends Entity, PolymerEntity>> ENTITY_TYPE_FUNCTION_MAP = new HashMap<>();
    public static final WeakHashMap<Entity, ItemDisplayElement> POLYMER_PLAYER_ELEMENTS = new WeakHashMap<>();
    private static final ServerEntityHolderQueue HOLD_RENDER_QUEUE = new ServerEntityHolderQueue();
    public static final Model YOUSEI_WING_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("yousei_wing"));
    public static final Model HAIRBALL_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("hairball"));
    public static final Model BLACK_HAIRBALL_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("black_hairball"));
    private static final Map<YoukaiRabbitAppearance, Model> YOUKAI_RABBIT_MODELS = createYoukaiRabbitModels();
    public static final Model MUSHROOM_MONSTER_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("mushroom_monster"));
    public static final Model UFO_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("ufo"));
    public static final Model SCARECROW_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("scarecrow"));
    public static final Model GENSOKYO_FAIRY_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("gensokyo_fairy"));
    public static final Model BLUE_YOUSEI_MODEL = ModelUtil.loadBBModel(GensokyoFairySkin.FORMAL_BLUE_MODEL_ID);
    private static final Map<GensokyoFairySkin, Model> FAIRY_MODELS = createFairyModels();
    private static final Map<GensokyoFairyAppearance, Model> FAIRY_APPEARANCES = createFairyAppearances();
    private static final Map<OniAppearance, Model> ONI_MODELS = createOniModels();
    private static final Map<RabbitUnitAppearance, Model> RABBIT_UNIT_MODELS = createRabbitUnitModels();

    private static Map<YoukaiRabbitAppearance, Model> createYoukaiRabbitModels() {
        Map<YoukaiRabbitAppearance, Model> models = new EnumMap<>(YoukaiRabbitAppearance.class);
        for (var appearance : YoukaiRabbitAppearance.values()) models.put(appearance, ModelUtil.loadBBModel(appearance.resourceId()));
        return models;
    }

    public static Model getYoukaiRabbitModel(YoukaiRabbitAppearance appearance) { return YOUKAI_RABBIT_MODELS.get(appearance); }

    private static Map<RabbitUnitAppearance, Model> createRabbitUnitModels() {
        Map<RabbitUnitAppearance, Model> models = new EnumMap<>(RabbitUnitAppearance.class);
        for (var appearance : RabbitUnitAppearance.values()) models.put(appearance, ModelUtil.loadBBModel(appearance.modelId()));
        return models;
    }

    public static Model getRabbitUnitModel(RabbitUnitAppearance appearance) { return RABBIT_UNIT_MODELS.get(appearance); }

    private static Map<OniAppearance, Model> createOniModels() {
        Map<OniAppearance, Model> models = new EnumMap<>(OniAppearance.class);
        for (var appearance : OniAppearance.values()) models.put(appearance, ModelUtil.loadBBModel(appearance.modelId()));
        return models;
    }

    public static Model getOniModel(OniAppearance appearance) { return ONI_MODELS.get(appearance); }
    private static final Map<TenguAppearance, Model> TENGU_MODELS = createTenguModels();
    private static Map<TenguAppearance, Model> createTenguModels() {
        Map<TenguAppearance, Model> models = new EnumMap<>(TenguAppearance.class);
        for (var appearance : TenguAppearance.values()) models.put(appearance, ModelUtil.loadBBModel(appearance.modelId()));
        return models;
    }
    public static Model getTenguModel(TenguAppearance appearance) { return TENGU_MODELS.get(appearance); }

    private static Map<GensokyoFairyAppearance, Model> createFairyAppearances() {
        Map<GensokyoFairyAppearance, Model> models = new EnumMap<>(GensokyoFairyAppearance.class);
        for (var appearance : GensokyoFairyAppearance.values()) {
            models.put(appearance, appearance == GensokyoFairyAppearance.YOUSEI_BLUE
                    ? BLUE_YOUSEI_MODEL : ModelUtil.loadBBModel(appearance.modelId()));
        }
        return models;
    }

    public static Model getFairyAppearance(GensokyoFairyAppearance appearance) {
        return FAIRY_APPEARANCES.get(appearance);
    }

    private static Map<GensokyoFairySkin, Model> createFairyModels() {
        Map<GensokyoFairySkin, Model> models = new EnumMap<>(GensokyoFairySkin.class);
        for (GensokyoFairySkin skin : GensokyoFairySkin.values()) {
            models.put(skin, skin == GensokyoFairySkin.BLUE
                    ? GENSOKYO_FAIRY_MODEL : ModelUtil.loadBBModel(skin.modelId()));
        }
        return models;
    }

    public static Model getFairyModel(GensokyoFairySkin skin) {
        return FAIRY_MODELS.get(skin);
    }

    public static void bootstrap() {
        CuisineAquaticImpl.initialize();
        for (var species : AquaticSpecies.values()) {
            registerOverlay(RDAquaticEntities.type(species).value(), CuisineAquaticImpl::new);
        }
        registerOverlay(RDEntityTypes.CUSHION_SEAT.value(), seat -> context -> EntityType.BLOCK_DISPLAY);
        registerOverlay(RDEntityTypes.SUNFLOWER_YOUSEI.value(), SunflowerYouseiImpl::new);
        registerOverlay(RDEntityTypes.YOUSEI.value(), YouseiImpl::new);
        registerOverlay(RDEntityTypes.MAID_YOUSEI.value(), MaidYouseiImpl::new);
        registerOverlay(RDEntityTypes.ICE_FAIRY.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.GHOST.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.GOBLIN.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.WATER_ELEMENTAL.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.FIRE_ELEMENTAL.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.ICE_ELEMENTAL.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.MAGIC_BROOM.value(), MagicBroomImpl::new);
        registerOverlay(RDEntityTypes.WHEEL_CHAIR.value(), WheelChairImpl::new);
        registerOverlay(RDEntityTypes.YOUKAI_RABBIT.value(), YoukaiRabbitImpl::new);
        registerOverlay(RDEntityTypes.LEGACY_MOON_RABBIT.value(), YoukaiRabbitImpl::new);
        registerOverlay(RDEntityTypes.KILLER_BEE.value(), KillerBeeImpl::new);
        registerOverlay(RDEntityTypes.ORE_ESP.value(), OreEspImpl::new);
        registerOverlay(RDEntityTypes.GAP_PORTAL.value(), GapPortalImpl::new);
        registerOverlay(RDEntityTypes.CURSED_DECOY.value(), CursedDecoyImpl::new);
        registerOverlay(RDEntityTypes.BAGUA_FIREBALL.value(), fireball -> context -> EntityType.SMALL_FIREBALL);
        registerOverlay(RDEntityTypes.BAGUA_FURNACE.value(), BaguaFurnaceImpl::new);
        registerOverlay(RDEntityTypes.DANMAKU.value(), DanmakuImpl::new);
        registerOverlay(RDEntityTypes.DANMAKU_FAMILIAR.value(), FamiliarImpl::new);
        registerOverlay(RDEntityTypes.DANMAKU_LASER.value(), LaserImpl::new);
        registerOverlay(RDEntityTypes.THROWN_EXORCISM_PAPER.value(), ThrownItemProjectileImpl::new);
        registerOverlay(RDEntityTypes.THROWN_CUISINE_ITEM.value(), ThrownItemProjectileImpl::new);
        registerOverlay(RDEntityTypes.THROWN_ICE_BLOCK.value(), ThrownItemProjectileImpl::new);
        registerOverlay(RDEntityTypes.THROWN_ICE_SNOWBALL.value(), ThrownItemProjectileImpl::new);
        registerOverlay(RDEntityTypes.THROWN_NUE_TRIDENT.value(), trident -> context -> EntityType.TRIDENT);
        registerOverlay(RDEntityTypes.THROWN_GUNGNIR.value(), trident -> context -> EntityType.TRIDENT);
        registerOverlay(RDEntityTypes.KNIFE.value(), DanmakuImpl::new);
        registerOverlay(RDEntityTypes.FUMO_SELLER_VILLAGER.value(), VillagerImpl::new);
        registerOverlay(RDEntityTypes.NPC_SIMPLE_ENTITY.value(), RoleImpl::new);
        registerOverlay(RDEntityTypes.HAIRBALL.value(), HairballImpl::new);
        registerOverlay(RDEntityTypes.MUSHROOM_MONSTER.value(), MushroomMonsterImpl::new);
        registerOverlay(RDEntityTypes.WILD_PIG.value(), WildPigImpl::new);
        registerOverlay(RDEntityTypes.WAGYU.value(), wagyu -> context -> EntityType.COW);
        registerOverlay(RDEntityTypes.HUMAN.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.TAVERN_VILLAGER.value(), VillagerImpl::new);
        registerOverlay(RDEntityTypes.SCARECROW.value(), ScarecrowImpl::new);
        registerOverlay(RDEntityTypes.UFO.value(), UfoImpl::new);
        registerOverlay(RDEntityTypes.RABBIT_UNIT.value(), RabbitUnitImpl::new);
        registerOverlay(RDEntityTypes.ONI.value(), OniImpl::new);
        registerOverlay(RDEntityTypes.WHITE_WOLF_TENGU.value(), TenguImpl::new);
        registerOverlay(RDEntityTypes.CROW_TENGU.value(), TenguImpl::new);
        registerOverlay(RDEntityTypes.FISHING_BOBBER.value(), npcFishingHook -> (PolymerEntity) context -> EntityType.BLOCK_DISPLAY);

        for (NPCRoleType role : BuiltInRegistryProviders.NPC_ROLE_TYPE) {
            registerOverlay(role.getEntityType().value(), npcRoleFastEntity -> context -> EntityType.BLOCK_DISPLAY);
        }

        ServerTickEvents.START_SERVER_TICK.register(PolymerEntityHelper::tickServer);
    }

    private static void tickServer(MinecraftServer server) {
        tickEntityCreate(server);
        tickHolder(server);
    }

    private static void tickEntityCreate(MinecraftServer server) {
        HOLD_RENDER_QUEUE.drain(server);
    }

    private static void tickHolder(MinecraftServer server) {
        TickHolderEntity.tick(server);
    }

    public static void addEntityHolderModel(PolymerHolderEntity polymerHolderEntity, Entity source) {
        HOLD_RENDER_QUEUE.add(polymerHolderEntity, source);
    }

    public static <T extends Entity> void registerOverlay(EntityType<T> type, Function<T, PolymerEntity> constructor) {
        PolymerEntityUtils.registerOverlay(type, constructor);
        ENTITY_TYPE_FUNCTION_MAP.put(type, constructor);
    }
}
