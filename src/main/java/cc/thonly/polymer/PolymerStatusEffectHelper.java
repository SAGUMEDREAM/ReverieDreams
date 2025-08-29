package cc.thonly.polymer;

import cc.thonly.reverie_dreams.config.ReverieDreamsConfiguration;
import eu.pb4.polymer.core.api.other.PolymerStatusEffect;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.registry.entry.RegistryEntry;

public class PolymerStatusEffectHelper {
    public static void registerOverlay(RegistryEntry<StatusEffect> registryEntry) {
        if (!ReverieDreamsConfiguration.POLYMER_PATCH) {
            return;
        }
        PolymerStatusEffect.registerOverlay(registryEntry.value());
    }
}
