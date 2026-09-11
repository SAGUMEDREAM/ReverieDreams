package cc.thonly.reverie_dreams.neoforge.integration.sparkle.predicate;

import cc.thonly.reverie_dreams.neoforge.integration.sparkle.NPCCapability;
import com.micaftic.morpher.client.animation.IAnimationPredicate;
import com.micaftic.morpher.core.compat.touhoulittlemaid.TouhouLittleMaidCompat;
import com.micaftic.morpher.geckolib3.core.enums.PlayState;
import com.micaftic.morpher.geckolib3.core.event.predicate.AnimationEvent;
import com.micaftic.morpher.molang.runtime.ExpressionEvaluator;

public final class NPCStateAnimationPredicate implements IAnimationPredicate<NPCCapability> {
    private final boolean renderState;

    public NPCStateAnimationPredicate(boolean renderState) {
        this.renderState = renderState;
    }

    @Override
    public PlayState predicate(AnimationEvent<NPCCapability> event, ExpressionEvaluator<?> evaluator) {
        String animation = this.renderState
                ? TouhouLittleMaidCompat.getMaidRenderAnimation(event.getAnimatable().getEntity())
                : TouhouLittleMaidCompat.getMaidGameAnimation(event.getAnimatable().getEntity());
        return animation == null || animation.isBlank()
                ? PlayState.STOP
                : IAnimationPredicate.playLoopAnimation(event, animation);
    }
}
