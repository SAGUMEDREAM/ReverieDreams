package cc.thonly.reverie_dreams.fabric.integration.sparkle;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.micaftic.morpher.client.animation.AnimationTracker;
import com.micaftic.morpher.client.animation.molang.MolangEventDispatcher;
import com.micaftic.morpher.client.entity.IPreviewAnimatable;
import com.micaftic.morpher.client.entity.LivingAnimatable;
import com.micaftic.morpher.client.model.ModelAssembly;
import com.micaftic.morpher.core.compat.touhoulittlemaid.MaidCapability;
import com.micaftic.morpher.geckolib3.core.event.predicate.AnimationEvent;
import com.micaftic.morpher.geckolib3.core.molang.value.IValue;
import com.micaftic.morpher.molang.runtime.Struct;
import com.micaftic.morpher.resource.models.ModelProperties;
import com.micaftic.morpher.util.data.OrderedStringMap;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

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