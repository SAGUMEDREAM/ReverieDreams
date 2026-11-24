package cc.thonly.reverie_dreams.world.gensokyo.storage;

import cc.thonly.reverie_dreams.mixin.world.RegionFileZstdMixin;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import org.junit.jupiter.api.Test;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.InputStream;
import java.lang.classfile.ClassFile;
import java.lang.classfile.ClassModel;
import java.lang.classfile.instruction.InvokeInstruction;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegionFileMixinTargetTest {
    @Test
    void decoderRedirectCoversBothInlineAndExternalCallSites() throws Exception {
        var handler = RegionFileZstdMixin.class.getDeclaredMethod("reverie_dreams$decompressChunk",
                RegionFile.class, ChunkPos.class, byte.class, InputStream.class);
        Redirect redirect = handler.getAnnotation(Redirect.class);
        Set<String> selectedMethods = Set.of(redirect.method());
        ClassModel region;
        try (InputStream bytes = RegionFile.class.getResourceAsStream("RegionFile.class")) {
            assertNotNull(bytes);
            region = ClassFile.of().parse(bytes.readAllBytes());
        }
        Set<String> callers = new HashSet<>();
        int selectedCallSites = 0;
        for (var method : region.methods()) {
            if (method.code().isEmpty()) {
                continue;
            }
            for (var instruction : method.code().get()) {
                if (instruction instanceof InvokeInstruction call
                        && call.owner().equals(region.thisClass()) && call.name().equalsString("createChunkInputStream")) {
                    callers.add(method.methodName().stringValue());
                    if (selectedMethods.contains(method.methodName().stringValue())) {
                        selectedCallSites++;
                    }
                }
            }
        }
        assertEquals(Set.of("getChunkDataInputStream", "createExternalChunkInputStream"), callers);
        assertEquals(callers, selectedMethods, "Both .mca and .mcc decoder paths must be intercepted");
        assertEquals(2, selectedCallSites);
        assertTrue(selectedCallSites >= redirect.require(), "Injection minimum must hold inside selected methods");
    }
}
