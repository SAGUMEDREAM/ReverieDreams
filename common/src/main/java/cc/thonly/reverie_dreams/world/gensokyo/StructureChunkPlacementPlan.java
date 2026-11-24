package cc.thonly.reverie_dreams.world.gensokyo;

import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockRotProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A deterministic XZ chunk-slice view of one transformed template placement.
 * The template itself is not split; pieces only bound each placement call.
 */
public final class StructureChunkPlacementPlan {
    public record Piece(int chunkX, int chunkZ, BoundingBox bounds, String completionKey) {
    }

    private final Identifier anchorId;
    private final BoundingBox bounds;
    private final List<Piece> pieces;
    private final String planIdentity;

    private StructureChunkPlacementPlan(
            Identifier anchorId,
            BoundingBox bounds,
            List<Piece> pieces,
            String planIdentity
    ) {
        this.anchorId = anchorId;
        this.bounds = bounds;
        this.pieces = List.copyOf(pieces);
        this.planIdentity = planIdentity;
    }

    public static StructureChunkPlacementPlan create(
            Identifier anchorId,
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos anchorPos
    ) {
        Objects.requireNonNull(anchorId, "anchorId");
        Objects.requireNonNull(template, "template");
        Objects.requireNonNull(settings, "settings");
        Objects.requireNonNull(anchorPos, "anchorPos");

        BoundingBox bounds = template.getBoundingBox(settings, anchorPos);
        String planIdentity = planIdentity(template, settings, anchorPos, bounds);

        List<Piece> pieces = new ArrayList<>();
        for (int chunkX = bounds.minX() >> 4; chunkX <= bounds.maxX() >> 4; chunkX++) {
            for (int chunkZ = bounds.minZ() >> 4; chunkZ <= bounds.maxZ() >> 4; chunkZ++) {
                int minX = Math.max(bounds.minX(), chunkX << 4);
                int minZ = Math.max(bounds.minZ(), chunkZ << 4);
                int maxX = Math.min(bounds.maxX(), (chunkX << 4) + 15);
                int maxZ = Math.min(bounds.maxZ(), (chunkZ << 4) + 15);
                BoundingBox pieceBounds = new BoundingBox(
                        minX,
                        bounds.minY(),
                        minZ,
                        maxX,
                        bounds.maxY(),
                        maxZ
                );
                pieces.add(new Piece(chunkX, chunkZ, pieceBounds, completionKey(anchorId, planIdentity, chunkX, chunkZ)));
            }
        }

        return new StructureChunkPlacementPlan(anchorId, bounds, pieces, planIdentity);
    }

    public Identifier anchorId() {
        return this.anchorId;
    }

    public BoundingBox bounds() {
        return this.bounds;
    }

    public List<Piece> pieces() {
        return this.pieces;
    }

    public String planIdentity() {
        return this.planIdentity;
    }

    private static String completionKey(Identifier anchorId, String planIdentity, int chunkX, int chunkZ) {
        MessageDigest digest = sha256();
        update(digest, anchorId.toString());
        update(digest, planIdentity);
        update(digest, chunkX);
        update(digest, chunkZ);
        return anchorId + "#" + chunkX + "," + chunkZ + ":" + hex(digest.digest(), 8);
    }

    private static String planIdentity(
            StructureTemplate template,
            StructurePlaceSettings settings,
            BlockPos anchorPos,
            BoundingBox bounds
    ) {
        MessageDigest digest = sha256();
        update(digest, template.getSize().getX());
        update(digest, template.getSize().getY());
        update(digest, template.getSize().getZ());
        update(digest, anchorPos.getX());
        update(digest, anchorPos.getY());
        update(digest, anchorPos.getZ());
        update(digest, bounds.minX());
        update(digest, bounds.minY());
        update(digest, bounds.minZ());
        update(digest, bounds.maxX());
        update(digest, bounds.maxY());
        update(digest, bounds.maxZ());
        update(digest, settings.getMirror().name());
        update(digest, settings.getRotation().name());
        BlockPos pivot = settings.getRotationPivot();
        update(digest, pivot == null ? 0 : pivot.getX());
        update(digest, pivot == null ? 0 : pivot.getY());
        update(digest, pivot == null ? 0 : pivot.getZ());
        update(digest, Boolean.toString(settings.isIgnoreEntities()));
        update(digest, Boolean.toString(settings.getKnownShape()));
        update(digest, Boolean.toString(settings.shouldFinalizeEntities()));
        update(digest, Boolean.toString(settings.shouldApplyWaterlogging()));
        for (var processor : settings.getProcessors()) {
            update(digest, processorIdentity(processor));
        }
        return hex(digest.digest(), 16);
    }

    private static String processorIdentity(StructureProcessor processor) {
        if (processor instanceof BlockRotProcessor blockRotProcessor) {
            return BlockRotProcessor.CODEC.codec()
                    .encodeStart(NbtOps.INSTANCE, blockRotProcessor)
                    .result()
                    .map(Tag::toString)
                    .orElseGet(() -> processor.getClass().getName());
        }
        return processor.getClass().getName();
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private static void update(MessageDigest digest, String value) {
        digest.update(value.getBytes(StandardCharsets.UTF_8));
        digest.update((byte) 0);
    }

    private static void update(MessageDigest digest, int value) {
        digest.update((byte) (value >>> 24));
        digest.update((byte) (value >>> 16));
        digest.update((byte) (value >>> 8));
        digest.update((byte) value);
    }

    private static String hex(byte[] bytes, int length) {
        StringBuilder builder = new StringBuilder(length * 2);
        for (int i = 0; i < Math.min(length, bytes.length); i++) {
            builder.append(Character.forDigit((bytes[i] >> 4) & 0x0F, 16));
            builder.append(Character.forDigit(bytes[i] & 0x0F, 16));
        }
        return builder.toString();
    }
}
