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
        registerOverlay(RDEntityTypes.SUNFLOWER_YOUSEI.asHolder().value(), SunflowerYouseiImpl::new);
        registerOverlay(RDEntityTypes.YOUSEI.asHolder().value(), YouseiImpl::new);
        registerOverlay(RDEntityTypes.MAID_YOUSEI.asHolder().value(), MaidYouseiImpl::new);
        registerOverlay(RDEntityTypes.GHOST.asHolder().value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.GOBLIN.asHolder().value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.WATER_ELEMENTAL.asHolder().value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.FIRE_ELEMENTAL.asHolder().value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.ICE_ELEMENTAL.asHolder().value(), NPCImpl::new);
        registerOverlay(RDEntityTypes.MAGIC_BROOM.asHolder().value(), MagicBroomImpl::new);
        registerOverlay(RDEntityTypes.WHEEL_CHAIR.asHolder().value(), WheelChairImpl::new);
        registerOverlay(RDEntityTypes.MOON_RABBIT.asHolder().value(), MoonRabbitImpl::new);
        registerOverlay(RDEntityTypes.KILLER_BEE.asHolder().value(), KillerBeeImpl::new);
        registerOverlay(RDEntityTypes.ORE_ESP.asHolder().value(), OreEspImpl::new);
        registerOverlay(RDEntityTypes.BAGUA_FURNACE.asHolder().value(), BaguaFurnaceImpl::new);
        registerOverlay(RDEntityTypes.DANMAKU.asHolder().value(), DanmakuImpl::new);
        registerOverlay(RDEntityTypes.KNIFE.asHolder().value(), DanmakuImpl::new);
        registerOverlay(RDEntityTypes.FUMO_SELLER_VILLAGER.asHolder().value(), VillagerImpl::new);
        registerOverlay(RDEntityTypes.NPC_ROLE.asHolder().value(), RoleImpl::new);
        registerOverlay(RDEntityTypes.HAIRBALL.asHolder().value(), HairballImpl::new);
        registerOverlay(RDEntityTypes.MUSHROOM_MONSTER.asHolder().value(), MushroomMonsterImpl::new);
        registerOverlay(RDEntityTypes.WILD_PIG.asHolder().value(), WildPigImpl::new);
        registerOverlay(RDEntityTypes.TAVERN_VILLAGER.asHolder().value(), VillagerImpl::new);
        registerOverlay(RDEntityTypes.SCARECROW.asHolder().value(), ScarecrowImpl::new);
        registerOverlay(RDEntityTypes.UFO.asHolder().value(), UfoImpl::new);
        registerOverlay(RDEntityTypes.RABBIT_UNIT.asHolder().value(), RoleImpl::new);

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
