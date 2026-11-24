package cc.thonly.reverie_dreams.polymer.helper;

import cc.thonly.reverie_dreams.util.item.PolymerMagazineData;
import net.minecraft.SharedConstants;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PolymerMagazineDataTest {
    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        if (!Items.PAPER.builtInRegistryHolder().areComponentsBound()) {
            Items.PAPER.builtInRegistryHolder().bindComponents(DataComponentMap.EMPTY);
        }
    }

    @Test
    void readsServerRoundsWithoutChangingTheReceivedItem() {
        for (int rounds : new int[]{0, 7, 22, 23, 30}) {
            var stack = received(rounds);
            var data = stack.get(DataComponents.CUSTOM_DATA).copyTag();
            assertEquals(rounds, PolymerMagazineData.encodedRounds(stack));
            assertEquals(data, stack.get(DataComponents.CUSTOM_DATA).copyTag());
        }
    }

    @Test
    void clampsInvalidCountsAndIgnoresUnrelatedNativeItems() {
        assertEquals(0, PolymerMagazineData.encodedRounds(received(-10)));
        assertEquals(30, PolymerMagazineData.encodedRounds(received(99)));
        assertNull(PolymerMagazineData.encodedRounds(new ItemStack(Items.PAPER)));
        var malformed = new ItemStack(Items.PAPER);
        var root = new CompoundTag();
        root.put("$polymer:stack", new CompoundTag());
        malformed.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        assertNull(PolymerMagazineData.encodedRounds(malformed));
    }

    @Test
    void recognizesOnlyTheExistingMagazineLineOnEncodedItems() {
        var stack = received(23);
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.empty().append(Component.translatable("item.tooltip.magazine.loaded", 23, 30)))));
        assertTrue(PolymerMagazineData.hasGeneratedMagazineTooltip(stack));
        var nativeStack = new ItemStack(Items.PAPER);
        nativeStack.set(DataComponents.LORE, stack.get(DataComponents.LORE));
        assertFalse(PolymerMagazineData.hasGeneratedMagazineTooltip(nativeStack));
    }

    @Test
    void preservesLocalTooltipWhenServerDidNotProvideAnAmmoLine() {
        var stack = received(23);
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.literal("Custom gun lore"))));
        assertFalse(PolymerMagazineData.hasGeneratedMagazineTooltip(stack));
    }

    private static ItemStack received(int rounds) {
        var root = new CompoundTag();
        var encoded = new CompoundTag();
        encoded.putString("id", "reverie_dreams:weapon_of_the_moon");
        var components = new CompoundTag();
        components.putInt("reverie_dreams:magazine_rounds", rounds);
        encoded.put("components", components);
        root.put("$polymer:stack", encoded);
        var stack = new ItemStack(Items.PAPER);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        return stack;
    }
}
