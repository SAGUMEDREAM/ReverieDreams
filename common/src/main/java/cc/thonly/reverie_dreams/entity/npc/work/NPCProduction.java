package cc.thonly.reverie_dreams.entity.npc.work;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.server.level.ServerLevel;

/**
 * Credits originate in synchronous work or successful entity-spawn events, never inventory snapshots.
 */
public final class NPCProduction {
    public record Source(UUID npc, UUID task) {
    }

    public interface AnimalSource {
        Source reverie_dreams$productionSource();

        void reverie_dreams$productionSource(Source source);
    }

    public interface FurnaceSource {
        NPCFurnaceBatch reverie_dreams$productionBatch();
    }

    private record Capture(BaseNPCLikeEntity npc, String skill) {
    }

    private static final ThreadLocal<Capture> CAPTURE = new ThreadLocal<>();
    private static final Map<Entity, Source> BIRTHS = new WeakHashMap<>();

    private NPCProduction() {
    }

    public static void capture(BaseNPCLikeEntity npc, String skill, Runnable action) {
        Capture previous = CAPTURE.get();
        CAPTURE.set(new Capture(npc, skill));
        try {
            action.run();
        } finally {
            if (previous == null)
                CAPTURE.remove();
            else
                CAPTURE.set(previous);
        }
    }

    public static void item(BaseNPCLikeEntity npc, String skill, ItemStack stack) {
        if (npc instanceof NPCSimpleEntity simple && !stack.isEmpty()) {
            simple.getSkillTask().recordProduction(simple, simple.getSkillTask().id(), skill,
                    BuiltInRegistries.ITEM.getKey(stack.getItem()).toString(), stack.getCount(), false);
        }
    }

    public static Source source(BaseNPCLikeEntity npc) {
        return npc instanceof NPCSimpleEntity simple && simple.getSkillTask().active() && simple.getSkillTask().id() != null
                ? new Source(npc.getUUID(), simple.getSkillTask().id()) : null;
    }

    public static boolean ownedParent(Animal animal, BaseNPCLikeEntity npc) {
        return animal instanceof AnimalSource marker && source(npc) != null && source(npc).equals(marker.reverie_dreams$productionSource());
    }

    public static void fed(Animal animal, BaseNPCLikeEntity npc) {
        if (animal instanceof AnimalSource marker)
            marker.reverie_dreams$productionSource(source(npc));
    }

    public static void breeding(Animal first, Animal second, AgeableMob offspring) {
        if (offspring == null || !(first instanceof AnimalSource a) || !(second instanceof AnimalSource b))
            return;
        Source source = a.reverie_dreams$productionSource();
        if (source != null && source.equals(b.reverie_dreams$productionSource()))
            BIRTHS.put(offspring, source);
        a.reverie_dreams$productionSource(null);
        b.reverie_dreams$productionSource(null);
    }

    public static void spawned(Entity entity) {
        Capture capture = CAPTURE.get();
        if (capture != null && entity instanceof ItemEntity item && entity.level() == capture.npc.level()) {
            item(capture.npc, capture.skill, item.getItem());
        }
        Source source = BIRTHS.remove(entity);
        if (source != null && entity.level() instanceof ServerLevel level
                && level.getEntity(source.npc) instanceof NPCSimpleEntity npc) {
            npc.getSkillTask().recordProduction(npc, source.task, "breed_animals",
                    BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString(), 1, true);
        }
    }
}
