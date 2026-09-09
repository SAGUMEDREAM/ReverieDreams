package cc.thonly.reverie_dreams.fabric.integration.sparkle;

import cc.thonly.reverie_dreams.entity.npc.BaseNPCLikeEntity;
import com.micaftic.morpher.capability.VehicleModelCapability;
import com.micaftic.morpher.geckolib3.resource.GeckoLibCache;
import com.micaftic.morpher.molang.parser.ParseException;
import com.micaftic.morpher.molang.runtime.Int2FloatOpenHashMapStruct;
import com.micaftic.morpher.molang.runtime.Struct;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

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

    public NPCCapability(BaseNPCLikeEntity entity) {
        super(entity);
    }

    public static Optional<NPCCapability> get(Entity entity) {
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
