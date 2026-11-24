package cc.thonly.reverie_dreams.ai.npc;

import cc.thonly.reverie_dreams.entity.npc.work.NPCWorkFeedback;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.ai.ChatProvider;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.entity.ai.work.AnimalFeeding;
import cc.thonly.reverie_dreams.entity.ai.work.WorkArea;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.sheep.Sheep;

/**
 * Advisory inspection at one loaded site, never an autonomous order or a production guarantee.
 */
public final class NPCWorkSuggestions {
    public record Suggestion(String skill, String reason, boolean blocked, boolean preferred) {
    }

    private NPCWorkSuggestions() {
    }

    public static List<Suggestion> atCurrentSite(NPCSimpleEntity npc) {
        var behavior = NPCPersonas.behavior(npc.getRoleType().identify());
        return NPCBehavior.WORK_SKILLS.stream().map(skill -> {
            var report = NPCWorkFeedback.inspect(npc, skill, npc.blockPosition());
            if (!report.blocked() && npc.level() instanceof ServerLevel level
                    && (skill.equals("breed_animals") || skill.equals("shear_sheep"))) {
                var config = ReverieDreams.config();
                var area = new WorkArea(npc.blockPosition(), config.npcWorkRange, config.npcWorkVerticalRange);
                if (skill.equals("shear_sheep") && level.getEntitiesOfClass(Sheep.class, area.box(),
                        sheep -> sheep.isAlive() && sheep.readyForShearing() && !sheep.hasCustomName() && area.contains(sheep.blockPosition())).isEmpty())
                    report = new NPCWorkFeedback.Report("waiting_for_sheep", true);
                if (skill.equals("breed_animals")) {
                    var adults = level.getEntitiesOfClass(Animal.class, area.box(), animal -> animal.isAlive()
                            && !animal.isBaby() && animal.getAge() == 0 && animal.canFallInLove() && !animal.hasCustomName() && area.contains(animal.blockPosition()));
                    var representatives = new HashMap<EntityType<?>, Animal>();
                    var pairs = new HashSet<EntityType<?>>();
                    for (var animal : adults)
                        if (representatives.putIfAbsent(animal.getType(), animal) != null)
                            pairs.add(animal.getType());
                    if (pairs.isEmpty())
                        report = new NPCWorkFeedback.Report("waiting_for_animals", true);
                    else if (pairs.stream().noneMatch(type -> AnimalFeeding.foodCount(npc.getInventory(), representatives.get(type)::isFood)
                            >= 2 + config.npcBreedFoodReserve))
                        report = new NPCWorkFeedback.Report("need_animals_and_food", true);
                }
            }
            return new Suggestion(skill, report.code(), report.blocked(), behavior.workPreferences().contains(skill));
        }).sorted(Comparator.comparing(Suggestion::blocked)
                .thenComparingInt(s -> behavior.preference(s.skill()))).toList();
    }

    public static JsonObject context(NPCSimpleEntity npc) {
        var data = new JsonObject();
        data.add("behavior", NPCPersonas.behavior(npc.getRoleType().identify()).context());
        data.addProperty("x", npc.blockPosition().getX());
        data.addProperty("y", npc.blockPosition().getY());
        data.addProperty("z", npc.blockPosition().getZ());
        data.add("suggestions", ChatProvider.GSON.toJsonTree(atCurrentSite(npc)));
        data.addProperty("notice", "Site diagnostics only, not completed work. Unblocked suggestions precede preferences. Recheck supplies and owner instructions before start_skill; never silently change an active plan.");
        return data;
    }

    /**
     * Called only by an explicit owner click. The captured menu cannot replace newer work.
     */
    public static String startFromPanel(NPCSimpleEntity npc, ServerPlayer player, UUID expectedTask,
                                        Identifier expectedRole, Identifier expectedDimension,
                                        BlockPos expectedSite, String skill) {
        if (!npc.isAlive() || npc.isRemoved() || !player.isAlive() || player.hasDisconnected()
                || !npc.isOwnedBy(player) || player.level() != npc.level() || npc.distanceToSqr(player) > 1024
                || !Objects.equals(expectedTask, npc.getSkillTask().id()) || npc.getSkillTask().active()
                || !npc.level().dimension().identifier().equals(expectedDimension)
                || !Objects.equals(expectedRole, npc.getRoleType().identify()) || !npc.blockPosition().equals(expectedSite)
                || !NPCBehavior.WORK_SKILLS.contains(skill))
            return "Denied: owner, task, role or site changed; refresh the panel.";
        var report = atCurrentSite(npc).stream().filter(s -> s.skill().equals(skill)).findFirst().orElseThrow();
        if (report.blocked())
            return "Failed: " + report.reason();
        var args = new JsonObject();
        args.addProperty("skill", skill);
        args.addProperty("x", npc.getX());
        args.addProperty("y", npc.getY());
        args.addProperty("z", npc.getZ());
        npc.cancelChatRequests();
        return npc.getSkillTask().startWithoutModel(npc, player, args);
    }
}
