package cc.thonly.reverie_dreams.fabric.polymer.helper;

import eu.pb4.polymer.core.api.other.PolymerMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public class PolymerStatusEffectHelper {
    public static void registerOverlay(Holder<MobEffect> registryEntry) {
        PolymerMobEffect.registerOverlay(registryEntry.value());
    }
}
