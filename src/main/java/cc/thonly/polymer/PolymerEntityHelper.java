package cc.thonly.polymer;

import cc.thonly.polymer.entity.*;
import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.registry.content.entity.RDEntityTypes;
import cc.thonly.reverie_dreams.data.npc.NPCRole;
import cc.thonly.reverie_dreams.registry.RegistryHandlers;
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
    public static final Model HAIRBALL_MODEL = ModelUtil.loadModel(ReverieDreams.id("hairball"));
    public static final Model MUSHROOM_MONSTER_MODEL = ModelUtil.loadModel(ReverieDreams.id("mushroom_monster"));

    public static void bootstrap() {
        registerOverlay(RDEntityTypes.SUNFLOWER_YOUSEI_ENTITY_TYPE, SunflowerYouseiImpl::new);
        registerOverlay(RDEntityTypes.YOUSEI_ENTITY_TYPE, YouseiImpl::new);
        registerOverlay(RDEntityTypes.MAID_YOUSEI_ENTITY_TYPE, MaidYouseiImpl::new);
        registerOverlay(RDEntityTypes.GHOST_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(RDEntityTypes.GOBLIN_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(RDEntityTypes.WATER_ELEMENTAL_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(RDEntityTypes.FIRE_ELEMENTAL_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(RDEntityTypes.ICE_ELEMENTAL_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(RDEntityTypes.BROOM_ENTITY_TYPE, MagicBroomImpl::new);
        registerOverlay(RDEntityTypes.WHEEL_CHAIR_ENTITY, WheelChairImpl::new);
        registerOverlay(RDEntityTypes.MOON_RABBIT_ENTITY_TYPE, MoonRabbitImpl::new);
        registerOverlay(RDEntityTypes.KILLER_BEE_ENTITY_TYPE, KillerBeeImpl::new);
        registerOverlay(RDEntityTypes.ORE_ESP_ENTITY_TYPE, OreEspImpl::new);
        registerOverlay(RDEntityTypes.BAGUA_FURNACE_ENTITY, BaguaFurnaceImpl::new);
        registerOverlay(RDEntityTypes.DANMAKU_ENTITY_TYPE, DanmakuImpl::new);
        registerOverlay(RDEntityTypes.KNIFE_ENTITY_TYPE, DanmakuImpl::new);
        registerOverlay(RDEntityTypes.FUMO_SELLER_VILLAGER, VillagerImpl::new);
        registerOverlay(RDEntityTypes.NPC_ROLE_ENTITY, RoleImpl::new);
        registerOverlay(RDEntityTypes.HAIRBALL_ENTITY_TYPE, HairballImpl::new);
        registerOverlay(RDEntityTypes.MUSHROOM_MONSTER_ENTITY_TYPE, MushroomMonsterImpl::new);
        registerOverlay(RDEntityTypes.WILD_PIG, WildPigImpl::new);
        registerOverlay(RDEntityTypes.TAVERN_VILLAGER, VillagerImpl::new);

        for (NPCRole role : RegistryHandlers.NPC_ROLE) {
            registerOverlay(role.getEntityType(), npcRoleFastEntity -> context -> EntityType.BLOCK_DISPLAY);
        }

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<PolymerHolderEntity> iterator = HOLD_RENDER_QUEUE.iterator();
            while (iterator.hasNext()) {
                PolymerHolderEntity next = iterator.next();
                next.onCreated();
                iterator.remove();
            }
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
