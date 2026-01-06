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
}
