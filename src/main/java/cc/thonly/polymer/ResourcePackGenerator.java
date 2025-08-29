package cc.thonly.polymer;

import cc.thonly.reverie_dreams.LateLoaderInit;
import cc.thonly.reverie_dreams.Touhou;
import eu.pb4.factorytools.api.block.model.generic.BlockStateModelManager;
import eu.pb4.factorytools.api.resourcepack.ModelModifiers;
import eu.pb4.polymer.resourcepack.api.AssetPaths;
import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import eu.pb4.polymer.resourcepack.api.ResourcePackBuilder;
import eu.pb4.polymer.resourcepack.extras.api.format.atlas.AtlasAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelAsset;
import eu.pb4.polymer.resourcepack.extras.api.format.model.ModelElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public class ResourcePackGenerator {
    private static final Set<String> EXPANDABLE = Set.of(
            "wall", "fence", "slab", "stairs", "pressure_plate", "button",
            "glass_pane", "lattice", "bars", "carpet", "chain", "lantern"
    );

    public static void setup() {
        PolymerResourcePackUtils.RESOURCE_PACK_AFTER_INITIAL_CREATION_EVENT.register(ResourcePackGenerator::build);
    }

    private static void build(ResourcePackBuilder builder) {
        final var expansion = new Vec3d(0.08, 0.08, 0.08);
        var atlas = AtlasAsset.builder();

        builder.forEachFile(((string, bytes) -> {
            for (var expandable : EXPANDABLE) {
                if (string.contains(expandable) && string.startsWith("assets/%s/models/block/".formatted(Touhou.MOD_ID))) {
                    var asset = ModelAsset.fromJson(new String(bytes, StandardCharsets.UTF_8));
                    if (asset.parent().isPresent()) {
                        var parentId = asset.parent().get();
                        var parentAsset = ModelAsset.fromJson(new String(Objects.requireNonNull(builder.getDataOrSource(AssetPaths.model(parentId) + ".json")), StandardCharsets.UTF_8));

                        builder.addData(AssetPaths.model(LateLoaderInit.POLYMER_MOD_ID, parentId.getPath()) + ".json", new ModelAsset(parentAsset.parent(), parentAsset.elements().map(x -> x.stream()
                                .map(element -> new ModelElement(element.from().subtract(expansion), element.to().add(expansion),
                                        element.faces(), element.rotation(), element.shade(), element.lightEmission())
                                ).toList()), parentAsset.textures(), parentAsset.display(), parentAsset.guiLight(), parentAsset.ambientOcclusion()).toBytes());
                    }

                    if (asset.elements().isPresent()) {
                        builder.addData(string, new ModelAsset(asset.parent(), asset.elements().map(x -> x.stream()
                                .map(element -> new ModelElement(element.from().subtract(expansion), element.to().add(expansion),
                                        element.faces(), element.rotation(), element.shade(), element.lightEmission())
                                ).toList()), asset.textures(), asset.display(), asset.guiLight(), asset.ambientOcclusion()).toBytes());
                    }
                }
            }
        }));

        for (var entry : BlockStateModelManager.UV_LOCKED_MODELS.get(Touhou.MOD_ID).entrySet()) {
            var expand = EXPANDABLE.stream().anyMatch(expandable -> entry.getKey().contains(expandable) && entry.getKey().startsWith("block/")) ? expansion : Vec3d.ZERO;
            for (var v : entry.getValue()) {
                var suffix = "_uvlock_" + v.x() + "_" + v.y();
                var modelId = v.model().withSuffixedPath(suffix);
                var asset = ModelAsset.fromJson(new String(Objects.requireNonNull(builder.getData(AssetPaths.model(v.model()) + ".json")), StandardCharsets.UTF_8));

                if (asset.parent().isPresent()) {
                    var parentId = asset.parent().get();
                    var parentAsset = ModelAsset.fromJson(new String(Objects.requireNonNull(builder.getDataOrSource(AssetPaths.model(parentId) + ".json")), StandardCharsets.UTF_8));
                    builder.addData(AssetPaths.model(LateLoaderInit.POLYMER_MOD_ID, parentId.getPath() + suffix) + ".json",
                            ModelModifiers.expandModelAndRotateUVLocked(parentAsset, expand, v.x(), v.y()));
                    builder.addData(AssetPaths.model(modelId) + ".json",
                            new ModelAsset(Optional.of(Identifier.of(LateLoaderInit.POLYMER_MOD_ID, parentId.getPath() + suffix)), asset.elements(),
                                    asset.textures(), asset.display(), asset.guiLight(), asset.ambientOcclusion()).toBytes());
                }
            }
        }

        builder.addWriteConverter(((string, bytes) -> {
            if (!string.contains("_uvlock_")) {
                for (var expandable : EXPANDABLE) {
                    if (string.contains(expandable) && string.startsWith("assets/%s/models/block/".formatted(Touhou.MOD_ID))) {
                        var asset = ModelAsset.fromJson(new String(bytes, StandardCharsets.UTF_8));
                        return new ModelAsset(asset.parent().map(x -> Identifier.of(LateLoaderInit.POLYMER_MOD_ID, x.getPath())), asset.elements(), asset.textures(), asset.display(), asset.guiLight(), asset.ambientOcclusion()).toBytes();
                    }
                }
            }
            return bytes;
        }));

        builder.addData("assets/minecraft/atlases/blocks.json", atlas.build());
    }
}
