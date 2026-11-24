package cc.thonly.reverie_dreams.server.component;

import cc.thonly.reverie_dreams.component.BasePlayerComponentManager;
import cc.thonly.reverie_dreams.api.player.PlayerComponent;
import cc.thonly.reverie_dreams.api.player.PlayerComponentInitializer;
import cc.thonly.reverie_dreams.registry.content.PlayerComponentRegistry;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.player.Player;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServerPlayerComponentManagerTest {
    @TempDir Path directory;
    private static final Identifier VALUE_ID = Identifier.fromNamespaceAndPath("test", "value");

    @BeforeAll static void registerTestComponent() {
        if (PlayerComponentRegistry.get(VALUE_ID) == null) {
            PlayerComponentRegistry.register(VALUE_ID, TestComponent.class, new PlayerComponentInitializer<TestComponent>() {
                @Override public TestComponent create() { return new TestComponent(0); }
                @Override public Codec<TestComponent> getCodec() { return new TestComponent(0).getCodec(); }
            });
        }
    }

    @Test void unknownComponentsAndRootMetadataSurviveKnownComponentUpdates() throws Exception {
        var manager = manager();
        UUID id = UUID.randomUUID();
        String original = "{\"version\":1,\"meta\":{\"keep\":true},\"components\":{\"test:value\":7,\"missing:extension\":{\"nested\":[1,2,3]}}}";
        Files.writeString(manager.withPath(id), original);
        manager.loadAll();
        add(manager, id, 9);
        manager.saveAll();
        var saved = JsonParser.parseString(Files.readString(manager.withPath(id))).getAsJsonObject();
        assertTrue(saved.getAsJsonObject("meta").get("keep").getAsBoolean());
        assertEquals(9, saved.getAsJsonObject("components").get("test:value").getAsInt());
        assertEquals(JsonParser.parseString(original).getAsJsonObject().getAsJsonObject("components").get("missing:extension"),
                saved.getAsJsonObject("components").get("missing:extension"));
    }

    @Test void anUnknownOnlyPlayerIsNotRewrittenAsAnEmptySave() throws Exception {
        var manager = manager();
        UUID id = UUID.randomUUID();
        Files.writeString(manager.withPath(id), "{\"version\":1,\"components\":{\"missing:only\":42}}");
        manager.loadAll();
        manager.saveAll();
        var saved = JsonParser.parseString(Files.readString(manager.withPath(id))).getAsJsonObject();
        assertEquals(42, saved.getAsJsonObject("components").get("missing:only").getAsInt());
    }

    @Test void corruptAndFutureFilesAreBackedUpAndCannotBeOverwrittenByDefaults() throws Exception {
        for (String original : new String[]{"{broken json", "null", "{\"version\":99,\"components\":{\"test:value\":4}}"}) {
            var manager = manager();
            UUID id = UUID.randomUUID();
            Files.writeString(manager.withPath(id), original);
            manager.loadAll();
            add(manager, id, 0);
            manager.saveAll();
            assertEquals(original, Files.readString(manager.withPath(id)));
            assertEquals(original, Files.readString(directory.resolve(id + ".json.unreadable.bak")));
            assertTrue(manager.getDirtyPlayers().contains(id));
        }
    }

    @Test void knownDecodeErrorsIncludingPartialResultsNeverReplaceTheOriginal() throws Exception {
        for (String data : new String[]{"{}", "-1"}) {
            var manager = manager();
            UUID id = UUID.randomUUID();
            String original = "{\"version\":1,\"components\":{\"test:value\":" + data + "}}";
            Files.writeString(manager.withPath(id), original);
            manager.loadAll();
            add(manager, id, 0);
            manager.saveAll();
            assertEquals(original, Files.readString(manager.withPath(id)));
            assertTrue(manager.getDirtyPlayers().contains(id));
        }
    }

    @Test void migratesKnownLegacyDataWhileKeepingUnknownLegacyEntries() throws Exception {
        var manager = manager();
        UUID id = UUID.randomUUID();
        String original = "{\"" + TestComponent.class.getName() + "\":12,\"missing.legacy.Component\":{\"value\":3}}";
        Files.writeString(manager.withPath(id), original);
        manager.loadAll();
        manager.saveAll();
        var saved = JsonParser.parseString(Files.readString(manager.withPath(id))).getAsJsonObject();
        assertEquals(12, saved.getAsJsonObject("components").get("test:value").getAsInt());
        assertFalse(saved.getAsJsonObject("legacy_components").has(TestComponent.class.getName()));
        assertEquals(3, saved.getAsJsonObject("legacy_components").getAsJsonObject("missing.legacy.Component").get("value").getAsInt());
        var restored = manager();
        restored.loadAll();
        restored.saveAll();
        assertEquals(saved, JsonParser.parseString(Files.readString(restored.withPath(id))));
    }

    @Test
    void partialEncodingFailurePreservesTheCompletePreviousFileAndDirtyState() throws Exception {
        ServerPlayerComponentManager manager = manager();
        UUID id = UUID.randomUUID();
        TestComponent value = add(manager, id, 10);
        manager.saveAll();
        String original = Files.readString(manager.withPath(id));
        value.value = 20;
        value.fail = true;
        manager.markDirty(id);
        manager.flushDirty();
        assertEquals(original, Files.readString(manager.withPath(id)));
        assertTrue(manager.getDirtyPlayers().contains(id));
        value.fail = false;
        manager.saveAll();
        assertEquals(20, savedValue(manager, id));
        assertFalse(manager.getDirtyPlayers().contains(id));
    }

    @Test
    void failedWriteIsRetriedAfterBackoffWithoutOverwritingTheOldFile() throws Exception {
        ServerPlayerComponentManager manager = manager();
        UUID id = UUID.randomUUID();
        TestComponent value = add(manager, id, 1);
        manager.saveAll();
        Path blockedTemporary = manager.withPath(id).resolveSibling(id + ".json.tmp");
        Files.createDirectory(blockedTemporary);
        value.value = 2;
        manager.markDirty(id);
        manager.flushDirty();
        assertEquals(1, savedValue(manager, id));
        assertTrue(manager.getDirtyPlayers().contains(id));
        Files.delete(blockedTemporary);
        for (int i = 0; i < 19; i++) manager.flushDirty();
        assertEquals(1, savedValue(manager, id));
        manager.flushDirty();
        assertEquals(2, savedValue(manager, id));
        assertFalse(manager.getDirtyPlayers().contains(id));
    }

    @Test
    void saveAllBypassesBackoffAndKeepsOnlyFailedPlayersDirty() throws Exception {
        ServerPlayerComponentManager manager = manager();
        UUID goodId = UUID.randomUUID();
        UUID badId = UUID.randomUUID();
        add(manager, goodId, 3);
        TestComponent bad = add(manager, badId, 4);
        bad.fail = true;
        manager.saveAll();
        assertEquals(3, savedValue(manager, goodId));
        assertFalse(Files.exists(manager.withPath(badId)));
        assertEquals(Set.of(badId), manager.getDirtyPlayers());
        bad.fail = false;
        manager.saveAll();
        assertEquals(4, savedValue(manager, badId));
        assertTrue(manager.getDirtyPlayers().isEmpty());
    }

    @Test
    void changesMarkedDuringEncodingAreNotClearedAfterTheWrite() throws Exception {
        ServerPlayerComponentManager manager = manager();
        UUID id = UUID.randomUUID();
        TestComponent value = add(manager, id, 5);
        value.onEncode = () -> manager.markDirty(id);
        manager.flushDirty();
        assertEquals(5, savedValue(manager, id));
        assertTrue(manager.getDirtyPlayers().contains(id));
        value.onEncode = () -> {};
        manager.flushDirty();
        assertTrue(manager.getDirtyPlayers().isEmpty());
    }

    @Test
    void refusesReloadWhileAForcedSaveStillFails() throws Exception {
        ServerPlayerComponentManager manager = manager();
        UUID id = UUID.randomUUID();
        TestComponent value = add(manager, id, 7);
        value.fail = true;
        IllegalStateException error = assertThrows(IllegalStateException.class, () -> manager.onLoad(null));
        assertTrue(error.getMessage().contains("unsaved"));
        assertSame(value, manager.getDataList().get(id).get(VALUE_ID));
        assertTrue(manager.getDirtyPlayers().contains(id));
    }

    @Test
    void uninitializedStorageDoesNotLosePendingChanges() {
        ServerPlayerComponentManager manager = new ServerPlayerComponentManager();
        UUID id = UUID.randomUUID();
        manager.markDirty(id);
        manager.flushDirty();
        assertTrue(manager.getDirtyPlayers().contains(id));
    }

    private ServerPlayerComponentManager manager() throws Exception {
        ServerPlayerComponentManager manager = new ServerPlayerComponentManager();
        set(manager, "baseSavePath", directory);
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.EMPTY);
        set(manager, "registryOps", ops);
        return manager;
    }

    private static void set(ServerPlayerComponentManager manager, String name, Object value) throws Exception {
        Field field = BasePlayerComponentManager.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(manager, value);
    }

    @SuppressWarnings("rawtypes")
    private static TestComponent add(ServerPlayerComponentManager manager, UUID id, int initial) {
        TestComponent value = new TestComponent(initial);
        Map<Identifier, PlayerComponent> components = new HashMap<>();
        components.put(VALUE_ID, value);
        manager.getDataList().put(id, components);
        manager.markDirty(id);
        return value;
    }

    private static int savedValue(ServerPlayerComponentManager manager, UUID id) throws Exception {
        return JsonParser.parseString(Files.readString(manager.withPath(id))).getAsJsonObject()
                .getAsJsonObject("components").get(VALUE_ID.toString()).getAsInt();
    }

    private static class TestComponent implements PlayerComponent<TestComponent> {
        int value;
        boolean fail;
        Runnable onEncode = () -> {};

        TestComponent(int value) { this.value = value; }
        @Override public void onLoad() {}
        @Override public void setPlayer(Player player) {}
        @Override public Player getPlayer() { return null; }
        @Override public Codec<TestComponent> getCodec() {
            return Codec.INT.flatXmap(value -> value < 0
                    ? DataResult.error(() -> "deliberate partial decoding failure", new TestComponent(0))
                    : DataResult.success(new TestComponent(value)), component -> {
                component.onEncode.run();
                return component.fail ? DataResult.error(() -> "deliberate partial encoding failure", component.value)
                        : DataResult.success(component.value);
            });
        }
    }
}
