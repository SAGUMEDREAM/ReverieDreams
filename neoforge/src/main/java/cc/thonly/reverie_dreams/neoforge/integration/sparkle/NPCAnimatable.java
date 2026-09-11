package cc.thonly.reverie_dreams.neoforge.integration.sparkle;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.micaftic.morpher.client.entity.LivingAnimatable;
import com.micaftic.morpher.client.model.ModelAssembly;
import com.micaftic.morpher.geckolib3.core.event.predicate.AnimationEvent;
import com.micaftic.morpher.molang.runtime.Struct;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class NPCAnimatable extends LivingAnimatable<BaseNPCLikeEntity> {

    public NPCAnimatable(BaseNPCLikeEntity entity) {
        super(entity, true);
    }

    @Override
    public void registerAnimationControllers() {

    }

    @Override
    public boolean shouldSkipAnimation(AnimationEvent<?> event) {
        return false;
    }

    public @Nullable Struct getServerVarContainer() {
        return null;
    }

    @Override
    public boolean hasCustomTexture() {
        return true;
    }

    @Override
    public @NotNull TexturedModelWrapper buildRenderShape(ModelAssembly modelAssembly, boolean isActive) {
        return new TexturedModelWrapper(
                modelAssembly,
                isActive,
                false,
                true,
                300
        );
    }

}