package cc.thonly.reverie_dreams.api.entity.callback;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import net.blay09.mods.balm.platform.event.Event;
import net.blay09.mods.balm.platform.event.EventFactory;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.ArrayList;
import java.util.List;

public interface CompatGoalAddedCallback {
    Event<CompatGoalAddedCallback> EVENT = EventFactory.createArrayBacked(
            CompatGoalAddedCallback.class,
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
