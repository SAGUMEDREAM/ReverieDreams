package cc.thonly.reverie_dreams.data.gensokyo;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import java.util.List;

public record GensokyoStructureMetadata(
        int version,
        int contentVersion,
        int actorsVersion,
        String contentSha256,
        String sourceSha256,
        BlockPos size,
        List<FunctionPoint> functionPoints,
        List<Integer> exteriorGroundColumns,
        int exteriorGroundY,
        List<BlockPos> cherryRoots,
        String templateSha256
) {
    public GensokyoStructureMetadata {
        if (!contentSha256.matches("[a-f0-9]{64}") || !sourceSha256.matches("[a-f0-9]{64}")) {
            throw new IllegalArgumentException("Structure hashes must be SHA-256");
        }
        if (size.getX() <= 0 || size.getY() <= 0 || size.getZ() <= 0) {
            throw new IllegalArgumentException("Structure dimensions must be positive");
        }
        functionPoints = List.copyOf(functionPoints);
        exteriorGroundColumns = List.copyOf(exteriorGroundColumns);
        cherryRoots = List.copyOf(cherryRoots);
        if (functionPoints.stream().map(FunctionPoint::id).distinct().count() != functionPoints.size()) {
            throw new IllegalArgumentException("Duplicate structure function point ID");
        }
    }

    public static final Codec<GensokyoStructureMetadata> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(1, 1).fieldOf("version").forGetter(GensokyoStructureMetadata::version),
            Codec.intRange(1, Integer.MAX_VALUE).fieldOf("content_version").forGetter(GensokyoStructureMetadata::contentVersion),
            Codec.INT.fieldOf("actors_version").forGetter(GensokyoStructureMetadata::actorsVersion),
            Codec.STRING.fieldOf("content_sha256").forGetter(GensokyoStructureMetadata::contentSha256),
            Codec.STRING.fieldOf("source_sha256").forGetter(GensokyoStructureMetadata::sourceSha256),
            BlockPos.CODEC.fieldOf("size").forGetter(GensokyoStructureMetadata::size),
            FunctionPoint.CODEC.listOf().optionalFieldOf("function_points", List.of()).forGetter(GensokyoStructureMetadata::functionPoints),
            Codec.INT.listOf().optionalFieldOf("exterior_ground_columns", List.of()).forGetter(GensokyoStructureMetadata::exteriorGroundColumns),
            Codec.INT.optionalFieldOf("exterior_ground_y", 0).forGetter(GensokyoStructureMetadata::exteriorGroundY),
            BlockPos.CODEC.listOf().optionalFieldOf("cherry_roots", List.of()).forGetter(GensokyoStructureMetadata::cherryRoots),
            Codec.STRING.optionalFieldOf("template_sha256", "").forGetter(GensokyoStructureMetadata::templateSha256)
    ).apply(instance, GensokyoStructureMetadata::new));

    public GensokyoStructureMetadata withFingerprint(String fingerprint) {
        String contentHash = templateSha256.isEmpty() || templateSha256.equals(fingerprint)
                ? contentSha256 : fingerprint;
        return new GensokyoStructureMetadata(version, contentVersion, actorsVersion, contentHash,
                sourceSha256, size, functionPoints, exteriorGroundColumns, exteriorGroundY, cherryRoots, fingerprint);
    }

    public record FunctionPoint(
            String id,
            BlockPos pos
    ) {
        public static final Codec<FunctionPoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("id").forGetter(FunctionPoint::id),
                BlockPos.CODEC.fieldOf("pos").forGetter(FunctionPoint::pos)
        ).apply(instance, FunctionPoint::new));
    }
}
