package cc.thonly.reverie_dreams.networking;

import com.mojang.serialization.Codec;
import net.minecraft.core.HolderLookup;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;

class NPCSyncSnapshotTest {
    private final HolderLookup.Provider registries = HolderLookup.Provider.create(Stream.empty());

    @Test void successfulSnapshotRetainsItsFields() {
        var tag = NPCSyncSnapshot.encode(UUID.randomUUID(), registries, out -> out.putString("NPCOwner", "fixture owner"));
        assertNotNull(tag);
        assertEquals("fixture owner", tag.getStringOr("NPCOwner", ""));
    }

    @Test void thrownSerializationFailureDoesNotReturnAnEmptyOrPartialSnapshot() {
        assertNull(NPCSyncSnapshot.encode(UUID.randomUUID(), registries, out -> {
            out.putString("NPCOwner", "partial");
            throw new IllegalStateException("fixture serialization failure");
        }));
    }

    @Test void codecReportedFailureAlsoRejectsTheSnapshot() {
        assertNull(NPCSyncSnapshot.encode(UUID.randomUUID(), registries, out -> {
            out.putString("NPCOwner", "partial");
            out.store("Invalid", Codec.intRange(1, 2), 10);
        }));
    }
}
