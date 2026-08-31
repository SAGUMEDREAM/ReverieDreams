package cc.thonly.reverie_dreams.api.entity.entry;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.function.Function;

public record RoleGoalEntry(Integer prio, Function<BaseNPCLikeEntity, Goal> function) {
}
