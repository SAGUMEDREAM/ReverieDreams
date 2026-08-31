package cc.thonly.reverie_dreams.util.test;

import lombok.extern.slf4j.Slf4j;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.TestOnly;

import java.util.Collection;

@Slf4j
public class ModTest {

    public static class ByCommand {
        @TestOnly
        public static void onTest(ServerPlayer player) {
            log.info("start test holder success");
            Collection<MobEffectInstance> activeEffects = player.getActiveEffects();
            for (MobEffectInstance activeEffect : activeEffects) {
                Holder<MobEffect> effect = activeEffect.getEffect();
                if (!(effect instanceof Holder.Reference<MobEffect>)) {
                    log.error("{} is a direct holder", effect);
                }
            }
            log.info("test holder success");
        }
    }


}
