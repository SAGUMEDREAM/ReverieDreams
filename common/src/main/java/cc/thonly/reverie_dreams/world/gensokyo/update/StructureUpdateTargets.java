package cc.thonly.reverie_dreams.world.gensokyo.update;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 * Compiled landmark catalogue; the maintenance engine does not re-run a world generator.
 */
public interface StructureUpdateTargets {
    List<Identifier> anchors();

    default Map<Identifier, BoundingBox> bounds() {
        return Map.of();
    }

    CompletableFuture<StructureBlueprint> blueprint(Identifier id);

    Optional<CompletableFuture<StructureBlueprint>> preparedBlueprint(Identifier id);

    boolean hasLegacyBlocks(Identifier id);

    boolean freshRevision(Identifier id);

    void markUpdated(Identifier id, StructureBlueprint revision);
}
