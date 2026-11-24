package cc.thonly.reverie_dreams.neoforge.integration.sparkle;

import com.micaftic.morpher.client.model.ModelAssembly;
import com.micaftic.morpher.client.model.ModelResourceBundle;
import com.micaftic.morpher.client.model.PlayerModelBundle;

/**
 * 将 Sparkle-Morpher 的玩家动作 Controller 体系 * 适配到 ReverieDreams 的 NPCAnimatable。* * 不修改 Sparkle-Morpher 源码。
 */
public final class NPCPlayerAnimationController {
    public static void register(NPCCapability animatable, ModelAssembly assembly) {
        PlayerModelBundle animationBundle = assembly.getAnimationBundle();
        ModelResourceBundle expressionCache = assembly.getExpressionCache();
        if (animationBundle != null) {
            NPCPlayerAnimationControllers.buildControllers(animationBundle, expressionCache).accept(animatable);
        }
    }

}
