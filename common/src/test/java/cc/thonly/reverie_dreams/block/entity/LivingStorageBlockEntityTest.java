package cc.thonly.reverie_dreams.block.entity;

import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import static org.junit.jupiter.api.Assertions.*;

class LivingStorageBlockEntityTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        // Bare bootstrap does not load the vanilla component datapack.
        for (var item : new Item[]{Items.AIR, Items.IRON_INGOT, Items.DIAMOND, Items.WHITE_WOOL})
            if (!item.builtInRegistryHolder().areComponentsBound())
                item.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                        .set(DataComponents.MAX_STACK_SIZE, 64).build());
    }
    private LivingStorageBlockEntity storage() {
        return new LivingStorageBlockEntity(BlockEntityType.BARREL, BlockPos.ZERO, Blocks.BARREL.defaultBlockState());
    }
    @Test void nonFoodCanBeStoredAndRemovedWithoutDuplication() {
        var storage = storage();
        storage.setItem(26, new ItemStack(Items.IRON_INGOT, 64));
        assertEquals(27, storage.getContainerSize());
        assertEquals(17, storage.removeItem(26,17).getCount());
        assertEquals(47, storage.getItem(26).getCount());
        assertEquals(47, storage.removeItemNoUpdate(26).getCount());
        assertTrue(storage.isEmpty());
    }
    @Test void allSlotsAndTheirContentsSurviveSavingAndLoading() {
        var original = storage();
        original.setItem(0, new ItemStack(Items.DIAMOND,3));
        original.setItem(26, new ItemStack(Items.WHITE_WOOL,12));
        var registries = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, registries);
        original.saveAdditional(output);
        var restored = storage();
        restored.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, registries, output.buildResult()));
        assertTrue(restored.getItem(0).is(Items.DIAMOND));
        assertEquals(3, restored.getItem(0).getCount());
        assertTrue(restored.getItem(26).is(Items.WHITE_WOOL));
        assertEquals(12, restored.getItem(26).getCount());
        assertTrue(restored.getItem(1).isEmpty());
    }
}
