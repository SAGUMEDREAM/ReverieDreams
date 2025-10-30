package cc.thonly.reverie_dreams.entity.ai.goal;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import cc.thonly.reverie_dreams.entity.npc.NPCWorkModes;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class NPCWanderAroundFarGoal extends RandomStrollGoal {
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
    protected Vec3 getPosition() {
        if (this.mob.isInWater()) {
            Vec3 vec3d = LandRandomPos.getPos(this.mob, 15, 7);
            return vec3d == null ? super.getPosition() : vec3d;
        }
        if (this.mob.getRandom().nextFloat() >= this.probability) {
            return LandRandomPos.getPos(this.mob, 10, 7);
        }
        return super.getPosition();
    }

    @Override
    public boolean canUse() {
        return this.mob.getLastHurtByMob() == null && super.canUse();
    }
}
