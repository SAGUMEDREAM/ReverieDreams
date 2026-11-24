package cc.thonly.reverie_dreams.neoforge.integration.sparkle.predicate;

import cc.thonly.reverie_dreams.entity.npc.NPCSimpleEntity;
import cc.thonly.reverie_dreams.neoforge.integration.sparkle.NPCCapability;
import cc.thonly.reverie_dreams.neoforge.integration.sparkle.NPCActionCatalog;
import com.micaftic.morpher.client.animation.IAnimationPredicate;
import com.micaftic.morpher.geckolib3.core.builder.Animation;
import com.micaftic.morpher.geckolib3.core.builder.ILoopType;
import com.micaftic.morpher.geckolib3.core.enums.PlayState;
import com.micaftic.morpher.geckolib3.core.event.predicate.AnimationEvent;
import com.micaftic.morpher.molang.runtime.ExpressionEvaluator;
import org.jetbrains.annotations.Nullable;

public final class NPCRouletteAnimationPredicate implements IAnimationPredicate<NPCCapability> {
    private int revision;
    private String action = "";
    private @Nullable Animation clip;

    @Override
    public PlayState predicate(AnimationEvent<NPCCapability> event, ExpressionEvaluator<?> evaluator) {
        var animatable = event.getAnimatable();
        if (!(animatable.getEntity() instanceof NPCSimpleEntity npc)
                || !npc.reverie_dreams$getModelId().equals(animatable.getModelId())) {
            return PlayState.STOP;
        }
        var controller = event.getController();
        if (revision != npc.getYsmActionRevision()) {
            revision = npc.getYsmActionRevision();
            action = npc.getYsmAction();
            clip = NPCActionCatalog.declaredClip(animatable.getModelAssembly(), action);
            // Cancel playback while keeping the model's registered bone queues.
            controller.clearAnimation();
        }
        if (clip == null) {
            return PlayState.STOP;
        }
        float elapsed = Math.max(0L, npc.level().getGameTime() - npc.getYsmActionStart()) + event.getPartialTick();
        if (clip.loop == ILoopType.EDefaultLoopTypes.PLAY_ONCE && elapsed > clip.animationLength + 1.0F) {
            clip = null;
            controller.clearAnimation();
            return PlayState.STOP;
        }
        controller.setAnimation(action);
        controller.setAnimationTickOverride(elapsed);
        return PlayState.CONTINUE;
    }
}
