package cc.thonly.reverie_dreams.entity.ai.goal.work;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.AnimalEntityActionController;
import cc.thonly.reverie_dreams.entity.ai.work.AnimalFeeding;
import cc.thonly.reverie_dreams.entity.ai.work.ScheduledWorkGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Animal;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.entity.npc.work.NPCProduction;

public class NPCBreedGoal extends ScheduledWorkGoal {
    private Animal first;
    private Animal second;
    private int phase;

    private record Region(Object species, int x, int z) {
    }

    public NPCBreedGoal(BaseNPCLikeEntity npc) {
        super(npc, NPCWorkModes.BREED);
    }

    @Override
    protected int searchInterval() {
        return 20;
    }

    private boolean ready(Animal animal) {
        return animal.isAlive() && !animal.isBaby() && animal.getAge() == 0 && !animal.hasCustomName() && animal.canFallInLove();
    }

    @Override
    protected boolean findTask(ServerLevel level) {
        var animals = level.getEntitiesOfClass(Animal.class, site.box(), a -> a.isAlive() && site.contains(a.blockPosition()));
        if (npc instanceof NPCSimpleEntity simple && simple.getSkillTask().offspringObjective()) {
            long pending = animals.stream().filter(a -> a.isInLove() && NPCProduction.ownedParent(a, npc)).count();
            if ((pending + 1) / 2 >= simple.getSkillTask().quantityRemaining()) {
                work().status("waiting_for_offspring");
                return false;
            }
        }
        var candidates = animals.stream().filter(this::ready).filter(a -> !(npc instanceof NPCSimpleEntity simple) || simple.getSkillTask().wantsAnimal(a)).filter(a -> work().available(a.getUUID(), level.getGameTime()))
                .sorted(Comparator.comparingDouble(npc::distanceToSqr)).toList();
        for (var animal : candidates) {
            long count = animals.stream().filter(a -> a.getType() == animal.getType()).count();
            long lovers = animals.stream().filter(a -> a.getType() == animal.getType() && a.isInLove()).count();
            if (!AnimalFeeding.withinLimit((int) count, (int) lovers, ReverieDreams.config().npcBreedMaxAnimals))
                continue;
            var partner = animals.stream().filter(a -> a != animal && a.getClass() == animal.getClass()
                    && !a.isBaby() && a.getAge() == 0 && !a.hasCustomName() && (a.isInLove() || a.canFallInLove())
                    && (!a.isInLove() || !(npc instanceof NPCSimpleEntity simple)
                    || !simple.getSkillTask().active() || NPCProduction.ownedParent(a, npc))
                    && work().available(a.getUUID(), level.getGameTime())).min(Comparator.comparingDouble(animal::distanceToSqr)).orElse(null);
            if (partner == null)
                continue;
            int required = partner.isInLove() ? 1 : 2;
            if (AnimalFeeding.foodCount(npc.getInventory(), animal::isFood) < required + ReverieDreams.config().npcBreedFoodReserve) {
                work().demand(animal::isFood, 32, "need_animals_and_food");
                continue;
            }
            first = animal;
            second = partner;
            phase = 0;
            return true;
        }
        if (work().demand() == null)
            work().status(candidates.isEmpty() ? "waiting_for_animals" : "breed_limit_or_pair");
        return false;
    }

    @Override
    protected List<?> resources() {
        if (first == null || second == null)
            return List.of();
        var keys = new ArrayList<Object>();
        keys.add(first.getUUID());
        keys.add(second.getUUID());
        for (int x = (site.center().getX() - site.radius()) >> 4; x <= (site.center().getX() + site.radius()) >> 4; x++)
            for (int z = (site.center().getZ() - site.radius()) >> 4; z <= (site.center().getZ() + site.radius()) >> 4; z++)
                keys.add(new Region(first.getType(), x, z));
        return keys;
    }

    @Override
    protected boolean validTask(ServerLevel level) {
        return first != null && second != null && first.level() == level && second.level() == level
                && first.isAlive() && second.isAlive() && !first.isBaby() && !second.isBaby()
                && first.getAge() == 0 && second.getAge() == 0 && site.contains(first.blockPosition()) && site.contains(second.blockPosition());
    }

    @Override
    protected void clearTask() {
        first = null;
        second = null;
    }

    @Override
    protected void runTask(ServerLevel level) {
        Animal animal = phase == 0 ? first : second;
        if (animal.isInLove()) {
            if (phase++ == 1)
                finish("ready", 20);
            return;
        }
        if (!ready(animal)) {
            finish("waiting_for_animals", 20);
            return;
        }
        if (!reach(animal, 1.8))
            return;
        var hand = work().tools().ensure(npc.getInventory(), animal::isFood);
        if (hand == null) {
            work().demand(animal::isFood, 32, "need_animals_and_food");
            finish("need_animals_and_food", 20);
            return;
        }
        boolean fed = AnimalFeeding.feed(npc.getInventory(), hand == InteractionHand.MAIN_HAND ? 22 : 23,
                animal::isFood, () -> validTask(level) && ready(animal) && npc.distanceToSqr(animal) <= 4 && belowLimit(level, animal),
                ReverieDreams.config().npcBreedFoodReserve,
                stack -> ((AnimalEntityActionController) animal).reverie_dreams$eatStackFood(npc, hand, stack),
                () -> ((AnimalEntityActionController) animal).reverie_dreams$loveEntity(npc));
        if (!fed) {
            finish("need_animals_and_food", 20);
            return;
        }
        NPCProduction.fed(animal, npc);
        npc.swing(hand);
        work().action();
        if (phase++ == 1)
            finish("ready", 20);
    }

    private boolean belowLimit(ServerLevel level, Animal animal) {
        var sameSpecies = level.getEntitiesOfClass(Animal.class, site.box(), a -> a.isAlive() && a.getType() == animal.getType() && site.contains(a.blockPosition()));
        int lovers = (int) sameSpecies.stream().filter(Animal::isInLove).count();
        // Completing an existing pair does not reserve a second offspring.
        int expected = (lovers + 1) / 2;
        if (phase == 1 && first.isInLove() || phase == 0 && second.isInLove())
            expected = lovers / 2;
        return sameSpecies.size() + expected < ReverieDreams.config().npcBreedMaxAnimals;
    }
}
