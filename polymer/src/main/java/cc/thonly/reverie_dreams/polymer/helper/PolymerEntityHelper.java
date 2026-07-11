package cc.thonly.reverie_dreams.polymer.helper;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.polymer.entity.*;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.polymer.entity.inf.PolymerHolderEntity;
import cc.thonly.reverie_dreams.polymer.entity.inf.TickHolderEntity;
import cc.thonly.reverie_dreams.registry.RegistryImpls;
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
    public static final List<PolymerHolderEntity> HOLD_RENDER_QUEUE = new LinkedList<>();
    public static final Model YOUSEI_WING_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("yousei_wing"));
    public static final Model HAIRBALL_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("hairball"));
    public static final Model BLACK_HAIRBALL_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("black_hairball"));
    public static final Model MUSHROOM_MONSTER_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("mushroom_monster"));
    public static final Model UFO_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("ufo"));
    public static final Model SCARECROW_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("scarecrow"));

    public static void bootstrap() {
        registerOverlay(RDEntityTypes.SUNFLOWER_YOUSEI.value(), SunflowerYouseiImpl::new);
        registerOverlay(RDEntityTypes.YOUSEI.value(), YouseiImpl::new);
        registerOverlay(RDEntityTypes.MAID_YOUSEI.value(), MaidYouseiImpl::new);
        registerOverlay(RDEntityTypes.GHOST.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.GOBLIN.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.WATER_ELEMENTAL.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.FIRE_ELEMENTAL.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.ICE_ELEMENTAL.value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.MAGIC_BROOM.value(), MagicBroomImpl::new);
        registerOverlay(RDEntityTypes.WHEEL_CHAIR.value(), WheelChairImpl::new);
        registerOverlay(RDEntityTypes.MOON_RABBIT.value(), MoonRabbitImpl::new);
        registerOverlay(RDEntityTypes.KILLER_BEE.value(), KillerBeeImpl::new);
        registerOverlay(RDEntityTypes.ORE_ESP.value(), OreEspImpl::new);
        registerOverlay(RDEntityTypes.BAGUA_FURNACE.value(), BaguaFurnaceImpl::new);
        registerOverlay(RDEntityTypes.DANMAKU.value(), DanmakuImpl::new);
        registerOverlay(RDEntityTypes.KNIFE.value(), DanmakuImpl::new);
        registerOverlay(RDEntityTypes.FUMO_SELLER_VILLAGER.value(), VillagerImpl::new);
        registerOverlay(RDEntityTypes.NPC_ROLE.value(), RoleImpl::new);
        registerOverlay(RDEntityTypes.HAIRBALL.value(), HairballImpl::new);
        registerOverlay(RDEntityTypes.MUSHROOM_MONSTER.value(), MushroomMonsterImpl::new);
        registerOverlay(RDEntityTypes.WILD_PIG.value(), WildPigImpl::new);
        registerOverlay(RDEntityTypes.TAVERN_VILLAGER.value(), VillagerImpl::new);
        registerOverlay(RDEntityTypes.SCARECROW.value(), ScarecrowImpl::new);
        registerOverlay(RDEntityTypes.UFO.value(), UfoImpl::new);
        registerOverlay(RDEntityTypes.RABBIT_UNIT.value(), RoleImpl::new);
        registerOverlay(RDEntityTypes.ONI.value(), RoleImpl::new);

        for (NPCRole role : RegistryImpls.NPC_ROLE) {
            registerOverlay(role.getEntityType().value(), npcRoleFastEntity -> context -> EntityType.BLOCK_DISPLAY);
        }

        ServerTickEvents.START_SERVER_TICK.register(PolymerEntityHelper::tickServer);
    }

    private static void tickServer(MinecraftServer server) {
        tickEntityCreate(server);
        tickHolder(server);
    }

    private static void tickEntityCreate(MinecraftServer server) {
        if (HOLD_RENDER_QUEUE.isEmpty()) {
            return;
        }
        Iterator<PolymerHolderEntity> iterator = HOLD_RENDER_QUEUE.iterator();
        while (iterator.hasNext()) {
            PolymerHolderEntity next = iterator.next();
            next.onCreated();
            iterator.remove();
        }
    }

    private static void tickHolder(MinecraftServer server) {
        TickHolderEntity.tick();
    }

    public static void addEntityHolderModel(PolymerHolderEntity polymerHolderEntity) {
        PolymerEntityHelper.HOLD_RENDER_QUEUE.add(polymerHolderEntity);
    }

    public static <T extends Entity> void registerOverlay(EntityType<T> type, Function<T, PolymerEntity> constructor) {
        PolymerEntityUtils.registerOverlay(type, constructor);
        ENTITY_TYPE_FUNCTION_MAP.put(type, constructor);
    }
}
