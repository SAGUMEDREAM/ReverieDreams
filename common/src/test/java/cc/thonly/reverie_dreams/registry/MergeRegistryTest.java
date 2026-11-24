package cc.thonly.reverie_dreams.registry;

import cc.thonly.reverie_dreams.registry.impl.MergeRegistry;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MergeRegistryTest {
    @Test
    void returnsReferenceFromTheRegistryThatOwnsTheValue() {
        ResourceKey<Registry<String>> key = ResourceKey.createRegistryKey(Identifier.parse("test:source"));
        var first = new MappedRegistry<String>(key, Lifecycle.stable());
        var second = new MappedRegistry<String>(key, Lifecycle.stable());
        String value = new String("second registry value");
        var expected = second.register(ResourceKey.create(key, Identifier.parse("test:value")), value, RegistrationInfo.BUILT_IN);
        var merged = new MergeRegistry<String>(key, first, second) {};
        assertSame(expected, merged.wrapAsHolder(value));
    }

    @Test
    void wrapsAnUnregisteredValueAsADirectHolder() {
        ResourceKey<Registry<String>> key = ResourceKey.createRegistryKey(Identifier.parse("test:source"));
        var merged = new MergeRegistry<String>(key) {};
        String value = new String("unregistered value");
        var holder = merged.wrapAsHolder(value);
        assertSame(value, holder.value());
        assertTrue(holder.unwrapKey().isEmpty());
    }
}
