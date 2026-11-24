package cc.thonly.reverie_dreams.block;

import cc.thonly.reverie_dreams.block.base.BaseFumoBlock;
import cc.thonly.reverie_dreams.block.entity.FumoBlockEntity;
import cc.thonly.reverie_dreams.event.block.FumoBlockEventHandlers;
import cc.thonly.reverie_dreams.state.SixteenDirection;
import com.mojang.authlib.GameProfile;
import com.mojang.math.Axis;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainerFactory;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.joml.Vector3f;
import sun.misc.Unsafe;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FumoBlockTest {
    private static Unsafe unsafe;
    private static BaseFumoBlock block;
    private static MarisaHatBlock hat;

    @BeforeAll
    static void bootstrap() throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        var field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        unsafe = (Unsafe) field.get(null);
        // Bootstrap freezes vanilla's registry. Allow only these unregistered test
        // instances to obtain holders, then restore the registry without changing its entries.
        var holders = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        var frozen = MappedRegistry.class.getDeclaredField("frozen");
        holders.setAccessible(true);
        frozen.setAccessible(true);
        Object previous = holders.get(BuiltInRegistries.BLOCK);
        boolean wasFrozen = frozen.getBoolean(BuiltInRegistries.BLOCK);
        try {
            frozen.setBoolean(BuiltInRegistries.BLOCK, false);
            holders.set(BuiltInRegistries.BLOCK, new IdentityHashMap<>());
            block = new BaseFumoBlock(BlockBehaviour.Properties.of().noCollision()
                    .setId(ResourceKey.create(Registries.BLOCK, Identifier.parse("test:fumo"))));
            hat = new MarisaHatBlock(BlockBehaviour.Properties.of()
                    .setId(ResourceKey.create(Registries.BLOCK, Identifier.parse("test:hat"))));
        } finally {
            holders.set(BuiltInRegistries.BLOCK, previous);
            frozen.setBoolean(BuiltInRegistries.BLOCK, wasFrozen);
        }
    }

    @Test
    void placementUsesAllSixteenAnglesAndWrapsWithoutAPlatformDependentFlip() throws Exception {
        FixturePlaceContext context = (FixturePlaceContext) unsafe.allocateInstance(FixturePlaceContext.class);
        for (int i = -16; i < 32; i++) {
            context.yaw = i * 22.5f;
            assertEquals(Math.floorMod(i, 16), block.getStateForPlacement(context).getValue(BaseFumoBlock.FACING_16).ordinal());
        }
    }

    @Test
    void sneakingAlignmentUsesAllSixteenLookAnglesAndWraps() throws Exception {
        FixtureLevel level = level(false);
        FixturePlayer player = player(level);
        var angles = new HashSet<Float>();
        for (int i = -16; i < 32; i++) {
            player.setYRot(i * 22.5f);
            assertTrue(FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP).consumesAction());
            // At player yaw 0 (looking south), the model's front must face back north.
            assertEquals(Math.floorMod(i, 16) * 22.5f, level.state.getValue(BaseFumoBlock.FACING_16).getYaw());
            angles.add(level.state.getValue(BaseFumoBlock.FACING_16).getYaw());
        }
        assertEquals(16, angles.size());
    }

    @Test
    void lookAlignmentSnapsToNearestAngleAndIgnoresVerticalLook() throws Exception {
        FixtureLevel level = level(false);
        FixturePlayer player = player(level);
        float[][] examples = {{0, 0}, {90, 90}, {180, 180}, {-90, 270},
                {10, 0}, {12, 22.5f}, {-10, 0}, {-12, 337.5f}, {359, 0}, {765, 45}, {-765, 315}};
        for (float[] example : examples) {
            player.setYRot(example[0]);
            player.setXRot(-80);
            FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP);
            assertEquals(example[1], level.state.getValue(BaseFumoBlock.FACING_16).getYaw());
            int writes = level.writes;
            player.setXRot(80);
            FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP);
            assertEquals(writes, level.writes, "Same horizontal look must retain direction without another block update");
        }
    }

    @Test
    void renderedFrontFacesThePlayerAndMatchesPlacementAtEveryAngle() throws Exception {
        FixtureLevel level = level(false);
        FixturePlayer player = player(level);
        FixturePlaceContext context = (FixturePlaceContext) unsafe.allocateInstance(FixturePlaceContext.class);
        for (int i = -32; i <= 32; i++) {
            // Include angles between the sixteen steps, including both sides of a rounding boundary.
            for (float offset : new float[]{0, 11.24f, 11.26f}) {
                context.yaw = i * 22.5f + offset;
                player.setYRot(context.yaw);
                FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP);
                assertSame(block.getStateForPlacement(context), level.state);
                float yaw = level.state.getValue(BaseFumoBlock.FACING_16).getYaw();
                // Same north-facing model front and -yaw rotation as FumoBlockEntityRenderer.
                Vector3f front = Axis.YP.rotationDegrees(-yaw).transform(new Vector3f(0, 0, -1));
                var look = player.getLookAngle();
                double dot = front.x() * look.x + front.y() * look.y + front.z() * look.z;
                assertTrue(dot < -.98, "Rendered front must face back toward the player at yaw " + context.yaw);
            }
        }
    }

    @Test
    void structureRotationsAndMirrorsRetainTheIntermediateDirections() {
        for (SixteenDirection direction : SixteenDirection.values()) {
            BlockState state = block.defaultBlockState().setValue(BaseFumoBlock.FACING_16, direction);
            assertEquals((direction.getYaw() + 90) % 360,
                    state.rotate(Rotation.CLOCKWISE_90).getValue(BaseFumoBlock.FACING_16).getYaw());
            assertSame(state, state.rotate(Rotation.CLOCKWISE_90).rotate(Rotation.COUNTERCLOCKWISE_90));
            assertSame(state, state.mirror(Mirror.FRONT_BACK).mirror(Mirror.FRONT_BACK));
            assertSame(state, state.mirror(Mirror.LEFT_RIGHT).mirror(Mirror.LEFT_RIGHT));
            assertEquals((360 - direction.getYaw()) % 360,
                    state.mirror(Mirror.FRONT_BACK).getValue(BaseFumoBlock.FACING_16).getYaw());
        }
    }

    @Test
    void existingStateNamesKeepTheirAnglesAndDoNotRenderASecondStaticModel() throws Exception {
        // Vanilla owns persistence/network synchronization of facing_16. The BE reads
        // that same state rather than relying on a second saved yaw that can go stale.
        var entity = (FumoBlockEntity) unsafe.allocateInstance(FumoBlockEntity.class);
        var stateField = BlockEntity.class.getDeclaredField("blockState");
        stateField.setAccessible(true);
        for (SixteenDirection direction : SixteenDirection.values()) {
            var restored = BaseFumoBlock.FACING_16.getValue(direction.getSerializedName()).orElseThrow();
            BlockState state = block.defaultBlockState().setValue(BaseFumoBlock.FACING_16, restored);
            stateField.set(entity, state);
            assertEquals(direction.getYaw(), entity.getYaw());
            assertTrue(state.hasBlockEntity());
            assertEquals(RenderShape.INVISIBLE, state.getRenderShape());
        }
    }

    @Test
    void oldChunksGetMissingEntitiesAtTheCorrectPositionsWithoutReplacingExistingOnes() throws Exception {
        FixtureLevel level = level(false);
        FixtureChunk chunk = (FixtureChunk) unsafe.allocateInstance(FixtureChunk.class);
        FixtureSection populated = (FixtureSection) unsafe.allocateInstance(FixtureSection.class);
        FixtureSection empty = (FixtureSection) unsafe.allocateInstance(FixtureSection.class);
        populated.state = block.defaultBlockState();
        empty.state = Blocks.AIR.defaultBlockState();
        chunk.sections = new LevelChunkSection[] { populated, empty };
        chunk.entities = new HashMap<>();
        chunk.fixtureLevel = level;
        FumoBlockEventHandlers.onChunkLoad(chunk, level, false);
        BlockPos expected = new BlockPos(-29, -60, 53);
        assertEquals(1, chunk.entities.size());
        assertTrue(chunk.entities.containsKey(expected));
        assertEquals(1, chunk.created);
        assertEquals(1, chunk.markedUnsaved);
        assertEquals(0, empty.reads, "Sections with no Fumo must not be scanned");
        BlockEntity existing = chunk.entities.get(expected);
        FumoBlockEventHandlers.onChunkLoad(chunk, level, false);
        assertSame(existing, chunk.entities.get(expected));
        assertEquals(1, chunk.created);
        assertEquals(1, chunk.markedUnsaved);

        level.client = true;
        chunk.entities.clear();
        chunk.markedUnsaved = 0;
        FumoBlockEventHandlers.onChunkLoad(chunk, level, false);
        assertTrue(chunk.entities.containsKey(expected));
        assertEquals(0, chunk.markedUnsaved, "Client repairs must not mark server storage dirty");
    }

    @Test
    void sneakingInteractionAlignsOnceAndConsumesBothHandPaths() throws Exception {
        FixtureLevel level = level(false);
        FixturePlayer player = player(level);
        player.setYRot(90);
        assertTrue(FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP).consumesAction());
        assertEquals(1, level.writes);
        assertEquals(90, level.state.getValue(BaseFumoBlock.FACING_16).getYaw());
        assertTrue(FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP).consumesAction());
        assertEquals(1, level.writes, "Repeated clicks at the same angle must not cycle the state");
        assertTrue(FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.OFF_HAND, BlockPos.ZERO, Direction.UP).consumesAction());
        assertEquals(1, level.writes, "The offhand must not rotate a second time or use its item");
    }

    @Test
    void normalClicksPassThroughAndClientsDoNotChangeAuthoritativeState() throws Exception {
        FixtureLevel level = level(false);
        FixturePlayer player = player(level);
        player.setYRot(90);
        player.sneaking = false;
        assertEquals(InteractionResult.PASS, FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP));
        assertEquals(0, level.writes);
        level.client = true;
        player.sneaking = true;
        assertEquals(InteractionResult.SUCCESS, FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP));
        assertEquals(0, level.writes);
    }

    @Test
    void spectatorAndAdventurePlayersCannotRotate() throws Exception {
        FixtureLevel level = level(false);
        FixturePlayer player = player(level);
        player.setYRot(90);
        player.canBuild = false;
        assertEquals(InteractionResult.FAIL, FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP));
        player.canBuild = true;
        player.spectator = true;
        assertEquals(InteractionResult.FAIL, FumoBlockEventHandlers.onRightClickBlock(player, InteractionHand.MAIN_HAND, BlockPos.ZERO, Direction.UP));
        assertEquals(0, level.writes);
    }

    @Test
    void marisaHatAlsoRotatesWithoutAcquiringTheFumoSound() throws Exception {
        FixtureLevel level = level(false);
        FixturePlayer player = player(level);
        player.setYRot(-45);
        level.state = hat.defaultBlockState();
        assertTrue(hat.useWithoutItem(level.state, level, BlockPos.ZERO, player, null).consumesAction());
        assertEquals(1, level.writes);
        assertEquals(315, level.state.getValue(BaseFumoBlock.FACING_16).getYaw());
        player.sneaking = false;
        assertTrue(hat.useWithoutItem(level.state, level, BlockPos.ZERO, player, null).consumesAction());
        assertEquals(1, level.writes);
    }

    private static FixtureLevel level(boolean client) throws Exception {
        FixtureLevel level = (FixtureLevel) unsafe.allocateInstance(FixtureLevel.class);
        level.state = block.defaultBlockState();
        level.client = client;
        return level;
    }

    private static FixturePlayer player(FixtureLevel level) throws Exception {
        FixturePlayer player = (FixturePlayer) unsafe.allocateInstance(FixturePlayer.class);
        player.fixtureLevel = level;
        player.sneaking = true;
        player.canBuild = true;
        return player;
    }

    // Only world storage and player inputs are fixtures; production block/event code
    // performs all rotations and permission checks. Constructors are never invoked.
    private static class FixtureLevel extends ServerLevel {
        BlockState state;
        boolean client;
        int writes;
        private FixtureLevel() { super(null, null, null, null, null, null, false, 0, List.of(), false); }
        @Override public boolean isClientSide() { return client; }
        @Override public BlockState getBlockState(BlockPos pos) { return state; }
        @Override public boolean setBlock(BlockPos pos, BlockState state, int flags) {
            this.state = state;
            writes++;
            return true;
        }
    }

    private static class FixturePlayer extends Player {
        FixtureLevel fixtureLevel;
        boolean sneaking;
        boolean canBuild;
        boolean spectator;
        private FixturePlayer() { super(null, new GameProfile(UUID.randomUUID(), "fumo-test")); }
        @Override public GameType gameMode() { return GameType.SURVIVAL; }
        @Override public Level level() { return fixtureLevel; }
        @Override public boolean isShiftKeyDown() { return sneaking; }
        @Override public boolean mayBuild() { return canBuild; }
        @Override public boolean isSpectator() { return spectator; }
    }

    private static class FixturePlaceContext extends BlockPlaceContext {
        float yaw;
        private FixturePlaceContext() { super(null, InteractionHand.MAIN_HAND, ItemStack.EMPTY, null); }
        @Override public float getRotation() { return yaw; }
    }

    private static class FixtureSection extends LevelChunkSection {
        BlockState state;
        int reads;
        private FixtureSection() { super((PalettedContainerFactory) null); }
        @Override public boolean maybeHas(Predicate<BlockState> predicate) { return predicate.test(state); }
        @Override public BlockState getBlockState(int x, int y, int z) {
            reads++;
            return x == 3 && y == 4 && z == 5 ? state : Blocks.AIR.defaultBlockState();
        }
    }

    private static class FixtureChunk extends LevelChunk {
        LevelChunkSection[] sections;
        Map<BlockPos, BlockEntity> entities;
        FixtureLevel fixtureLevel;
        int created;
        int markedUnsaved;
        private FixtureChunk() { super(null, new ChunkPos(-2, 3)); }
        @Override public LevelChunkSection[] getSections() { return sections; }
        @Override public ChunkPos getPos() { return new ChunkPos(-2, 3); }
        @Override public int getSectionYFromSectionIndex(int index) { return -4 + index; }
        @Override public Level getLevel() { return fixtureLevel; }
        @Override public void markUnsaved() { markedUnsaved++; }
        @Override public BlockEntity getBlockEntity(BlockPos pos, EntityCreationType creation) {
            if (creation == EntityCreationType.IMMEDIATE && !entities.containsKey(pos)) {
                try {
                    entities.put(pos.immutable(), (FumoBlockEntity) unsafe.allocateInstance(FumoBlockEntity.class));
                    created++;
                } catch (InstantiationException e) {
                    throw new AssertionError(e);
                }
            }
            return entities.get(pos);
        }
    }
}
