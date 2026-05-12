package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.callback.CompatGoalAddedCallback;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCFindBlockGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.util.PlatformContext;
import eu.pb4.polyfactory.block.FactoryBlocks;
import eu.pb4.polyfactory.item.FactoryItems;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

@Slf4j
public class PolyFactoryCompatImpl {
    public static final String WORK_MODE_ID = "polyfactory/hand_crank";

    public static void bootstrap() {
        if (!PlatformContext.hasPolyfactory()) {
            return;
        }
        PlatformContext.FABRIC_POLYFACTORY_HAND_CRANK = FactoryBlocks.HAND_CRANK;
        NPCWorkModes.POLYFACTORY_HAND_CRANK = NPCWorkModes.register(ReverieDreams.id(WORK_MODE_ID), new NPCWorkMode(WORK_MODE_ID, FactoryItems.HAND_CRANK));
        CompatGoalAddedCallback.EVENT.register(PolyFactoryCompatImpl::getGoals);
    }

    public static List<Tuple<Integer, Goal>> getGoals(BaseNPCLikeEntity npcLikeEntity) {
        if (!PlatformContext.hasPolyfactory() || PlatformContext.FABRIC_POLYFACTORY_HAND_CRANK == Blocks.AIR) {
            return List.of();
        }
        return List.of(new Tuple<>(0, new NPCFindBlockGoal(npcLikeEntity, NPCWorkModes.POLYFACTORY_HAND_CRANK, PlatformContext.FABRIC_POLYFACTORY_HAND_CRANK, true, true)));
    }
}
