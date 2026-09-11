package cc.thonly.reverie_dreams.neoforge.integration.sparkle.predicate;

import cc.thonly.reverie_dreams.neoforge.integration.sparkle.NPCCapability;
import com.micaftic.morpher.client.animation.IAnimationPredicate;
import com.micaftic.morpher.geckolib3.core.enums.PlayState;
import com.micaftic.morpher.geckolib3.core.event.predicate.AnimationEvent;
import com.micaftic.morpher.molang.runtime.ExpressionEvaluator;

public final class NPCRouletteAnimationPredicate implements IAnimationPredicate<NPCCapability> {
    @Override
    public PlayState predicate(AnimationEvent<NPCCapability> event, ExpressionEvaluator<?> evaluator) {
        String animation = event.getAnimatable().getRouletteAnimation();
        return animation == null || animation.isBlank()
                ? PlayState.STOP
                : IAnimationPredicate.predicate(event, animation);
    }
}
