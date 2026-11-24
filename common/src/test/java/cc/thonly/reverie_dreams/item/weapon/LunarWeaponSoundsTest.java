package cc.thonly.reverie_dreams.item.weapon;

import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

class LunarWeaponSoundsTest {
    @BeforeAll static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test void everyGunUsesDistinctBoundedVanillaLayers() {
        var profiles = new HashSet<>();
        for (LunarWeaponType type : LunarWeaponType.values()) {
            var profile = LunarWeaponSounds.profile(type);
            assertNotNull(profile);
            assertTrue(profile.size() >= 1 && profile.size() <= 3);
            assertTrue(profiles.add(profile), "Two guns share the exact same firing sound");
            for (var layer : profile) {
                assertEquals("minecraft", layer.event().getNamespace());
                assertNotNull(BuiltInRegistries.SOUND_EVENT.getValue(layer.event()), layer.event().toString());
                assertTrue(layer.volume() > 0 && layer.volume() <= 1.5f);
                assertTrue(layer.pitch() >= .5f && layer.pitch() <= 2);
            }
        }
    }

    @Test void automaticSmgIsQuieterAndSniperHasItsOwnTailLayer() {
        float smg = totalVolume(LunarWeaponType.SMG), rifle = totalVolume(LunarWeaponType.RIFLE);
        assertTrue(smg < rifle);
        assertTrue(totalVolume(LunarWeaponType.SNIPER) > rifle);
        assertTrue(LunarWeaponSounds.profile(LunarWeaponType.SNIPER).stream()
                .anyMatch(layer -> layer.event().getPath().equals("entity.firework_rocket.large_blast_far")));
    }

    private static float totalVolume(LunarWeaponType type) {
        float sum = 0;
        for (var layer : LunarWeaponSounds.profile(type)) sum += layer.volume();
        return sum;
    }
}
