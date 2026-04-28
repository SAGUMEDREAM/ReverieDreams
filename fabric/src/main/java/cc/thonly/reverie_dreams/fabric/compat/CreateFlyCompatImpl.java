package cc.thonly.reverie_dreams.fabric.compat;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.api.entity.CompatGoalAddedCallback;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCFindBlockGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.util.PlatformContext;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

@Slf4j
public class CreateFlyCompatImpl {
    public static final String WORK_MODE_ID = "create-fly/hand_crank";

    public static void bootstrap() {
        if (!PlatformContext.hasCreateFly()) {
            return;
        }
        PlatformContext.FABRIC_CREATE_FLY_HAND_CRANK = AllBlocks.HAND_CRANK;
        NPCWorkModes.CREATE_FLY_HAND_CRANK = NPCWorkModes.register(ReverieDreams.id(WORK_MODE_ID), new NPCWorkMode(WORK_MODE_ID, AllItems.HAND_CRANK));
        CompatGoalAddedCallback.EVENT.register(CreateFlyCompatImpl::getGoals);
    }

    public static List<Tuple<Integer, Goal>> getGoals(BaseNPCLikeEntity npcLikeEntity) {
        if (!PlatformContext.hasCreateFly() || PlatformContext.FABRIC_CREATE_FLY_HAND_CRANK == Blocks.AIR) {
            return List.of();
        }
//        System.out.println(NPCWorkModes.CREATE_FLY_HAND_CRANK);
        return List.of(new Tuple<>(0, new NPCFindBlockGoal(npcLikeEntity, NPCWorkModes.CREATE_FLY_HAND_CRANK, PlatformContext.FABRIC_CREATE_FLY_HAND_CRANK, true, true)));
    }
}
