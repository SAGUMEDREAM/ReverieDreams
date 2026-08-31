package cc.thonly.reverie_dreams.api.entity.callback;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.List;

public interface CompatGoalAddedCallback {
    Event<CompatGoalAddedCallback> EVENT = EventFactory.of(
            (listeners) -> (npcLikeEntity) -> {
                List<Tuple<Integer, Goal>> result = new ArrayList<>();
                for (CompatGoalAddedCallback callback : listeners) {
                    result.addAll(callback.handle(npcLikeEntity));
                }
                return result;
            }
    );

    List<Tuple<Integer, Goal>> handle(BaseNPCLikeEntity npcLikeEntity);
}
