package cc.thonly.reverie_dreams.client.renderer.item;

import cc.thonly.reverie_dreams.block.MarisaHatBlock;
import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.util.FumoGrowth;
import cc.thonly.reverie_dreams.mixin.client.ItemLayerRenderStateAccessor;
import cc.thonly.reverie_dreams.mixin.client.ItemStackRenderStateAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.SharedConstants;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FumoHeldItemRendererTest {
    private static BlockItem plush, hat;
    private static final ItemTransform GRIP = new ItemTransform(new Vector3f(15, 30, -10),
            new Vector3f(.2f, .1f, -.3f), new Vector3f(.5f, .6f, .7f));

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        Field holders = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        Field frozen = MappedRegistry.class.getDeclaredField("frozen");
        holders.setAccessible(true);
        frozen.setAccessible(true);
        for (var registry : List.of(BuiltInRegistries.BLOCK, BuiltInRegistries.ITEM)) {
            Object previous = holders.get(registry);
            boolean wasFrozen = frozen.getBoolean(registry);
            try {
                frozen.setBoolean(registry, false);
                holders.set(registry, new IdentityHashMap<>());
                if (registry == BuiltInRegistries.BLOCK) {
                    fixturePlush = new BaseFumoBlock(properties("held_fumo"));
                    fixtureHat = new MarisaHatBlock(properties("held_hat"));
                } else {
                    plush = new BlockItem(fixturePlush, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.parse("test:held_fumo"))));
                    hat = new BlockItem(fixtureHat, new Item.Properties().setId(ResourceKey.create(Registries.ITEM, Identifier.parse("test:held_hat"))));
                }
            } finally {
                holders.set(registry, previous);
                frozen.setBoolean(registry, wasFrozen);
            }
        }
        for (Item item : new Item[]{plush, hat, Items.STICK, Items.AIR}) {
            if (!item.builtInRegistryHolder().areComponentsBound()) {
                item.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
            }
        }
    }

    private static Block fixturePlush, fixtureHat;

    private static BlockBehaviour.Properties properties(String path) {
        return BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.parse("test:" + path)));
    }

    private static ItemStack grown(Item item, int cakes) {
        ItemStack stack = new ItemStack(item);
        FumoGrowth.writeItem(stack, cakes);
        return stack;
    }

    @Test
    void onlyTheFourHandContextsUseSavedGrowth() {
        ItemStack stack = grown(plush, 5);
        for (ItemDisplayContext context : ItemDisplayContext.values()) {
            TestState state = new TestState();
            state.newLayer().setItemTransform(GRIP);
            FumoHeldItemRenderer.apply(state, stack, context);
            boolean held = context.firstPerson() || context == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                    || context == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            assertEquals(held ? .75f : .5f, state.transform(0).scale().x(), .00001f, context.name());
        }
    }

    @Test
    void plainHatEmptyStackAndForgedOrdinaryItemsNeverEnterTheScalePath() {
        for (ItemStack stack : List.of(grown(hat, 10), grown(Items.STICK, 10), ItemStack.EMPTY)) {
            assertFalse(FumoHeldItemRenderer.isHeldFumo(stack, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND));
            // An untouched vanilla state has no test accessor: this also checks the early return.
            FumoHeldItemRenderer.apply(new ItemStackRenderState(), stack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        }
    }

    @Test
    void editedItemDataIsClampedAndMissingDataKeepsTheOriginalSize() {
        for (int cakes : new int[]{-100, 0, 7, 500}) {
            ItemStack stack = new ItemStack(plush);
            CustomData.update(DataComponents.CUSTOM_DATA, stack, tag -> tag.putInt(FumoGrowth.ITEM_KEY, cakes));
            TestState state = new TestState();
            state.newLayer().setItemTransform(GRIP);
            FumoHeldItemRenderer.apply(state, stack, ItemDisplayContext.FIRST_PERSON_LEFT_HAND);
            assertEquals(.5f * FumoGrowth.scale(cakes), state.transform(0).scale().x(), .00001f);
            assertEquals(cakes, stack.get(DataComponents.CUSTOM_DATA).copyTag().getIntOr(FumoGrowth.ITEM_KEY, 0));
        }
        TestState normal = new TestState();
        normal.newLayer().setItemTransform(GRIP);
        FumoHeldItemRenderer.apply(normal, new ItemStack(plush), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        assertSame(GRIP, normal.transform(0));
    }

    @Test
    void bothHandsKeepTheGripPositionWhileTheGeometryActuallyDoubles() {
        ItemTransform large = FumoHeldItemRenderer.scaleTransform(GRIP, 2f, 0f);
        for (boolean left : new boolean[]{false, true}) {
            PoseStack.Pose original = new PoseStack.Pose(), enlarged = new PoseStack.Pose();
            GRIP.apply(left, original);
            large.apply(left, enlarged);
            Vector3f center = new Vector3f(.5f);
            Vector3f oldCenter = original.pose().transformPosition(new Vector3f(center));
            Vector3f newCenter = enlarged.pose().transformPosition(new Vector3f(center));
            assertTrue(oldCenter.distance(newCenter) < .00001f);
            Vector3f corner = new Vector3f(1, 1, 1);
            float oldRadius = original.pose().transformPosition(new Vector3f(corner)).distance(oldCenter);
            float newRadius = enlarged.pose().transformPosition(new Vector3f(corner)).distance(newCenter);
            assertEquals(oldRadius * 2, newRadius, .00001f);
        }
        assertEquals(new Vector3f(.5f, .6f, .7f), GRIP.scale());
        assertSame(GRIP.translation(), large.translation());
        assertSame(GRIP.rotation(), large.rotation());
    }

    @Test
    void everyActiveLayerScalesOnceWithoutEditingTheSharedBakedTransform() {
        TestState first = new TestState(), second = new TestState();
        first.newLayer().setItemTransform(GRIP);
        first.newLayer().setItemTransform(GRIP);
        first.reverie_dreams$getLayers()[2].setItemTransform(GRIP); // Allocated but inactive.
        second.newLayer().setItemTransform(GRIP);
        FumoHeldItemRenderer.apply(first, grown(plush, 10), ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        FumoHeldItemRenderer.apply(second, grown(plush, 3), ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        assertEquals(1f, first.transform(0).scale().x());
        assertEquals(1f, first.transform(1).scale().x());
        assertSame(GRIP, first.transform(2));
        assertEquals(.65f, second.transform(0).scale().x(), .00001f);
        assertEquals(.5f, GRIP.scale().x());
        assertNotSame(first.transform(0).scale(), first.transform(1).scale());
    }

    @Test
    void vanillaClearResetsPooledLayersBeforeAnotherItemOrHandUsesThem() {
        TestState state = new TestState();
        state.newLayer().setItemTransform(GRIP);
        FumoHeldItemRenderer.apply(state, grown(plush, 10), ItemDisplayContext.THIRD_PERSON_LEFT_HAND);
        state.clear();
        assertTrue(state.isEmpty());
        assertSame(ItemTransform.NO_TRANSFORM, state.transform(0));
        state.newLayer().setItemTransform(GRIP);
        FumoHeldItemRenderer.apply(state, grown(plush, 2), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
        assertEquals(.6f, state.transform(0).scale().x(), .00001f);
        state.clear();
        state.newLayer().setItemTransform(GRIP);
        FumoHeldItemRenderer.apply(state, grown(plush, 10), ItemDisplayContext.NONE);
        assertSame(GRIP, state.transform(0));
    }

    @Test
    void boundsAreRecomputedFromTheScaledGeometry() {
        TestState state = new TestState();
        var layer = state.newLayer();
        layer.setExtents(() -> new Vector3f[]{new Vector3f(), new Vector3f(1)});
        layer.setItemTransform(ItemTransform.NO_TRANSFORM);
        AABB original = state.getModelBoundingBox();
        FumoHeldItemRenderer.apply(state, grown(plush, 10), ItemDisplayContext.FIRST_PERSON_RIGHT_HAND);
        AABB large = state.getModelBoundingBox();
        assertEquals(1, original.getXsize());
        assertEquals(2, large.getXsize());
        assertEquals(-1, large.minX);
        assertEquals(1, large.maxX);
    }

    @Test
    void musicSquashesAndReboundsAtEveryGrowthSizeWithoutExceedingTwo() {
        for (float base : new float[]{1f, 1.5f, 2f}) {
            for (float pulse : new float[]{-1f, 0f, 1f}) {
                ItemTransform animated = FumoHeldItemRenderer.scaleTransform(ItemTransform.NO_TRANSFORM, base, pulse);
                assertTrue(animated.scale().x() <= 2f && animated.scale().y() <= 2f);
                assertTrue(animated.scale().x() >= .5f && animated.scale().y() >= .5f);
                assertEquals(animated.scale().x(), animated.scale().z());
                if (pulse > 0) assertTrue(animated.scale().y() < base);
                if (pulse < 0) assertTrue(animated.scale().x() < base);
            }
        }
        assertEquals(1.68f, FumoHeldItemRenderer.scaleTransform(ItemTransform.NO_TRANSFORM, 2, 1).scale().y(), .00001f);
    }

    // Test accessors read actual vanilla fields; native clear(), newLayer(), apply() and bounds stay in use.
    private static final class TestState extends ItemStackRenderState implements ItemStackRenderStateAccessor {
        TestState() {
            write(ItemStackRenderState.class, "layers", this, new LayerRenderState[]{new TestLayer(this), new TestLayer(this), new TestLayer(this)});
        }
        @Override public LayerRenderState[] reverie_dreams$getLayers() { return (LayerRenderState[]) read(ItemStackRenderState.class, "layers", this); }
        @Override public int reverie_dreams$getActiveLayerCount() { return (Integer) read(ItemStackRenderState.class, "activeLayerCount", this); }
        @Override public void reverie_dreams$setCachedModelBoundingBox(AABB bounds) { write(ItemStackRenderState.class, "cachedModelBoundingBox", this, bounds); }
        ItemTransform transform(int index) { return ((ItemLayerRenderStateAccessor) reverie_dreams$getLayers()[index]).reverie_dreams$getItemTransform(); }
    }

    private static final class TestLayer extends ItemStackRenderState.LayerRenderState implements ItemLayerRenderStateAccessor {
        TestLayer(TestState owner) { owner.super(); }
        @Override public ItemTransform reverie_dreams$getItemTransform() { return (ItemTransform) read(ItemStackRenderState.LayerRenderState.class, "itemTransform", this); }
    }

    private static Object read(Class<?> type, String name, Object owner) {
        try { Field field = type.getDeclaredField(name); field.setAccessible(true); return field.get(owner); }
        catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }
    private static void write(Class<?> type, String name, Object owner, Object value) {
        try { Field field = type.getDeclaredField(name); field.setAccessible(true); field.set(owner, value); }
        catch (ReflectiveOperationException error) { throw new AssertionError(error); }
    }
}
