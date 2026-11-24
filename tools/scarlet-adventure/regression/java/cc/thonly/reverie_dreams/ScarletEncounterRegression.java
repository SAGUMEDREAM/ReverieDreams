package cc.thonly.reverie_dreams;

import cc.thonly.reverie_dreams.entity.MaidYousei;
import cc.thonly.reverie_dreams.world.gensokyo.adventure.ScarletAdventureState;
import cc.thonly.reverie_dreams.world.gensokyo.adventure.ScarletChestLoot;
import cc.thonly.reverie_dreams.world.dimension.RDBuiltinLevels;
import com.google.gson.GsonBuilder;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.TicketStorage;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BooleanSupplier;

/** Exercises the ordinary encounter tick and DH's actual internal load/release calls. */
public final class ScarletEncounterRegression implements FabricClientGameTest {
    private static final BlockPos ENTRANCE_CHEST = new BlockPos(5019, 1056, 7153);
    private final List<String> checks = new ArrayList<>();
    private TestSingleplayerContext world;
    private ClientGameTestContext context;

    @Override
    public void runTest(ClientGameTestContext context) {
        this.context = context;
        Throwable failure = null;
        try {
            world = context.worldBuilder().create();
            world.getServer().runCommand("difficulty normal");
            world.getServer().runCommand("gamemode creative @p");
            context.runOnClient(client -> {
                client.options.renderDistance().set(5);
                client.options.pauseOnLostFocus = false;
                client.options.broadcastOptions();
            });
            world.getServer().runCommand("execute in reverie_dreams:gensokyo run tp @p 5006.5 1056 7152.5 -90 0");
            await(() -> world.getServer().computeOnServer(server -> {
                var level = level(server);
                var state = level.getDataStorage().computeIfAbsent(ScarletAdventureState.TYPE);
                return state.has("mob:maid_01") && state.has("mob:maid_15")
                        && state.has("mob:maid_16") && state.has("mob:maid_17")
                        && level.getBlockEntity(ENTRANCE_CHEST) instanceof ChestBlockEntity chest
                        && ScarletChestLoot.key("supply").equals(chest.getLootTable());
            }), "entrance approach activates four live guard points and installs the visible supply chest without forceload");
            world.getServer().runOnServer(server -> {
                var level = level(server);
                var guards = level.getEntitiesOfClass(MaidYousei.class, new AABB(4940, 1051, 7130, 5031, 1064, 7170),
                        e -> e.isAlive() && e.entityTags().stream().anyMatch(tag -> tag.startsWith("rd_scarlet_point:")));
                check(guards.size() >= 4, "maid bindings have actual living mobs on the entrance route");
                ChestBlockEntity chest = (ChestBlockEntity) level.getBlockEntity(ENTRANCE_CHEST);
                chest.unpackLootTable(level.players().getFirst());
                check(!chest.isEmpty(), "entrance supply table produces real items");
                for (int i = 0; i < chest.getContainerSize(); i++) chest.setItem(i, ItemStack.EMPTY);
                chest.setChanged();
            });
            context.waitTicks(40);
            world.getServer().runOnServer(server -> {
                var level = level(server);
                var chest = (ChestBlockEntity) level.getBlockEntity(ENTRANCE_CHEST);
                check(chest.getLootTable() == null && chest.isEmpty(), "spent entrance reward does not refill");
                level.setBlockAndUpdate(ENTRANCE_CHEST, Blocks.AIR.defaultBlockState());
            });
            context.waitTicks(40);
            world.getServer().runOnServer(server ->
                    check(level(server).getBlockState(ENTRANCE_CHEST).isAir(), "collected and removed reward chest is not recreated"));
            checkDhTicketOverlap();
        } catch (Throwable error) {
            failure = error;
            error.printStackTrace();
        } finally {
            if (world != null) {
                try { closeWorld(); }
                catch (Throwable error) { if (failure == null) failure = error; else failure.addSuppressed(error); }
            }
            try {
                Path output = Path.of(System.getProperty("reverie.building.evidence"));
                Files.createDirectories(output);
                Files.writeString(output.resolve("regression.json"), new GsonBuilder().setPrettyPrinting().create().toJson(
                        Map.of("checks", checks, "status", failure == null ? "PASS" : "FAIL",
                                "failure", failure == null ? "" : failure.toString())));
            } catch (Exception error) { throw new RuntimeException(error); }
        }
        if (failure != null) throw new AssertionError("Scarlet and DH regression failed", failure);
    }

    private void checkDhTicketOverlap() throws Exception {
        var loader = net.fabricmc.loader.api.FabricLoader.getInstance();
        check(loader.isModLoaded("c2me") && loader.isModLoaded("distanthorizons"), "DH and C2ME are loaded");
        Class<?> wrapperType = Class.forName("com.seibel.distanthorizons.common.wrappers.world.ServerLevelWrapper");
        Class<?> generatorType = Class.forName("com.seibel.distanthorizons.common.wrappers.worldGeneration.DhChunkGenerator");
        Class<?> internalType = Class.forName("com.seibel.distanthorizons.common.wrappers.worldGeneration.DhInternalServerGenerator");
        var require = internalType.getDeclaredMethod("lambda$requestChunkFromServerAsync$0", ChunkPos.class);
        var release = internalType.getDeclaredMethod("lambda$releaseChunkFromServerAsync$0", ServerLevel.class, ChunkPos.class, CompletableFuture.class);
        var ticket = internalType.getDeclaredField("DH_SERVER_GEN_TICKET");
        var storage = ServerChunkCache.class.getDeclaredField("ticketStorage");
        require.setAccessible(true);
        release.setAccessible(true);
        ticket.setAccessible(true);
        storage.setAccessible(true);
        var chunk = new ChunkPos(620, 535);
        await(() -> world.getServer().computeOnServer(server -> {
            try { return wrapperType.getMethod("getDhLevel").invoke(wrapperType.getMethod("getWrapper", ServerLevel.class).invoke(null, level(server))) != null; }
            catch (Exception error) { throw new RuntimeException(error); }
        }), "DH binds the live Gensokyo dimension");
        CompletableFuture<?>[] requests = world.getServer().computeOnServer(server -> {
            var level = level(server);
            Object wrapper = wrapperType.getMethod("getWrapper", ServerLevel.class).invoke(null, level);
            Object dhLevel = wrapperType.getMethod("getDhLevel").invoke(wrapper);
            Object generator = generatorType.getConstructor(Class.forName("com.seibel.distanthorizons.core.level.IDhServerLevel")).newInstance(dhLevel);
            Object internal = generatorType.getField("internalServerGenerator").get(generator);
            try {
                var a = (CompletableFuture<?>) require.invoke(internal, chunk);
                var b = (CompletableFuture<?>) require.invoke(internal, chunk);
                var tickets = (TicketStorage) storage.get(level.getChunkSource());
                Object dhTicket = ticket.get(null);
                release.invoke(internal, level, chunk, new CompletableFuture<>());
                check(tickets.getTickets(chunk.pack()).stream().anyMatch(t -> t.getType().equals(dhTicket)),
                        "first DH release retains the ticket held by the overlapping batch");
                return new CompletableFuture<?>[]{a, b, CompletableFuture.completedFuture(generator)};
            } catch (Throwable error) {
                generatorType.getMethod("close").invoke(generator);
                throw error;
            }
        });
        Object generator = requests[2].join();
        try {
            await(() -> requests[0].isDone() && requests[1].isDone(), "overlapping chunk requests complete");
            check(requests[0].join() != null && requests[1].join() != null, "neither overlapping request fails as Unloaded chunk");
        } finally {
            world.getServer().runOnServer(server -> {
                var level = level(server);
                Object internal = generatorType.getField("internalServerGenerator").get(generator);
                release.invoke(internal, level, chunk, new CompletableFuture<>());
                var tickets = (TicketStorage) storage.get(level.getChunkSource());
                check(tickets.getTickets(chunk.pack()).stream().noneMatch(t -> t.getType().equals(uncheckedGet(ticket))),
                        "last DH release removes its ticket with no leak");
                generatorType.getMethod("close").invoke(generator);
            });
        }
    }

    private static Object uncheckedGet(java.lang.reflect.Field field) {
        try { return field.get(null); }
        catch (IllegalAccessException e) { throw new IllegalStateException(e); }
    }

    private ServerLevel level(MinecraftServer server) {
        return server.getLevel(RDBuiltinLevels.getGensokyo());
    }

    private void await(BooleanSupplier condition, String description) {
        for (int i = 0; i < 450; i++) {
            if (condition.getAsBoolean()) {
                check(true, description);
                return;
            }
            context.waitTicks(4);
        }
        throw new AssertionError("Timed out: " + description);
    }

    private void check(boolean result, String description) {
        if (!result) throw new AssertionError(description);
        checks.add(description);
    }

    private void closeWorld() throws ReflectiveOperationException {
        // DH and Fabric's test phase gate need the existing detached shutdown sequence.
        var server = world.getServer().computeOnServer(s -> s);
        context.runOnClient(client -> {
            var field = net.minecraft.client.Minecraft.class.getDeclaredField("singleplayerServer");
            field.setAccessible(true);
            field.set(client, null);
            var saving = net.minecraft.network.chat.Component.translatable("menu.savingLevel");
            if (client.level != null) client.level.disconnect(saving);
            client.disconnect(new net.minecraft.client.gui.screens.GenericMessageScreen(saving), false);
            client.setScreen(new net.minecraft.client.gui.screens.TitleScreen());
        });
        context.waitFor(client -> client.level == null, 1200);
        world.getServer().runOnServer(s -> s.halt(false));
        context.waitFor(client -> server.isShutdown(), 1200);
        context.waitTicks(2);
        context.setScreen(net.minecraft.client.gui.screens.TitleScreen::new);
    }
}
