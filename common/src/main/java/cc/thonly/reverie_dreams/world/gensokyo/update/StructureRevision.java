package cc.thonly.reverie_dreams.world.gensokyo.update;

import cc.thonly.reverie_dreams.data.gensokyo.GensokyoStructureMetadata;
import cc.thonly.reverie_dreams.registry.BuiltInRegistryProviders;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;

/** Content revision remains independent of the mod release version. */
public record StructureRevision(int version, String hash, GensokyoStructureMetadata metadata) {
    public static StructureRevision load(ServerLevel level, Identifier template) {
        var metadata = BuiltInRegistryProviders.GENSOKYO_STRUCTURE_METADATA.get(template)
                .orElseThrow(() -> new IllegalStateException("Missing structure metadata " + template)).value();
        if (!metadata.contentSha256().matches("[a-f0-9]{64}")) {
            throw new IllegalStateException("Invalid structure content hash " + template);
        }
        return new StructureRevision(metadata.contentVersion(), metadata.contentSha256(), metadata);
    }

    public String marker(String anchor) {
        return "revision-v1:" + anchor + ":" + version + ":" + hash;
    }
}
