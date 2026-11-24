package cc.thonly.reverie_dreams.data.npc;

import cc.thonly.reverie_dreams.data.skin.SkinType;
import cc.thonly.reverie_dreams.registry.impl.RegistryProvider;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.JsonOps;
import net.minecraft.SharedConstants;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoleRegistryLookupTest {
    private static final Identifier BUILTIN = Identifier.parse("test:builtin_role");
    private static final Identifier ALIAS = Identifier.parse("test:old_role_id");
    private static final Identifier DATA_ONLY = Identifier.parse("test:data_role");
    private static final Identifier MISSING = Identifier.parse("test:missing_role");

    @BeforeAll
    static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void exactLookupResolvesAliasesWithoutUsingTheConfiguredDefault() throws Exception {
        RegistryProvider<String> registry = registry("lookup_contract");
        register(registry, BUILTIN, "fallback");
        register(registry, DATA_ONLY, "selected");
        registry.addAliaName(ALIAS, DATA_ONLY);
        registry.defaultId(BUILTIN);
        registry.freeze();

        assertEquals(Optional.of("selected"), registry.findValue(ALIAS));
        assertTrue(registry.findValue(MISSING).isEmpty());
        assertTrue(registry.findValue(null).isEmpty());
        assertEquals("selected", registry.getValueOrDefault(ALIAS));
        assertEquals("fallback", registry.getValueOrDefault(MISSING));
        assertEquals("fallback", registry.getValue(MISSING));
        assertEquals("fallback", registry.getValueOrDefault(null));
    }

    @Test
    void spawnableAndDataOnlyRolesRoundTripWithTheSameIdentifierFormat() throws Exception {
        RegistryProvider<NPCRoleType> spawnable = registry("spawnable_roles");
        RegistryProvider<NPCSimpleRoleType> simple = registry("data_roles");
        SkinType skin = new SkinType(Identifier.parse("test:skin"));
        NPCRoleType builtin = new NPCRoleType(BUILTIN, skin);
        NPCSimpleRoleType definition = new NPCSimpleRoleType(DATA_ONLY, skin);
        register(spawnable, BUILTIN, builtin);
        spawnable.addAliaName(ALIAS, BUILTIN);
        spawnable.defaultId(BUILTIN);
        register(simple, DATA_ONLY, definition);
        var codec = RoleTypeCodec.byId(spawnable, simple);

        assertSame(builtin, codec.parse(JsonOps.INSTANCE, new JsonPrimitive(ALIAS.toString())).getOrThrow());
        assertEquals(new JsonPrimitive(BUILTIN.toString()), codec.encodeStart(JsonOps.INSTANCE, builtin).getOrThrow());
        var encoded = codec.encodeStart(JsonOps.INSTANCE, definition).getOrThrow();
        assertEquals(new JsonPrimitive(DATA_ONLY.toString()), encoded);
        assertSame(definition, codec.parse(JsonOps.INSTANCE, encoded).getOrThrow());
        assertSame(skin, definition.getSkinType());
        assertNull(definition.get(), "A data role has no spawn entry");
        assertTrue(codec.parse(JsonOps.INSTANCE, new JsonPrimitive(MISSING.toString())).error().isPresent());
        assertTrue(codec.encodeStart(JsonOps.INSTANCE, NPCRoleType.empty()).error().isPresent());

        NPCSimpleRoleType collision = new NPCSimpleRoleType(BUILTIN, skin);
        register(simple, BUILTIN, collision);
        assertTrue(codec.encodeStart(JsonOps.INSTANCE, collision).error().isPresent());
        assertSame(builtin, codec.parse(JsonOps.INSTANCE, new JsonPrimitive(BUILTIN.toString())).getOrThrow());
    }

    @Test
    void emptyRoleCannotReachEntityRegistration() {
        assertThrows(IllegalStateException.class, () -> NPCRoleType.empty().build());
    }

    private static <T> void register(RegistryProvider<T> registry, Identifier id, T value) {
        registry.register(registry.createKey(id), value, RegistrationInfo.BUILT_IN);
    }

    @SuppressWarnings("unchecked")
    private static <T> RegistryProvider<T> registry(String name) throws ReflectiveOperationException {
        ResourceKey<Registry<T>> key = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("test", name));
        String provider = System.getProperty("reverie_dreams.registryTestProvider");
        if (provider == null) {
            return new TestRegistry<>(key);
        }
        return (RegistryProvider<T>) Class.forName(provider).asSubclass(RegistryProvider.class)
                .getConstructor(ResourceKey.class).newInstance(key);
    }

    private static final class TestRegistry<T> extends RegistryProvider<T> {
        private final Map<Identifier, Identifier> aliases = new HashMap<>();

        private TestRegistry(ResourceKey<Registry<T>> key) {
            super(key);
        }

        @Override
        public void addAliaName(Identifier from, Identifier to) {
            this.aliases.put(from, to);
        }

        @Override
        public Identifier resolveId(Identifier name) {
            return this.aliases.getOrDefault(name, name);
        }

        @Override
        public ResourceKey<T> resolveKey(ResourceKey<T> key) {
            return ResourceKey.create(key.registryKey(), this.resolveId(key.identifier()));
        }
    }
}
