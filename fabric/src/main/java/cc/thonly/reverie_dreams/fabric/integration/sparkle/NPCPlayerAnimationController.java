package cc.thonly.reverie_dreams.fabric.integration.sparkle;

import com.micaftic.morpher.client.animation.predicate.*;
import com.micaftic.morpher.client.model.ModelAssembly;
import com.micaftic.morpher.client.model.ModelResourceBundle;
import com.micaftic.morpher.client.model.PlayerModelBundle;
import com.micaftic.morpher.client.model.processor.*;

import java.util.function.Consumer;

/**
 * 将 Sparkle-Morpher 的玩家动作 Controller 体系 * 适配到 ReverieDreams 的 NPCAnimatable。* * 不修改 Sparkle-Morpher 源码。
 */
public final class NPCPlayerAnimationController {
    private static boolean initialized = false;

    public static synchronized void register(NPCCapability animatable) {
        if (initialized) {
            return;
        }
        initialized = true;
        ModelAssembly assembly = animatable.getModelAssembly();
        PlayerModelBundle animationBundle = assembly.getAnimationBundle();
        ModelResourceBundle expressionCache = assembly.getExpressionCache();
        Consumer<NPCCapability> npcCapabilityConsumer = NPCPlayerAnimationControllers.buildControllers(animationBundle, expressionCache);
        NPCCapability.setCapabilityConsumer(npcCapabilityConsumer);
    }

}