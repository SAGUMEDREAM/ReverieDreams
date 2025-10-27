package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import net.minecraft.entity.ai.FuzzyTargeting;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class NPCWanderAroundFarGoal extends WanderAroundGoal {
    public static final float CHANCE = 0.001f;
    private final BaseNPCLikeEntity npcRole;
    protected final float probability;


    public NPCWanderAroundFarGoal(BaseNPCLikeEntity pathAwareEntity, double d) {
        this(pathAwareEntity, d, CHANCE);
    }

    public NPCWanderAroundFarGoal(BaseNPCLikeEntity mob, double speed, float probability) {
        super(mob, speed);
        this.npcRole = mob;
        this.probability = probability;
    }

    @Override
    public void start() {
        if (this.npcRole.getWorkMode() == NPCWorkModes.PLAYING_MUSIC) {
            return;
        }
        super.start();
    }

    @Override
    @Nullable
    protected Vec3d getWanderTarget() {
        if (this.mob.isTouchingWater()) {
            Vec3d vec3d = FuzzyTargeting.find(this.mob, 15, 7);
            return vec3d == null ? super.getWanderTarget() : vec3d;
        }
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return FuzzyTargeting.find(this.mob, 10, 7);
        }
        return super.getWanderTarget();
    }

    @Override
    public boolean canStart() {
        return this.mob.getAttacker() == null && super.canStart();
    }
}
