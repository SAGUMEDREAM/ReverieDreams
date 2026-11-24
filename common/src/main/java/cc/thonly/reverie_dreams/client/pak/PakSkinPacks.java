package cc.thonly.reverie_dreams.client.pak;

import cc.thonly.reverie_dreams.ReverieDreams;

import cc.thonly.pak.ClassResourceSource;
import cc.thonly.pak.NativePak;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.repository.RepositorySource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * Client-only registration. The pack exists only when an encrypted catalog is provisioned.
 */
public final class PakSkinPacks {
    public static final String PACK_ID = ReverieDreams.MOD_ID + ":gensokyo_skins_pak";
    private static final Logger LOGGER = LoggerFactory.getLogger("reverie_dreams/pak");
    public static final RepositorySource SOURCE = consumer -> {
        try {
            ClassResourceSource container = ClassResourceSource.of(PakSkinPacks.class);
            PakSkinPack.Source source = container::open;
            try (InputStream catalog = source.open(SkinPackManifest.CATALOG)) {
                if (catalog == null)
                    return;
            }
            var decoder = new NativePak(container);
            var location = new PackLocationInfo(PACK_ID, Component.literal("Gensokyo PAK skins"), PackSource.BUILT_IN, Optional.empty());
            var supplier = new Pack.ResourcesSupplier() {
                @Override
                public PackResources openPrimary(PackLocationInfo info) {
                    return new PakSkinPack(info, source, decoder);
                }

                @Override
                public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                    return openPrimary(info);
                }
            };
            Pack pack = Pack.readMetaAndCreate(location, supplier, PackType.CLIENT_RESOURCES,
                    new PackSelectionConfig(true, Pack.Position.BOTTOM, true));
            if (pack != null)
                consumer.accept(pack);
        } catch (IOException e) {
            LOGGER.error("Cannot register the PAK skin resource pack: {}", e.getMessage());
        }
    };

    private PakSkinPacks() {
    }

}
