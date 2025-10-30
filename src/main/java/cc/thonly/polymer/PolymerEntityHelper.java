package cc.thonly.polymer;

import cc.thonly.mystias_izakaya.entity.MIEntities;
import cc.thonly.polymer.entity.*;
import cc.thonly.reverie_dreams.entity.ModEntities;
import cc.thonly.reverie_dreams.entity.npc.NPCRole;
import cc.thonly.reverie_dreams.registry.RegistryManager;
import eu.pb4.polymer.core.api.entity.PolymerEntity;
import eu.pb4.polymer.core.api.entity.PolymerEntityUtils;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import java.util.*;
import java.util.function.Function;

public class PolymerEntityHelper {
    public static final List<PolymerHolderEntity> NEXT = new LinkedList<>();
    public static final Map<EntityType<? extends Entity>, Function<? extends Entity, PolymerEntity>> ENTITY_TYPE_FUNCTION_MAP = new HashMap<>();
    public static final WeakHashMap<Entity, ItemDisplayElement> POLYMER_PLAYER_ELEMENTS = new WeakHashMap<>();

    public static void bootstrap() {
        registerOverlay(ModEntities.SUNFLOWER_YOUSEI_ENTITY_TYPE, SunflowerYouseiImpl::new);
        registerOverlay(ModEntities.YOUSEI_ENTITY_TYPE, YouseiImpl::new);
        registerOverlay(ModEntities.GHOST_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(ModEntities.GOBLIN_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(ModEntities.WATER_ELEMENTAL_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(ModEntities.FIRE_ELEMENTAL_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(ModEntities.ICE_ELEMENTAL_ENTITY_TYPE, NPCImpl::new);
        registerOverlay(ModEntities.BROOM_ENTITY_TYPE, MagicBroomImpl::new);
        registerOverlay(ModEntities.WHEEL_CHAIR_ENTITY, WheelChairImpl::new);
        registerOverlay(ModEntities.MOON_RABBIT_ENTITY_TYPE, MoonRabbitImpl::new);
        registerOverlay(ModEntities.KILLER_BEE_ENTITY_TYPE, KillerBeeImpl::new);
        registerOverlay(ModEntities.ORE_ESP_ENTITY_TYPE, OreEspImpl::new);
        registerOverlay(ModEntities.BAGUA_FURNACE_ENTITY, BaguaFurnaceImpl::new);
        registerOverlay(ModEntities.DANMAKU_ENTITY_TYPE, DanmakuImpl::new);
        registerOverlay(ModEntities.KNIFE_ENTITY_TYPE, DanmakuImpl::new);
        registerOverlay(ModEntities.FUMO_SELLER_VILLAGER, VillagerImpl::new);
        registerOverlay(ModEntities.NPC_ROLE_ENTITY, RoleImpl::new);
        registerOverlay(MIEntities.WILD_PIG, WildPigImpl::new);
        registerOverlay(MIEntities.TAVERN_VILLAGER, VillagerImpl::new);

        for (NPCRole role : RegistryManager.NPC_ROLE) {
            registerOverlay(role.getEntityType(), npcRoleFastEntity -> context -> EntityType.BLOCK_DISPLAY);
        }

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<PolymerHolderEntity> iterator = NEXT.iterator();
            while (iterator.hasNext()) {
                PolymerHolderEntity next = iterator.next();
                next.onCreated();
                iterator.remove();
            }
        });
    }

    public static <T extends Entity> void registerOverlay(EntityType<T> type, Function<T, PolymerEntity> constructor) {
        PolymerEntityUtils.registerOverlay(type, constructor);
        ENTITY_TYPE_FUNCTION_MAP.put(type, constructor);
    }
}
