package cc.thonly.reverie_dreams.fabric.polymer.helper;

import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;

public class PolymerStatusEffectHelper {
    public static void registerOverlay(Holder<MobEffect> registryEntry) {
        PolymerStatusEffect.registerOverlay(registryEntry.value());
    }
}
