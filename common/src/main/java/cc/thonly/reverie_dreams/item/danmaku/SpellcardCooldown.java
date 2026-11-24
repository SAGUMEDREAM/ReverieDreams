package cc.thonly.reverie_dreams.item.danmaku;

import cc.thonly.reverie_dreams.ReverieDreams;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.UseCooldown;

import java.util.Optional;
import java.util.UUID;

/**
 * Uses the vanilla cooldown component so the server check and slot overlay read the same group.
 */
final class SpellcardCooldown {
    private static final String PREFIX = "cooldown/spellcard/";

    private SpellcardCooldown() {
    }

    static void ensureGroup(ItemStack stack) {
        UseCooldown existing = stack.get(DataComponents.USE_COOLDOWN);
        Identifier group = existing == null ? null : existing.cooldownGroup().orElse(null);
        if (group == null || !group.getNamespace().equals(ReverieDreams.MOD_ID) || !group.getPath().startsWith(PREFIX)) {
            stack.set(DataComponents.USE_COOLDOWN, new UseCooldown(2f,
                    Optional.of(ReverieDreams.id(PREFIX + UUID.randomUUID()))));
        }
    }
}
