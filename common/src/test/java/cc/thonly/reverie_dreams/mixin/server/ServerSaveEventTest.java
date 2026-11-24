package cc.thonly.reverie_dreams.mixin.server;

import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static org.junit.jupiter.api.Assertions.*;

class ServerSaveEventTest {
    @Test void saveEventsSurroundTheVanillaMethodRatherThanSharingItsHead() throws Exception {
        Class<?>[] signature = {boolean.class, boolean.class, boolean.class, CallbackInfoReturnable.class};
        Inject before = MinecraftServerMixin.class.getDeclaredMethod("startSaveChunks", signature).getAnnotation(Inject.class);
        Inject after = MinecraftServerMixin.class.getDeclaredMethod("endSaveChunks", signature).getAnnotation(Inject.class);
        assertNotNull(before);
        assertNotNull(after);
        assertArrayEquals(new String[]{"saveAllChunks"}, before.method());
        assertArrayEquals(before.method(), after.method());
        assertEquals("HEAD", before.at()[0].value());
        assertEquals("RETURN", after.at()[0].value());
    }
}
