package cc.thonly.reverie_dreams.polymer.item;

import cc.thonly.reverie_dreams.item.weapon.Levatin;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Mapping API test only: does not start a Polymer server or export a resource pack. */
class LevatinPolymerModelTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        if (!Items.IRON_SWORD.builtInRegistryHolder().areComponentsBound()) {
            Items.IRON_SWORD.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 1).set(DataComponents.ITEM_MODEL, Levatin.STAFF_MODEL).build());
        }
    }

    @Test void existingOverlayUsesThePersistedModelForBothForms() {
        var overlay = new PolymerItemImpl(Items.IRON_SWORD);
        var stack = new ItemStack(Items.IRON_SWORD);
        assertEquals(Levatin.STAFF_MODEL, overlay.getPolymerItemModel(stack, null, null));
        stack.set(DataComponents.ITEM_MODEL, Levatin.FLAME_MODEL);
        assertEquals(Levatin.FLAME_MODEL, overlay.getPolymerItemModel(stack, null, null));
        assertEquals(Levatin.FLAME_MODEL, overlay.getPolymerItemModel(stack.copy(), null, null));
        stack.set(DataComponents.ITEM_MODEL, Levatin.STAFF_MODEL);
        assertEquals(Levatin.STAFF_MODEL, overlay.getPolymerItemModel(stack, null, null));
    }
}
