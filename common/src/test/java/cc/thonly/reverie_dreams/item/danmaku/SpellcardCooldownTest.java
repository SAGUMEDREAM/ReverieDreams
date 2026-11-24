package cc.thonly.reverie_dreams.item.danmaku;

import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SpellcardCooldownTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var stick = Items.STICK.builtInRegistryHolder();
        if (!stick.areComponentsBound()) {
            stick.bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
        }
    }

    @Test
    void identicalCardsCoolIndependentlyAndInventoryCopiesKeepTheirOwnGroup() {
        var first = new ItemStack(Items.STICK);
        var second = first.copy();
        SpellcardCooldown.ensureGroup(first);
        SpellcardCooldown.ensureGroup(second);
        var cooldowns = new ItemCooldowns();
        var group = cooldowns.getCooldownGroup(first);
        assertNotEquals(group, cooldowns.getCooldownGroup(second));
        cooldowns.addCooldown(first, 3);
        cooldowns.addCooldown(second, 1);
        var inventoryCopy = first.copy();
        SpellcardCooldown.ensureGroup(inventoryCopy);
        assertEquals(group, cooldowns.getCooldownGroup(inventoryCopy));
        cooldowns.tick();
        assertTrue(cooldowns.isOnCooldown(inventoryCopy));
        assertFalse(cooldowns.isOnCooldown(second));
    }
}
