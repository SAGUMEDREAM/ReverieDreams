package cc.thonly.reverie_dreams.neoforge.integration.sparkle;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.micaftic.morpher.YesSteveModel;
import com.micaftic.morpher.capability.VehicleModelCapability;
import com.micaftic.morpher.client.model.ModelAssembly;
import com.micaftic.morpher.geckolib3.core.molang.value.IValue;
import com.micaftic.morpher.geckolib3.resource.GeckoLibCache;
import com.micaftic.morpher.molang.parser.ParseException;
import com.micaftic.morpher.molang.runtime.Int2FloatOpenHashMapStruct;
import com.micaftic.morpher.molang.runtime.Struct;
import com.micaftic.morpher.resource.models.ModelProperties;
import com.micaftic.morpher.util.AnimationRouletteDebugLog;
import com.micaftic.morpher.util.data.OrderedStringMap;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import lombok.Getter;
import lombok.Setter;
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
    @Getter
    private String rouletteAnimation = "";
    @Getter
    @Setter
    static Consumer<NPCCapability> capabilityConsumer = null;

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

        NPCPlayerAnimationController.register(this);
        if (capabilityConsumer instanceof Consumer consumer) {
            consumer.accept(this);
        }
    }


    public static synchronized Optional<NPCCapability> get(Entity entity) {
        if (entity instanceof BaseNPCLikeEntity npcLikeEntity) {
            return Optional.of(CACHE.compute(npcLikeEntity.getUUID(), (uuid, current) -> current != null && current.getEntity() == npcLikeEntity ? current : new NPCCapability(npcLikeEntity)));
        }

        return Optional.empty();
    }

    public void applySyncedState(VehicleModelCapability state, Int2FloatOpenHashMap values) {
        if (!state.isInitialized()) {
            this.rouletteAnimation = "";
            this.serverVars = null;
            this.resetModel();
        } else {
            this.serverVars = new Int2FloatOpenHashMapStruct(values);
            this.rouletteAnimation = state.getRouletteAnimation();
            this.initModelWithTexture(state.getOwnerModelId(), state.getOwnerTexture());
        }
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

    public void requestModelSwitch(String str) {
        String animationName = this.resolvePlayableAnimation(str);
        if (animationName != null) {
            AnimationRouletteDebugLog.info("client playback request={} resolved={} fallback={}", new Object[]{str, animationName, !animationName.equals(str)});
            this.selectedModelId = animationName;
            this.isModelSwitching = true;
            this.isDisabled = true;
        } else {
            if (AnimationRouletteDebugLog.enabled() && str != null && !str.isBlank() && !"idle".equals(str)) {
                try {
                    YesSteveModel.LOGGER.warn("[SM] 轮盘动画 '{}' 在当前模型的动画列表中不存在，已忽略播放；该模型可用动画: {}", str, this.getModelAssembly().getAnimationBundle().getMainAnimations().keySet());
                } catch (Exception var4) {
                }
            }

            this.isModelSwitching = false;
        }
    }

    private @Nullable String resolvePlayableAnimation(String animationName) {
        if (animationName != null && !animationName.isBlank()) {
            if (this.getAnimation(animationName) != null) {
                return animationName;
            } else {
                ModelProperties properties = this.getModelAssembly().getModelData().getModelProperties();
                String resolved = this.resolveExtraAnimationValue(properties.getExtraAnimation(), animationName);
                if (resolved != null) {
                    return resolved;
                } else {
                    for(OrderedStringMap<String, String> group : properties.getExtraAnimationClassify().values()) {
                        resolved = this.resolveExtraAnimationValue(group, animationName);
                        if (resolved != null) {
                            return resolved;
                        }
                    }

                    return null;
                }
            }
        } else {
            return null;
        }
    }

    private @Nullable String resolveExtraAnimationValue(OrderedStringMap<String, String> animations, String key) {
        if (animations != null && key != null) {
            for(Map.Entry<String, String> entry : animations.entrySet()) {
                if (key.equals(entry.getKey())) {
                    String value = (String)entry.getValue();
                    return value != null && this.getAnimation(value) != null ? value : null;
                }
            }

            return null;
        } else {
            return null;
        }
    }

//    @SuppressWarnings({"rawtypes", "unchecked"})
//    public void registerAnimationControllers() {
//        Object installer = this.getModelAssembly().getAnimationBundle().getMaidControllerInstaller();
//        if (installer instanceof Consumer consumer) {
//            consumer.accept(this);
//        }
//
//    }

    public @Nullable Struct getServerVarContainer() {
        return this.serverVars;
    }

    public void setupAnim(float seekTime, boolean firstPerson) {
        super.setupAnim(seekTime, firstPerson);
        this.getEvaluationContext().setRoamingProperties(this.serverVars);
    }

}
