package cc.thonly.reverie_dreams.block.entity;

import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.block.MarisaHatBlock;
import cc.thonly.reverie_dreams.util.FumoGrowth;
import net.minecraft.SharedConstants;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.*;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.*;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.storage.*;
import net.minecraft.world.phys.*;
import org.junit.jupiter.api.*;
import sun.misc.Unsafe;
import java.util.*;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.component.CustomData;
import static org.junit.jupiter.api.Assertions.*;

class FumoGrowthTest {
    static Unsafe unsafe; static BaseFumoBlock block, passable; static MarisaHatBlock hat;
    @BeforeAll static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion(); Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe"); field.setAccessible(true); unsafe = (Unsafe) field.get(null);
        var holders = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen = MappedRegistry.class.getDeclaredField("frozen"); holders.setAccessible(true); frozen.setAccessible(true);
        Object previous = holders.get(BuiltInRegistries.BLOCK); boolean wasFrozen = frozen.getBoolean(BuiltInRegistries.BLOCK);
        try {
            frozen.setBoolean(BuiltInRegistries.BLOCK, false); holders.set(BuiltInRegistries.BLOCK, new IdentityHashMap<>());
            block = new BaseFumoBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.parse("test:growing_fumo"))));
            passable = new BaseFumoBlock(BlockBehaviour.Properties.of().noCollision().setId(ResourceKey.create(Registries.BLOCK, Identifier.parse("test:passable_fumo"))));
            hat = new MarisaHatBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK, Identifier.parse("test:plain_hat"))));
        } finally { holders.set(BuiltInRegistries.BLOCK, previous); frozen.setBoolean(BuiltInRegistries.BLOCK, wasFrozen); }
        for (Item item : new Item[] {Items.CAKE, Items.STICK, Items.AIR}) if (!item.builtInRegistryHolder().areComponentsBound()) {
            item.builtInRegistryHolder().bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
        }
    }
    static final class Host extends FumoBlockEntity {
        int changed;
        Host(BlockState state) { super(BlockEntityType.BARREL, BlockPos.ZERO, state); }
        @Override public boolean isValidBlockState(BlockState state) { return true; } // Fixture type; production registration is unchanged.
        @Override public void setChanged() { changed++; }
    }
    static final class World extends ServerLevel {
        Host host; boolean client; int updates, sounds, writes;
        private World() { super(null, null, null, null, null, null, false, 0, List.of(), false); }
        @Override public boolean isClientSide() { return client; }
        @Override public BlockEntity getBlockEntity(BlockPos pos) { return host; }
        @Override public void sendBlockUpdated(BlockPos pos, BlockState old, BlockState state, int flags) { updates++; }
        @Override public void playSound(Entity except, BlockPos pos, SoundEvent sound, SoundSource source, float volume, float pitch) { sounds++; }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags) { writes++; return true; }
    }
    static final class Person extends Player {
        boolean creative, denied, spectator, sneaking; int messages;
        private Person() { super(null, null); }
        @Override public GameType gameMode() { return creative ? GameType.CREATIVE : GameType.SURVIVAL; }
        @Override public boolean hasInfiniteMaterials() { return creative; }
        @Override public boolean mayBuild() { return !denied; }
        @Override public boolean isSpectator() { return spectator; }
        @Override public boolean isShiftKeyDown() { return sneaking; }
        @Override public void sendSystemMessage(Component message) { messages++; }
    }
    World world(BlockState state) throws Exception {
        World world = (World) unsafe.allocateInstance(World.class); world.host = new Host(state); world.host.setLevel(world); return world;
    }
    Person player() throws Exception { return (Person) unsafe.allocateInstance(Person.class); }
    void feed(World world, Person player, ItemStack stack) {
        world.host.getBlockState().useItemOn(stack, world, player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.ZERO, Direction.UP, BlockPos.ZERO, false));
    }
    @Test void tenCakesGrowExactlyToTwoAndFurtherClicksKeepTheCake() throws Exception {
        World world = world(block.defaultBlockState()); Person player = player(); ItemStack cakes = new ItemStack(Items.CAKE, 12);
        for (int i = 0; i < 12; i++) feed(world, player, cakes);
        assertEquals(10, world.host.getCakesFed()); assertEquals(2f, world.host.getGrowthScale());
        assertEquals(2, cakes.getCount()); assertEquals(10, world.updates); assertEquals(10, world.sounds); assertEquals(2, player.messages);
    }
    @Test void creativeFeedsWithoutConsumingAndShiftChangesDirectionInstead() throws Exception {
        World world = world(block.defaultBlockState()); Person player = player(); player.creative = true;
        ItemStack cakes = new ItemStack(Items.CAKE); feed(world, player, cakes);
        assertEquals(1, cakes.getCount()); assertEquals(1, world.host.getCakesFed());
        player.sneaking = true; player.setYRot(90); feed(world, player, cakes);
        assertEquals(1, world.host.getCakesFed()); assertEquals(1, cakes.getCount()); assertEquals(1, world.writes);
    }
    @Test void deniedSpectatorAndClientInteractionsCannotConsumeOrGrow() throws Exception {
        World world = world(block.defaultBlockState()); Person player = player(); ItemStack cakes = new ItemStack(Items.CAKE, 3);
        player.denied = true; feed(world, player, cakes); player.denied = false;
        player.spectator = true; feed(world, player, cakes); player.spectator = false;
        world.client = true; feed(world, player, cakes);
        assertEquals(0, world.host.getCakesFed()); assertEquals(3, cakes.getCount()); assertEquals(0, world.updates);
    }
    @Test void grownSelectionAndCollisionUseTheSameBaseScale() throws Exception {
        World world = world(block.defaultBlockState()); world.host.setCakesFed(10);
        var selection = world.host.getBlockState().getShape(world, BlockPos.ZERO).bounds();
        var collision = world.host.getBlockState().getCollisionShape(world, BlockPos.ZERO).bounds();
        assertEquals(selection, collision); assertEquals(1.25, selection.getXsize()); assertEquals(1.25, selection.maxY);
        assertEquals(-.125, selection.minX);
    }
    @Test void originalPassablePlushRemainsPassableWhileItsSelectionGrows() throws Exception {
        World world = world(passable.defaultBlockState()); world.host.setCakesFed(10);
        assertEquals(1.25, world.host.getBlockState().getShape(world, BlockPos.ZERO).bounds().getXsize());
        assertTrue(world.host.getBlockState().getCollisionShape(world, BlockPos.ZERO).isEmpty());
    }
    @Test void saveLoadAndUpdateTagsKeepGrowthAndClampEditedValues() {
        Host host = new Host(block.defaultBlockState()); host.setCakesFed(7);
        var access = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
        var out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, access); host.saveAdditional(out);
        Host loaded = new Host(block.defaultBlockState());
        loaded.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, access, out.buildResult()));
        assertEquals(7, loaded.getCakesFed()); assertEquals(1.7f, loaded.getGrowthScale());
        assertEquals(7, host.getUpdateTag(access).getIntOr("CakesFed", -1));
        CompoundTag bad = new CompoundTag(); bad.putInt("CakesFed", 500);
        loaded.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, access, bad)); assertEquals(10, loaded.getCakesFed());
        loaded.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, access, new CompoundTag())); assertEquals(0, loaded.getCakesFed());
    }
    @Test void itemGrowthSurvivesARealItemCodecRoundTripAndKeepsOtherComponents() {
        ItemStack item = new ItemStack(Items.STICK); item.set(DataComponents.CUSTOM_NAME, Component.literal("My plush"));
        CustomData.update(DataComponents.CUSTOM_DATA, item, tag -> tag.putString("other", "keep"));
        FumoGrowth.writeItem(item, 8);
        assertEquals(8, FumoGrowth.fromItem(item)); assertEquals("keep", item.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("other", ""));
        var ops = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY).createSerializationContext(NbtOps.INSTANCE);
        var restored = ItemStack.CODEC.parse(ops, ItemStack.CODEC.encodeStart(ops, item).getOrThrow()).getOrThrow();
        assertEquals(8, FumoGrowth.fromItem(restored)); assertEquals("My plush", restored.getHoverName().getString());
        FumoGrowth.writeItem(restored, -1); assertEquals(0, FumoGrowth.fromItem(restored));
        assertEquals("keep", restored.get(DataComponents.CUSTOM_DATA).copyTag().getStringOr("other", ""));
    }
    @Test void placingTheGrownItemRestoresItsSavedMultiplier() throws Exception {
        World world = world(passable.defaultBlockState()); ItemStack saved = new ItemStack(Items.STICK);
        FumoGrowth.writeItem(saved, 6);
        passable.setPlacedBy(world, BlockPos.ZERO, world.host.getBlockState(), player(), saved);
        assertEquals(6, world.host.getCakesFed()); assertEquals(1.6f, world.host.getGrowthScale());
        assertEquals(1, world.updates);
    }
    @Test void displayCallbackOnlyFiresForActualServerGrowthAndCanBeDetached() throws Exception {
        World world = world(block.defaultBlockState()); int[] calls = {0}; Runnable callback = () -> calls[0]++;
        world.host.setGrowthUpdate(callback); world.host.setCakesFed(3); world.host.setCakesFed(3);
        assertEquals(2, calls[0]); assertEquals(1, world.updates);
        world.host.clearGrowthUpdate(callback); world.host.setCakesFed(4); assertEquals(2, calls[0]);
    }
    @Test void hatsDoNotEatCakeOrUseGrowthEvenWithEditedData() throws Exception {
        World world = world(hat.defaultBlockState()); Person player = player(); ItemStack cakes = new ItemStack(Items.CAKE, 2);
        feed(world, player, cakes); assertEquals(2, cakes.getCount()); assertEquals(0, world.host.getCakesFed());
        world.host.setCakesFed(10); assertEquals(1f, world.host.getGrowthScale());
    }
}
