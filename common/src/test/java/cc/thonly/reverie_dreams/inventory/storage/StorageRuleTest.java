package cc.thonly.reverie_dreams.inventory.storage;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class StorageRuleTest {
    private final StorageSettings settings = new StorageSettings(true, 0, 0, List.of("minecraft:logs"));
    @BeforeAll static void bootstrap() { StorageTestItems.bootstrap(); }

    @Test void everyTwoRotationsSwitchModesAndWrap() {
        var expected = List.of(StorageRule.Mode.ITEM, StorageRule.Mode.CATEGORY, StorageRule.Mode.INPUT, StorageRule.Mode.OVERFLOW);
        for (int i = 0; i < 24; i++) assertEquals(expected.get(i % 8 / 2), StorageRule.modeForRotation(i));
        assertEquals(StorageRule.Mode.OVERFLOW, StorageRule.modeForRotation(-1));
    }

    @Test void itemFramesAcceptTypeVariantsWhileMergingRemainsStrict() {
        var rule = frame(Items.WHEAT.getDefaultInstance(), 0);
        var named = new ItemStack(Items.WHEAT);
        named.set(DataComponents.CUSTOM_NAME, Component.literal("different"));
        assertTrue(rule.accepts(named, new SimpleContainer(1), settings));
        assertFalse(rule.accepts(new ItemStack(Items.DIAMOND), new SimpleContainer(1), settings));
        assertEquals(0, StorageTransfer.capacity(new SimpleContainer(new ItemStack(Items.WHEAT, 1)), named, 1));
    }

    @Test void multipleSamplesAreUnionButConflictingModesDisableTheContainer() {
        var rule = StorageRule.of(List.of(new StorageRule.Sample(new ItemStack(Items.WHEAT), 0), new StorageRule.Sample(new ItemStack(Items.DIAMOND), 1)));
        assertTrue(rule.accepts(new ItemStack(Items.DIAMOND), new SimpleContainer(1), settings));
        assertTrue(rule.accepts(new ItemStack(Items.WHEAT), new SimpleContainer(1), settings));
        var conflict = StorageRule.of(List.of(new StorageRule.Sample(new ItemStack(Items.WHEAT), 0), new StorageRule.Sample(new ItemStack(Items.DIAMOND), 4)));
        assertEquals(StorageRule.Mode.CONFLICT, conflict.mode());
        assertFalse(conflict.accepts(new ItemStack(Items.WHEAT), new SimpleContainer(1), settings));
        assertEquals(StorageRule.Mode.AUTO, frame(ItemStack.EMPTY, 4).mode());
    }

    @Test void inputNeverAcceptsDepositsAndOverflowAcceptsAnyType() {
        assertFalse(frame(new ItemStack(Items.WHEAT), 4).accepts(new ItemStack(Items.WHEAT), new SimpleContainer(1), settings));
        assertTrue(frame(new ItemStack(Items.WHEAT), 6).accepts(new ItemStack(Items.DIAMOND), new SimpleContainer(1), settings));
    }

    @Test void automaticStorageRequiresUniformComponentsAndRespectsEmptyClaimSetting() {
        var auto = StorageRule.of(List.of());
        var mixed = new SimpleContainer(new ItemStack(Items.WHEAT), new ItemStack(Items.DIAMOND));
        assertFalse(auto.accepts(new ItemStack(Items.WHEAT), mixed, settings));
        assertFalse(auto.accepts(new ItemStack(Items.WHEAT), new SimpleContainer(1), new StorageSettings(false, 0, 0, List.of())));
        assertTrue(auto.accepts(new ItemStack(Items.WHEAT), new SimpleContainer(new ItemStack(Items.WHEAT)), settings));
    }

    @Test void categoriesUseOnlyTheFirstAllowlistedMatchingTag() throws Exception {
        TagKey<Item> logs = TagKey.create(Registries.ITEM, Identifier.withDefaultNamespace("logs"));
        TagKey<Item> broad = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("test", "everything"));
        var field = Holder.Reference.class.getDeclaredField("tags");
        field.setAccessible(true);
        var holders = List.of(Items.OAK_LOG.builtInRegistryHolder(), Items.BIRCH_LOG.builtInRegistryHolder(), Items.DIAMOND.builtInRegistryHolder());
        Object[] previous = new Object[holders.size()];
        try {
            for (int i = 0; i < holders.size(); i++) {
                previous[i] = field.get(holders.get(i));
                field.set(holders.get(i), i < 2 ? Set.of(logs, broad) : Set.of(broad));
            }
            var rule = frame(new ItemStack(Items.OAK_LOG), 2);
            assertTrue(rule.accepts(new ItemStack(Items.BIRCH_LOG), new SimpleContainer(1), settings));
            assertFalse(rule.accepts(new ItemStack(Items.DIAMOND), new SimpleContainer(1), settings));
            assertFalse(rule.accepts(new ItemStack(Items.BIRCH_LOG), new SimpleContainer(1), new StorageSettings(true, 0, 0, List.of())));
        } finally {
            for (int i = 0; i < holders.size(); i++) field.set(holders.get(i), previous[i]);
        }
    }

    private static StorageRule frame(ItemStack item, int rotation) { return StorageRule.of(List.of(new StorageRule.Sample(item, rotation))); }
}
