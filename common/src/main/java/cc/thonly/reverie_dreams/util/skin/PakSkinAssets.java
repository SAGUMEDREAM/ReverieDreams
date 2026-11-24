package cc.thonly.reverie_dreams.util.skin;

import cc.thonly.pak.ClassResourceSource;
import cc.thonly.pak.PakDecoder;
import cc.thonly.pak.PakException;
import cc.thonly.pak.NativePak;
import cc.thonly.reverie_dreams.client.pak.PakSkinPacks;
import cc.thonly.reverie_dreams.client.pak.SkinCatalog;
import cc.thonly.reverie_dreams.client.pak.SkinPackManifest;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Reads decrypted skin PNGs from the encrypted assets shipped in this mod's container.
 */
public final class PakSkinAssets {
    private record State(ClassResourceSource container, SkinPackManifest manifest, PakDecoder decoder) {
    }

    private static volatile State state;

    public static Optional<byte[]> decrypt(String skin) throws IOException {
        State current = state();
        Identifier id = Identifier.fromNamespaceAndPath(SkinPackManifest.OWNER,
                "textures/entity/player/skin/" + skin + ".png");
        SkinPackManifest.Entry entry = current.manifest().entries().get(id);
        if (entry == null)
            return Optional.empty();
        try (InputStream in = current.container().open(entry.packedPath())) {
            if (in == null)
                throw new PakException("Missing PAK skin resource: " + entry.packedPath());
            return Optional.of(current.decoder().decrypt(in, entry.asset()));
        }
    }

    private static State state() throws IOException {
        State current = state;
        if (current != null)
            return current;
        synchronized (PakSkinAssets.class) {
            if (state != null)
                return state;
            ClassResourceSource container = ClassResourceSource.of(PakSkinPacks.class);
            PakDecoder decoder = new NativePak(container);
            SkinPackManifest manifest = SkinCatalog.load(container::open, decoder);
            state = new State(container, manifest, decoder);
            return state;
        }
    }

    private PakSkinAssets() {
    }
}
