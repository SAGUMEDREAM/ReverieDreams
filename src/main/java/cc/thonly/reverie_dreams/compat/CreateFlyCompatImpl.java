package cc.thonly.reverie_dreams.compat;

import cc.thonly.reverie_dreams.ReverieDreams;
import cc.thonly.reverie_dreams.data.npc.NPCWorkMode;
import cc.thonly.reverie_dreams.entity.ai.goal.work.NPCFindBlockGoal;
import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.registry.content.NPCWorkModes;
import cc.thonly.reverie_dreams.util.ConstantInfo;
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
        if (!ConstantInfo.hasCreateFly()) {
            return;
        }
        ConstantInfo.CREATE_FLY_HAND_CRANK = AllBlocks.HAND_CRANK;
        NPCWorkModes.CREATE_FLY_HAND_CRANK = NPCWorkModes.register(ReverieDreams.id(WORK_MODE_ID), new NPCWorkMode(WORK_MODE_ID, AllItems.HAND_CRANK));
        if (ConstantInfo.hasPolyfactory()) {
            log.error("No way, what the hell are you doing? You installed Polyfactory and CreateFly again.");
        }
    }

    public static List<Tuple<Integer, Goal>> getGoals(BaseNPCLikeEntity npcLikeEntity) {
        if (!ConstantInfo.hasCreateFly() || ConstantInfo.CREATE_FLY_HAND_CRANK == Blocks.AIR) {
            return List.of();
        }
//        System.out.println(NPCWorkModes.CREATE_FLY_HAND_CRANK);
        return List.of(new Tuple<>(0, new NPCFindBlockGoal(npcLikeEntity, NPCWorkModes.CREATE_FLY_HAND_CRANK, ConstantInfo.CREATE_FLY_HAND_CRANK, true, true)));
    }
}