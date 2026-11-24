package cc.thonly.reverie_dreams.neoforge.integration.sparkle;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.micaftic.morpher.client.model.ModelAssembly;
import com.micaftic.morpher.geckolib3.core.molang.value.IValue;
import com.micaftic.morpher.geckolib3.resource.GeckoLibCache;
import com.micaftic.morpher.molang.parser.ParseException;
import com.micaftic.morpher.molang.runtime.Struct;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j
public class NPCCapability extends NPCAnimatable {
    private static final Map<UUID, NPCCapability> CACHE = new ConcurrentHashMap<>();
    private @Nullable Struct serverVars;

    public boolean isModelSwitching = false;
    public String selectedModelId = "idle";
    public boolean isDisabled = false;
    private List<IValue> syncIValues = null;

    public NPCCapability(BaseNPCLikeEntity entity) {
        super(entity);
    }

    /**
     * Sparkle 的动画控制器默认必须由 AnimatableEntity 子类注册。
     * <p>
     * NPC 当前首先使用 Sparkle 的：
     * - movement
     * - head tracking
     * - Molang
     * - YSM animation
     * pipeline。
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void registerAnimationControllers() {
        ModelAssembly assembly = getModelAssembly();

        if (assembly == null) {
            return;
        }

        NPCPlayerAnimationController.register(this, assembly);
    }


    public static synchronized Optional<NPCCapability> get(Entity entity) {
        if (entity instanceof BaseNPCLikeEntity npcLikeEntity) {
            return Optional.of(CACHE.compute(npcLikeEntity.getUUID(), (uuid, current) -> current != null && current.getEntity() == npcLikeEntity ? current : new NPCCapability(npcLikeEntity)));
        }

        return Optional.empty();
    }

    public void executeMolang(String expression) {
        if (this.isModelReady() && expression != null && !expression.isBlank()) {
            try {
                this.executeExpression(GeckoLibCache.parseSimpleExpression(expression), true, false, (Consumer) null);
            } catch (ParseException e) {
                log.error("Failed to execute maid molang {}", expression, e);
            }

        }
    }

    public @Nullable Struct getServerVarContainer() {
        return this.serverVars;
    }

    public void setupAnim(float seekTime, boolean firstPerson) {
        super.setupAnim(seekTime, firstPerson);
        this.getEvaluationContext().setRoamingProperties(this.serverVars);
    }

}
