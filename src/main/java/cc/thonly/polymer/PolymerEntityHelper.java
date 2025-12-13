package cc.thonly.polymer;

import cc.thonly.polymer.entity.*;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.util.entity.ModelUtil;
import de.tomalbrc.bil.core.model.Model;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
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
    public static final Model MUSHROOM_MONSTER_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("mushroom_monster"));
    public static final Model UFO_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("ufo"));
    public static final Model SCARECROW_MODEL = ModelUtil.loadBBModel(ReverieDreams.id("scarecrow"));

    public static void bootstrap() {
        registerOverlay(RDEntityTypes.SUNFLOWER_YOUSEI, SunflowerYouseiImpl::new);
        registerOverlay(RDEntityTypes.YOUSEI, YouseiImpl::new);
        registerOverlay(RDEntityTypes.MAID_YOUSEI, MaidYouseiImpl::new);
        registerOverlay(RDEntityTypes.GHOST, NPCImpl::new);
        registerOverlay(RDEntityTypes.GOBLIN, NPCImpl::new);
        registerOverlay(RDEntityTypes.WATER_ELEMENTAL, NPCImpl::new);
        registerOverlay(RDEntityTypes.FIRE_ELEMENTAL, NPCImpl::new);
        registerOverlay(RDEntityTypes.ICE_ELEMENTAL, NPCImpl::new);
        registerOverlay(RDEntityTypes.MAGIC_BROOM, MagicBroomImpl::new);
        registerOverlay(RDEntityTypes.WHEEL_CHAIR, WheelChairImpl::new);
        registerOverlay(RDEntityTypes.MOON_RABBIT, MoonRabbitImpl::new);
        registerOverlay(RDEntityTypes.KILLER_BEE, KillerBeeImpl::new);
        registerOverlay(RDEntityTypes.ORE_ESP, OreEspImpl::new);
        registerOverlay(RDEntityTypes.BAGUA_FURNACE, BaguaFurnaceImpl::new);
        registerOverlay(RDEntityTypes.DANMAKU, DanmakuImpl::new);
        registerOverlay(RDEntityTypes.KNIFE, DanmakuImpl::new);
        registerOverlay(RDEntityTypes.FUMO_SELLER_VILLAGER, VillagerImpl::new);
        registerOverlay(RDEntityTypes.NPC_ROLE, RoleImpl::new);
        registerOverlay(RDEntityTypes.HAIRBALL, HairballImpl::new);
        registerOverlay(RDEntityTypes.MUSHROOM_MONSTER, MushroomMonsterImpl::new);
        registerOverlay(RDEntityTypes.WILD_PIG, WildPigImpl::new);
        registerOverlay(RDEntityTypes.TAVERN_VILLAGER, VillagerImpl::new);
        registerOverlay(RDEntityTypes.SCARECROW, ScarecrowImpl::new);
        registerOverlay(RDEntityTypes.UFO, UfoImpl::new);

        for (NPCRole role : RegistryHandlers.NPC_ROLE) {
            registerOverlay(role.getEntityType(), npcRoleFastEntity -> context -> EntityType.BLOCK_DISPLAY);
        }

        ServerTickEvents.START_SERVER_TICK.register(server -> {
            if (HOLD_RENDER_QUEUE.isEmpty()) {
                return;
            }
            Iterator<PolymerHolderEntity> iterator = HOLD_RENDER_QUEUE.iterator();
            while (iterator.hasNext()) {
                PolymerHolderEntity next = iterator.next();
                next.onCreated();
                iterator.remove();
            }

        });
        ServerTickEvents.START_SERVER_TICK.register(server -> {
            TickHolderEntity.tick();
        });
    }

    public static void addEntityHolderModel(PolymerHolderEntity polymerHolderEntity) {
        PolymerEntityHelper.HOLD_RENDER_QUEUE.add(polymerHolderEntity);
    }

    public static <T extends Entity> void registerOverlay(EntityType<T> type, Function<T, PolymerEntity> constructor) {
        PolymerEntityUtils.registerOverlay(type, constructor);
        ENTITY_TYPE_FUNCTION_MAP.put(type, constructor);
    }
}
